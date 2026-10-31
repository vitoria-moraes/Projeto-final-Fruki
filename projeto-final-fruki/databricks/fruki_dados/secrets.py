"""Leitura de credenciais SEM expô-las.

No Databricks: dbutils.secrets (escopo criado com a CLI; valores aparecem como [REDACTED] nos notebooks).
Fora do Databricks (desenvolvimento local/testes): variáveis de ambiente FRUKI_<CHAVE>.
Regra de ouro: nunca imprimir, logar nem colocar senha em URL/mensagem de erro.
"""
from __future__ import annotations

import os
from typing import Optional, Protocol

from ..errors import ErroConfiguracao


class SecretProvider(Protocol):
    def get(self, chave: str) -> str: ...


class DatabricksSecretProvider:
    """dbutils.secrets.get(scope, key). O objeto dbutils é injetado (existe nos notebooks/jobs)."""

    def __init__(self, dbutils, escopo: str) -> None:
        self._dbutils = dbutils
        self._escopo = escopo

    def get(self, chave: str) -> str:
        try:
            valor = self._dbutils.secrets.get(scope=self._escopo, key=chave)
        except Exception as exc:  # dbutils lança exceção genérica quando escopo/chave não existem
            raise ErroConfiguracao(
                f"Segredo '{chave}' não encontrado no escopo '{self._escopo}' (ou sem permissão de leitura). "
                f"Crie com: databricks secrets put-secret {self._escopo} {chave}"
            ) from exc
        if not valor:
            raise ErroConfiguracao(f"Segredo '{chave}' do escopo '{self._escopo}' está vazio.")
        return valor


class EnvSecretProvider:
    """Variáveis de ambiente: chave 'supabase-password' -> FRUKI_SUPABASE_PASSWORD."""

    def __init__(self, prefixo: str = "FRUKI_", ambiente: Optional[dict] = None) -> None:
        self._prefixo = prefixo
        self._env = ambiente if ambiente is not None else os.environ

    def get(self, chave: str) -> str:
        nome = self._prefixo + chave.upper().replace("-", "_")
        valor = self._env.get(nome)
        if not valor:
            raise ErroConfiguracao(f"Variável de ambiente {nome} não definida.")
        return valor


def provider_padrao(dbutils=None, escopo: str = "fruki-supabase") -> SecretProvider:
    """Usa dbutils quando disponível; senão cai para variáveis de ambiente."""
    return DatabricksSecretProvider(dbutils, escopo) if dbutils is not None else EnvSecretProvider()
