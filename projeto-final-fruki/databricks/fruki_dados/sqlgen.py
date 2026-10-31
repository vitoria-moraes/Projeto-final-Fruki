"""Geração de SQL (dialeto Spark/Databricks) a partir dos contratos. Funções PURAS: só devolvem texto.

Por que gerar SQL em vez de usar a API de DataFrame?
  * a lógica fica legível (dá para copiar o texto e rodar no SQL Editor),
  * é testável sem Spark (testes unitários comparam strings),
  * funciona igual em job, notebook e SQL Warehouse (a API usa o mesmo gerador).

Segurança: identificadores passam por naming.ident(); VALORES nunca são concatenados (vão como parâmetros ou
são literais fixos vindos do contrato, que rejeitamos se tiverem aspas ou barras).
"""
from __future__ import annotations

from typing import Iterable, Mapping, Optional, Sequence, Set, Tuple

from ..contracts import META_SILVER, ColumnSpec, DimensionContract
from ..errors import ErroConfiguracao
from ..naming import ident

TBLPROPS_PADRAO = {
    "delta.autoOptimize.optimizeWrite": "true",        # arquivos maiores já na escrita
    "delta.autoOptimize.autoCompact": "true",          # compacta arquivos pequenos automaticamente
    "delta.deletedFileRetentionDuration": "interval 7 days",
    "delta.logRetentionDuration": "interval 30 days",
}


def _literal(texto: str) -> str:
    """Literal de string SQL. Contratos só têm regex simples: rejeitamos aspas e barras por segurança."""
    if "'" in texto or "\\" in texto:
        raise ErroConfiguracao(f"Literal não permitido no contrato: {texto!r}")
    return f"'{texto}'"


def _tblprops(extra: Optional[Mapping[str, str]] = None) -> str:
    props = {**TBLPROPS_PADRAO, **(extra or {})}
    return ", ".join(f"'{k}' = '{v}'" for k, v in props.items())


def ddl_tipo(c: ColumnSpec) -> str:
    return c.dtype.upper()


# ----------------------------------------------------------------------------------------- DDL
def sql_create_silver(contract: DimensionContract, fqn: str, *, liquid_clustering: bool = True) -> str:
    """Tabela Delta da camada silver. NOT NULL nas obrigatórias: o Delta rejeita o que violar o contrato."""
    linhas = [f"  {ident(c.name)} {ddl_tipo(c)}{'' if c.nullable else ' NOT NULL'}" for c in contract.colunas]
    linhas += ["  `_ingested_at` TIMESTAMP", "  `_origem` STRING"]
    cluster = ""
    if liquid_clustering and contract.cluster_by:
        cluster = f"\nCLUSTER BY ({', '.join(ident(c) for c in contract.cluster_by)})"
    return (
        f"CREATE TABLE IF NOT EXISTS {fqn} (\n" + ",\n".join(linhas) + "\n)\n"
        f"USING DELTA{cluster}\n"
        f"COMMENT {_literal('Silver | ' + contract.nome + ' | tipada, validada e sem duplicidade de chave')}\n"
        f"TBLPROPERTIES ({_tblprops()})"
    )


def sql_create_bronze_api(contract: DimensionContract, fqn: str) -> str:
    """Landing append-only da API: mesmas colunas tipadas + metadados de auditoria."""
    linhas = [f"  {ident(c.name)} {ddl_tipo(c)}" for c in contract.colunas]
    linhas += ["  `_ingested_at` TIMESTAMP", "  `_origem` STRING", "  `_request_id` STRING"]
    return (
        f"CREATE TABLE IF NOT EXISTS {fqn} (\n" + ",\n".join(linhas) + "\n)\n"
        f"USING DELTA\n"
        f"COMMENT {_literal('Bronze | registros recebidos pela API (append-only) | ' + contract.nome)}\n"
        f"TBLPROPERTIES ({_tblprops()})"
    )


def sql_set_tblproperties(fqn: str) -> str:
    return f"ALTER TABLE {fqn} SET TBLPROPERTIES ({_tblprops()})"


# ----------------------------------------------------------------------------------------- tipagem
def expr_tipada(c: ColumnSpec, disponivel: bool = True) -> str:
    """Expressão que converte a coluna de origem para o tipo do contrato SEM estourar erro (try_cast).

    Em Databricks com ANSI ativo, CAST inválido lança exceção; try_cast devolve NULL e deixamos a regra de
    validação mandar a linha para a quarentena, em vez de derrubar o job inteiro.
    """
    tipo = c.dtype
    if not disponivel:
        return f"CAST(NULL AS {tipo.upper()})"
    col = ident(c.name)
    if c.dtype == "string":
        base = f"CAST({col} AS STRING)"
        if c.lpad:
            base = f"lpad({base}, {int(c.lpad)}, '0')"
        return f"NULLIF(trim({base}), '')"
    return f"try_cast({col} AS {tipo})"


def expr_motivo(contract: DimensionContract, disponiveis: Set[str]) -> str:
    """Texto com TODOS os motivos de invalidade da linha (NULL quando a linha é válida)."""
    partes = []
    for c in contract.colunas:
        disp = c.name in disponiveis
        tipada = expr_tipada(c, disp)
        nome = c.name
        if not c.nullable:
            partes.append(f"CASE WHEN {tipada} IS NULL THEN {_literal(nome + ': obrigatório ou inválido')} END")
        elif disp and c.dtype != "string":
            partes.append(f"CASE WHEN {ident(nome)} IS NOT NULL AND {tipada} IS NULL "
                          f"THEN {_literal(nome + ': valor inválido para ' + c.dtype)} END")
        if c.pattern:
            partes.append(f"CASE WHEN {tipada} IS NOT NULL AND NOT ({tipada} RLIKE {_literal(c.pattern)}) "
                          f"THEN {_literal(nome + ': formato inválido')} END")
        if c.ge is not None:
            partes.append(f"CASE WHEN {tipada} < {c.ge} THEN {_literal(nome + f': abaixo do mínimo {c.ge}')} END")
        if c.le is not None:
            partes.append(f"CASE WHEN {tipada} > {c.le} THEN {_literal(nome + f': acima do máximo {c.le}')} END")
    return "NULLIF(concat_ws('; ', " + ",\n    ".join(partes) + "), '')"


def sql_stage_view(contract: DimensionContract, view: str,
                   fontes: Sequence[Tuple[str, str, Set[str]]]) -> str:
    """View temporária que une as fontes (snapshot do Supabase + landing da API), já tipada e com _motivo.

    fontes: lista de (tabela_fqn, rotulo_origem, colunas_disponiveis_na_tabela)
    """
    selects = []
    for tabela, origem, disponiveis in fontes:
        cols = [f"{expr_tipada(c, c.name in disponiveis)} AS {ident(c.name)}" for c in contract.colunas]
        cols += [
            "CAST(`_ingested_at` AS TIMESTAMP) AS `_ingested_at`" if "_ingested_at" in disponiveis
            else "current_timestamp() AS `_ingested_at`",
            f"{_literal(origem)} AS `_origem`",
            f"{expr_motivo(contract, disponiveis)} AS `_motivo`",
        ]
        selects.append("SELECT\n  " + ",\n  ".join(cols) + f"\nFROM {tabela}")
    return f"CREATE OR REPLACE TEMP VIEW {ident(view)} AS\n" + "\nUNION ALL\n".join(selects)


# ----------------------------------------------------------------------------------------- silver / quarentena
def sql_silver_overwrite(contract: DimensionContract, silver_fqn: str, view: str) -> str:
    """Substitui o conteúdo da silver (INSERT OVERWRITE é atômico no Delta: leitores veem a versão antiga até o commit).

    Deduplicação por chave: registros vindos da API vencem o snapshot do Supabase; entre iguais, o mais recente.
    """
    chaves = ", ".join(ident(k) for k in contract.chave)
    cols = ", ".join(ident(n) for n in (*contract.nomes, *META_SILVER))
    return (
        f"INSERT OVERWRITE TABLE {silver_fqn}\n"
        f"SELECT {cols}\nFROM (\n"
        f"  SELECT *, row_number() OVER (\n"
        f"    PARTITION BY {chaves}\n"
        f"    ORDER BY CASE WHEN `_origem` = 'API' THEN 1 ELSE 0 END DESC, `_ingested_at` DESC\n"
        f"  ) AS `_rn`\n"
        f"  FROM {ident(view)}\n"
        f"  WHERE `_motivo` IS NULL\n"
        f") dedup\nWHERE `_rn` = 1"
    )


def sql_quarentena(contract: DimensionContract, quarentena_fqn: str, view: str) -> str:
    cols = ", ".join(ident(n) for n in (*contract.nomes, *META_SILVER, "_motivo"))
    return (
        f"CREATE OR REPLACE TABLE {quarentena_fqn}\n"
        f"COMMENT {_literal('Linhas rejeitadas pela validação do contrato | ' + contract.nome)}\n"
        f"AS SELECT {cols}, current_timestamp() AS `_quarentena_em`\n"
        f"FROM {ident(view)}\nWHERE `_motivo` IS NOT NULL"
    )


def sql_contagem_stage(view: str) -> str:
    return (f"SELECT COUNT(*) AS total, SUM(CASE WHEN `_motivo` IS NULL THEN 0 ELSE 1 END) AS invalidas "
            f"FROM {ident(view)}")


def sql_verifica_chave(contract: DimensionContract, silver_fqn: str) -> str:
    """Total de linhas x chaves distintas: se forem diferentes, há duplicidade de chave."""
    campos = ", ".join(ident(k) for k in contract.chave)
    return f"SELECT COUNT(*) AS n, COUNT(DISTINCT struct({campos})) AS distintas FROM {silver_fqn}"


# ----------------------------------------------------------------------------------------- manutenção
def sql_optimize(fqn: str) -> str:
    return f"OPTIMIZE {fqn}"


def sql_vacuum(fqn: str, horas: int = 168) -> str:
    if horas < 168:
        raise ErroConfiguracao("Retenção abaixo de 168h (7 dias) pode quebrar leituras e time travel: recusado.")
    return f"VACUUM {fqn} RETAIN {int(horas)} HOURS"


def sql_analyze(fqn: str) -> str:
    return f"ANALYZE TABLE {fqn} COMPUTE STATISTICS"


# ----------------------------------------------------------------------------------------- API (INSERT/MERGE/SELECT)
def _valor_param(valor) -> str:
    """Todo parâmetro vai como STRING e é convertido no SQL com CAST: elimina ambiguidade de tipo do conector."""
    if isinstance(valor, bool):
        return "true" if valor else "false"
    if hasattr(valor, "isoformat"):
        return valor.isoformat()
    return str(valor)


def sql_insert_api(contract: DimensionContract, bronze_api_fqn: str, registros: Sequence[Mapping]) -> Tuple[str, dict]:
    """INSERT multi-linha em UMA instrução (um único commit Delta = tudo ou nada). Retorna (sql, parametros)."""
    if not registros:
        raise ValueError("Nenhum registro para inserir.")
    colunas = [*contract.nomes, "_ingested_at", "_origem", "_request_id"]
    params: dict = {}
    linhas_sql = []
    for i, reg in enumerate(registros):
        valores = []
        for j, c in enumerate(contract.colunas):
            v = reg.get(c.name)
            if v is None:
                valores.append(f"CAST(NULL AS {c.dtype.upper()})")
            else:
                nome = f"p{i}_{j}"
                params[nome] = _valor_param(v)
                valores.append(f"CAST(:{nome} AS {c.dtype.upper()})")
        valores += ["current_timestamp()", "'API'", ":request_id"]
        linhas_sql.append("(" + ", ".join(valores) + ")")
    sql = (f"INSERT INTO {bronze_api_fqn} ({', '.join(ident(c) for c in colunas)})\nVALUES\n  "
           + ",\n  ".join(linhas_sql))
    return sql, params


def sql_merge_api(contract: DimensionContract, silver_fqn: str, bronze_api_fqn: str) -> str:
    """MERGE (upsert) do lote recém-gravado na silver. Chave comparada com <=> (igualdade que trata NULL)."""
    nomes = (*contract.nomes, *META_SILVER)
    on = " AND ".join(f"t.{ident(k)} <=> s.{ident(k)}" for k in contract.chave)
    atualizar = ",\n    ".join(f"t.{ident(n)} = s.{ident(n)}" for n in nomes)
    cols = ", ".join(ident(n) for n in nomes)
    valores = ", ".join(f"s.{ident(n)}" for n in nomes)
    return (
        f"MERGE INTO {silver_fqn} AS t\n"
        f"USING (SELECT {cols} FROM {bronze_api_fqn} WHERE `_request_id` = :request_id) AS s\n"
        f"ON {on}\n"
        f"WHEN MATCHED THEN UPDATE SET\n    {atualizar}\n"
        f"WHEN NOT MATCHED THEN INSERT ({cols}) VALUES ({valores})"
    )


def sql_select(contract: DimensionContract, silver_fqn: str, filtros: Mapping[str, object],
               limit: int, offset: int) -> Tuple[str, str, dict]:
    """SELECT paginado + COUNT com os mesmos filtros. Retorna (sql_dados, sql_total, parametros)."""
    permitidos = set(contract.filtros)
    condicoes, params = [], {}
    for i, (campo, valor) in enumerate(filtros.items()):
        if campo not in permitidos:
            raise ValueError(f"Filtro não permitido: {campo}. Permitidos: {', '.join(sorted(permitidos))}")
        spec = contract.coluna(campo)
        nome = f"f{i}"
        params[nome] = _valor_param(valor)
        condicoes.append(f"{ident(campo)} = CAST(:{nome} AS {spec.dtype.upper()})")
    onde = ("\nWHERE " + " AND ".join(condicoes)) if condicoes else ""
    ordem = ", ".join(ident(k) for k in contract.chave)
    cols = ", ".join(ident(n) for n in contract.nomes)
    limit, offset = int(limit), int(offset)           # inteiros já validados na borda; int() garante o tipo
    if limit < 1 or limit > 500 or offset < 0:
        raise ValueError("limit deve estar entre 1 e 500 e offset deve ser >= 0.")
    dados = f"SELECT {cols}\nFROM {silver_fqn}{onde}\nORDER BY {ordem}\nLIMIT {limit} OFFSET {offset}"
    total = f"SELECT COUNT(*) AS total FROM {silver_fqn}{onde}"
    return dados, total, params
