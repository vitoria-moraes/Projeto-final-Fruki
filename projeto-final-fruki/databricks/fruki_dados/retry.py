"""Retry com backoff exponencial e classificação de erros transitórios (puro, sem dependências)."""
from __future__ import annotations

import logging
import time
from typing import Callable, TypeVar

log = logging.getLogger("fruki.retry")
T = TypeVar("T")

# Trechos de mensagem que indicam falha TEMPORÁRIA (vale tentar de novo)
_TRANSITORIOS = (
    "timeout", "timed out", "connection reset", "connection refused", "connection closed",
    "connection is closed", "broken pipe", "too many connections", "remaining connection slots",
    "temporarily unavailable", "could not connect", "network is unreachable", "no route to host",
    "ssl syscall", "eof detected", "server closed the connection", "503", "502", "429",
    "session expired", "invalid session", "requesterror",
)

# Trechos que indicam erro PERMANENTE (retry só atrasa o diagnóstico)
_PERMANENTES = (
    "password authentication failed", "authentication failed", "permission denied", "does not exist",
    "unauthorized", "access denied", "syntax error", "invalid_parameter_value", "parse_syntax_error",
)


def is_transient(exc: BaseException) -> bool:
    msg = f"{type(exc).__name__}: {exc}".lower()
    if any(p in msg for p in _PERMANENTES):
        return False
    return any(t in msg for t in _TRANSITORIOS)


def with_retry(fn: Callable[[], T], *, tentativas: int = 3, atraso_inicial: float = 2.0, fator: float = 2.0,
               descricao: str = "operação", dormir: Callable[[float], None] = time.sleep) -> T:
    """Executa fn(); em erro transitório espera 2s, 4s, 8s... e tenta de novo. Erro permanente sobe na hora."""
    atraso = atraso_inicial
    for tentativa in range(1, tentativas + 1):
        try:
            return fn()
        except Exception as exc:  # noqa: BLE001 - classificamos abaixo
            if tentativa == tentativas or not is_transient(exc):
                raise
            log.warning("%s falhou (tentativa %d/%d): %s. Nova tentativa em %.0fs",
                        descricao, tentativa, tentativas, type(exc).__name__, atraso)
            dormir(atraso)
            atraso *= fator
    raise AssertionError("inalcançável")  # pragma: no cover
