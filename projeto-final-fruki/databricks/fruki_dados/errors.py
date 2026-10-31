"""Exceções do projeto. Cada uma diz claramente DE QUEM é o problema (config, origem, dados, destino)."""


class FrukiError(Exception):
    """Base de todos os erros do projeto."""


class ErroConfiguracao(FrukiError):
    """Falta ou está inválida uma configuração/segredo. Não adianta tentar de novo."""


class ErroOrigem(FrukiError):
    """Falha ao ler do Supabase (rede, credencial, tabela inexistente)."""


class ErroQualidade(FrukiError):
    """Dados violam o contrato (ex.: contagem diferente da origem, chave duplicada)."""


class ErroDestino(FrukiError):
    """Falha ao gravar/consultar no Databricks (Delta, SQL Warehouse)."""
