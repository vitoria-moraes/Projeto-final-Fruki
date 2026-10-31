"""Nomes de tabelas (Unity Catalog) e quoting seguro de identificadores.

Tudo que vira nome de tabela/coluna em SQL passa por aqui. Valores NUNCA entram em SQL por concatenação
(vão como parâmetros); identificadores são validados contra uma regex estrita e só então recebem crases.
"""
from __future__ import annotations

import re
from dataclasses import dataclass

from databricks.fruki_dados.contracts import DimensionContract
from databricks.fruki_dados.errors import ErroConfiguracao

_IDENT = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def ident(nome: str) -> str:
    """Identificador Spark SQL entre crases. Rejeita qualquer coisa fora de [A-Za-z0-9_]."""
    if not isinstance(nome, str) or not _IDENT.match(nome):
        raise ErroConfiguracao(f"Identificador SQL inválido: {nome!r}")
    return f"`{nome}`"


def pg_ident(nome: str) -> str:
    """Identificador PostgreSQL entre aspas duplas (mesma validação estrita)."""
    if not isinstance(nome, str) or not _IDENT.match(nome):
        raise ErroConfiguracao(f"Identificador SQL inválido: {nome!r}")
    return f'"{nome}"'


@dataclass(frozen=True)
class Naming:
    """catalog.schema.tabela das camadas do Lakehouse."""
    catalog: str = "fruki"
    bronze_schema: str = "bronze"
    silver_schema: str = "silver"

    def _fqn(self, schema: str, tabela: str) -> str:
        return f"{ident(self.catalog)}.{ident(schema)}.{ident(tabela)}"

    def schema_bronze(self) -> str:
        return f"{ident(self.catalog)}.{ident(self.bronze_schema)}"

    def schema_silver(self) -> str:
        return f"{ident(self.catalog)}.{ident(self.silver_schema)}"

    def bronze_supabase(self, c: DimensionContract) -> str:
        """Snapshot fiel do Supabase (sobrescrito a cada execução)."""
        return self._fqn(self.bronze_schema, f"supabase_{c.nome}")

    def bronze_api(self, c: DimensionContract) -> str:
        """Landing append-only dos registros que chegam pela API (trilha de auditoria)."""
        return self._fqn(self.bronze_schema, f"api_{c.nome}")

    def silver(self, c: DimensionContract) -> str:
        return self._fqn(self.silver_schema, c.nome)

    def quarentena(self, c: DimensionContract) -> str:
        return self._fqn(self.silver_schema, f"quarentena_{c.nome}")
