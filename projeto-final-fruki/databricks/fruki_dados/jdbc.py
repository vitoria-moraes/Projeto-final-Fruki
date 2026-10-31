"""Leitura do Supabase (PostgreSQL) via JDBC com Spark.

Pontos de atenção do Supabase:
  * Conexão DIRETA (db.<ref>.supabase.co:5432) é IPv6 por padrão; muitos clusters Databricks só têm IPv4.
    Use o POOLER em modo sessão: aws-0-<regiao>.pooler.supabase.com:5432, usuário 'postgres.<ref>'.
  * O pooler em modo transação (porta 6543) não suporta prepared statements: por isso prepareThreshold=0.
  * Cada partição do Spark abre uma conexão: mantenha numPartitions baixo (4 é mais que suficiente aqui).
"""
from __future__ import annotations

import logging
from dataclasses import dataclass, field
from typing import Any, Dict, Optional

from ..errors import ErroConfiguracao, ErroOrigem
from ..naming import pg_ident
from ..retry import with_retry
from .secrets import SecretProvider

log = logging.getLogger("fruki.jdbc")

DRIVER = "org.postgresql.Driver"


@dataclass(frozen=True)
class SupabaseSettings:
    host: str
    port: int
    database: str
    user: str
    password: str = field(repr=False)          # repr=False: nunca aparece em logs/tracebacks de dataclass
    sslmode: str = "require"

    @classmethod
    def from_secrets(cls, secrets: SecretProvider) -> "SupabaseSettings":
        try:
            porta = int(secrets.get("supabase-port"))
        except ValueError as exc:
            raise ErroConfiguracao("O segredo 'supabase-port' deve ser um número (ex.: 5432).") from exc
        return cls(
            host=secrets.get("supabase-host"),
            port=porta,
            database=secrets.get("supabase-database"),
            user=secrets.get("supabase-user"),
            password=secrets.get("supabase-password"),
        )

    def jdbc_url(self) -> str:
        """URL SEM usuário/senha (eles vão como opções separadas, para não vazar em logs do Spark)."""
        return (
            f"jdbc:postgresql://{self.host}:{self.port}/{self.database}"
            f"?sslmode={self.sslmode}&prepareThreshold=0&connectTimeout=15&socketTimeout=600"
            f"&ApplicationName=fruki-databricks"
        )


def build_jdbc_options(s: SupabaseSettings, *, dbtable: Optional[str] = None, query: Optional[str] = None,
                       fetchsize: int = 10_000, particao: Optional[Dict[str, Any]] = None) -> Dict[str, str]:
    """Monta o dicionário de opções do spark.read.format('jdbc'). Exatamente um entre dbtable e query."""
    if (dbtable is None) == (query is None):
        raise ValueError("Informe exatamente um entre 'dbtable' e 'query'.")
    opcoes: Dict[str, str] = {
        "url": s.jdbc_url(),
        "driver": DRIVER,
        "user": s.user,
        "password": s.password,
        "fetchsize": str(fetchsize),
    }
    if dbtable is not None:
        opcoes["dbtable"] = dbtable
    else:
        opcoes["query"] = query  # type: ignore[assignment]
    if particao:
        opcoes.update({
            "partitionColumn": particao["coluna"],
            "lowerBound": str(particao["minimo"]),
            "upperBound": str(particao["maximo"]),
            "numPartitions": str(particao["particoes"]),
        })
    return opcoes


def tabela_qualificada(schema: str, tabela: str) -> str:
    return f"{pg_ident(schema)}.{pg_ident(tabela)}"


def _consultar(spark, s: SupabaseSettings, sql: str, fetchsize: int = 1000):
    opcoes = build_jdbc_options(s, query=sql, fetchsize=fetchsize)
    return spark.read.format("jdbc").options(**opcoes).load()


def contar_linhas(spark, s: SupabaseSettings, schema: str, tabela: str) -> int:
    """SELECT COUNT(*) na origem (barato) para reconciliar com o que foi gravado no Delta."""
    sql = f"SELECT COUNT(*) AS n FROM {tabela_qualificada(schema, tabela)}"
    try:
        return int(with_retry(lambda: _consultar(spark, s, sql).collect()[0]["n"], descricao=f"contagem de {tabela}"))
    except Exception as exc:
        raise ErroOrigem(f"Falha ao contar linhas de {schema}.{tabela} no Supabase: {type(exc).__name__}") from exc


def limites_particao(spark, s: SupabaseSettings, schema: str, tabela: str, coluna: str):
    """MIN/MAX da coluna numérica usada para dividir a leitura em partições paralelas."""
    sql = (f"SELECT MIN({pg_ident(coluna)}) AS lo, MAX({pg_ident(coluna)}) AS hi "
           f"FROM {tabela_qualificada(schema, tabela)}")
    linha = with_retry(lambda: _consultar(spark, s, sql).collect()[0], descricao=f"limites de {tabela}")
    return linha["lo"], linha["hi"]


def ler_tabela(spark, s: SupabaseSettings, schema: str, tabela: str, *, fetchsize: int = 10_000,
               coluna_particao: Optional[str] = None, particoes: int = 1):
    """Lê a tabela inteira do Supabase para um DataFrame (retry em falhas transitórias de rede).

    Com coluna_particao (inteira e indexada), o Spark faz `particoes` leituras paralelas por faixas de valor.
    As faixas só definem o 'passo': linhas fora de [min, max] não são perdidas.
    """
    particao = None
    if coluna_particao and particoes > 1:
        lo, hi = limites_particao(spark, s, schema, tabela, coluna_particao)
        if lo is not None and hi is not None and hi > lo:
            particao = {"coluna": coluna_particao, "minimo": lo, "maximo": hi, "particoes": particoes}
    opcoes = build_jdbc_options(s, dbtable=tabela_qualificada(schema, tabela), fetchsize=fetchsize, particao=particao)

    def _ler():
        return spark.read.format("jdbc").options(**opcoes).load()

    try:
        return with_retry(_ler, descricao=f"leitura JDBC de {tabela}")
    except Exception as exc:
        # Não propagamos a mensagem original inteira: drivers JDBC às vezes ecoam a URL de conexão
        raise ErroOrigem(f"Falha ao ler {schema}.{tabela} do Supabase ({type(exc).__name__}). "
                         f"Verifique host/porta (pooler IPv4), usuário 'postgres.<ref>', SSL e rede do cluster.") from exc
