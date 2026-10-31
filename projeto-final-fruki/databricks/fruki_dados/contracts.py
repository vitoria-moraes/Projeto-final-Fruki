"""Contratos das dimensões: a ÚNICA fonte de verdade sobre colunas, tipos, chaves e regras.

Quem usa este módulo:
  * o pipeline (Spark) gera DDL, casts (try_cast) e validações a partir dele;
  * a API (pydantic) gera os modelos de entrada/saída e as colunas permitidas em filtros a partir dele.
Assim pipeline e API nunca divergem. Este arquivo só usa a biblioteca padrão do Python.

As colunas e a ORDEM seguem exatamente as tabelas higienizadas (dim_*_limpa.csv).
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Optional, Tuple

# Tipos aceitos em ColumnSpec.dtype (são strings válidas em CAST do Spark SQL)
DTYPES_SIMPLES = {"bigint", "int", "string", "double", "boolean", "date", "timestamp"}


@dataclass(frozen=True)
class ColumnSpec:
    """Uma coluna do contrato.

    nullable : False = obrigatória (linha sem ela vai para a quarentena / API devolve 422)
    pattern  : regex que o valor deve satisfazer (strings)
    ge / le  : limites numéricos inclusivos
    lpad     : completa com zeros à esquerda até N dígitos (ex.: cod_item '1' -> '00001'). Defesa contra
               o Supabase/CSV ter convertido código para número e perdido os zeros.
    """
    name: str
    dtype: str
    nullable: bool = True
    pattern: Optional[str] = None
    ge: Optional[float] = None
    le: Optional[float] = None
    lpad: Optional[int] = None

    def __post_init__(self) -> None:
        base = self.dtype.split("(")[0]
        if base not in DTYPES_SIMPLES and base != "decimal":
            raise ValueError(f"dtype não suportado em {self.name}: {self.dtype}")


@dataclass(frozen=True)
class DimensionContract:
    """Contrato completo de uma dimensão."""
    slug: str                        # usado na URL da API (/v1/<slug>)
    nome: str                        # nome lógico (dim_cliente)
    origem_supabase: str             # tabela no Supabase (schema public)
    colunas: Tuple[ColumnSpec, ...]
    chave: Tuple[str, ...]           # chave natural usada no MERGE e na deduplicação
    filtros: Tuple[str, ...]         # colunas aceitas como filtro na API
    cluster_by: Tuple[str, ...] = ()  # liquid clustering da tabela silver

    def __post_init__(self) -> None:
        nomes = [c.name for c in self.colunas]
        if len(nomes) != len(set(nomes)):
            raise ValueError(f"{self.nome}: colunas duplicadas")
        for col in (*self.chave, *self.filtros, *self.cluster_by):
            if col not in nomes:
                raise ValueError(f"{self.nome}: '{col}' não existe nas colunas")
        for col in self.chave:
            if self.coluna(col).nullable and self.slug != "tabelas-preco":
                # exceção documentada: data_validade faz parte da chave da tabela de preço e pode ser nula
                raise ValueError(f"{self.nome}: coluna de chave '{col}' deve ser obrigatória")

    @property
    def nomes(self) -> Tuple[str, ...]:
        return tuple(c.name for c in self.colunas)

    def coluna(self, nome: str) -> ColumnSpec:
        for c in self.colunas:
            if c.name == nome:
                return c
        raise KeyError(nome)

    @property
    def obrigatorias(self) -> Tuple[str, ...]:
        return tuple(c.name for c in self.colunas if not c.nullable)


# ---------------------------------------------------------------------------------------------
# dim_cliente_limpa  (27.490 linhas; 2.925 ativos)
# ---------------------------------------------------------------------------------------------
CLIENTE = DimensionContract(
    slug="clientes",
    nome="dim_cliente",
    origem_supabase="dim_cliente_limpa",
    chave=("cod_cliente",),
    filtros=("cod_cliente", "id_cliente", "estado", "situacao_cliente", "cod_segmento", "tabela_preco", "apto_oferta"),
    cluster_by=("cod_cliente",),
    colunas=(
        ColumnSpec("id_cliente", "bigint", nullable=False, ge=1),
        ColumnSpec("cod_cliente", "bigint", nullable=False, ge=1),
        ColumnSpec("id_hierarquia", "bigint"),
        ColumnSpec("cod_cadeia", "bigint"),
        ColumnSpec("cod_segmento", "int"),
        ColumnSpec("cod_subsegmento", "int"),
        ColumnSpec("cod_situacao_credito", "int"),
        ColumnSpec("id_tabela_preco", "bigint"),
        ColumnSpec("cod_grupo_economico", "bigint"),
        ColumnSpec("estado", "string", pattern=r"^[A-Z]{2}$"),
        ColumnSpec("situacao_cliente", "string", nullable=False, pattern=r"^(Ativo|Inativo)$"),
        ColumnSpec("situacao_credito", "string"),
        ColumnSpec("data_implantacao", "date"),
        ColumnSpec("endereco", "string"),
        ColumnSpec("bairro", "string"),
        ColumnSpec("cidade", "string"),
        ColumnSpec("cep", "string"),
        ColumnSpec("cadeia", "string"),
        ColumnSpec("segmento", "string"),
        ColumnSpec("subsegmento", "string"),
        ColumnSpec("tabela_preco", "string"),
        ColumnSpec("grupo_economico_descricao", "string"),
        ColumnSpec("latitude", "double", ge=-34.0, le=6.0),        # limites do Brasil
        ColumnSpec("longitude", "double", ge=-74.0, le=-32.0),
        ColumnSpec("data_implantacao_valida", "boolean"),
        ColumnSpec("coordenada_status", "string", pattern=r"^(VALIDA|INVALIDA|AUSENTE)$"),
        ColumnSpec("cliente_ativo", "boolean"),
        ColumnSpec("credito_suspenso", "boolean"),
        ColumnSpec("hierarquia_valida", "boolean"),
        ColumnSpec("pertence_cadeia", "boolean"),
        ColumnSpec("tem_grupo_economico", "boolean"),
        ColumnSpec("tem_preco_vigente", "boolean"),
        ColumnSpec("apto_oferta", "boolean"),
    ),
)

# ---------------------------------------------------------------------------------------------
# dim_tabela_preco_limpa  (357.117 linhas de histórico; 20.325 vigentes)
# Chave natural = (tabela_preco, cod_item, data_validade). id_tabela_preco é só o id da LINHA de origem.
# ---------------------------------------------------------------------------------------------
TABELA_PRECO = DimensionContract(
    slug="tabelas-preco",
    nome="dim_tabela_preco",
    origem_supabase="dim_tabela_preco_limpa",
    chave=("tabela_preco", "cod_item", "data_validade"),
    filtros=("tabela_preco", "cod_item", "vigente", "tabela_ativa", "preco_outlier"),
    cluster_by=("tabela_preco", "cod_item"),
    colunas=(
        ColumnSpec("id_tabela_preco", "bigint"),
        ColumnSpec("tabela_preco", "string", nullable=False),
        ColumnSpec("cod_item", "string", nullable=False, pattern=r"^[0-9]{5}$", lpad=5),
        ColumnSpec("id_item", "bigint"),
        ColumnSpec("data_validade", "date"),
        ColumnSpec("tabela_ativa", "string", pattern=r"^(Ativo|Inativo)$"),
        ColumnSpec("preco_venda", "decimal(12,2)", ge=0.01),
        ColumnSpec("preco_minimo", "decimal(12,2)", ge=0.01),
        ColumnSpec("vigente", "boolean"),
        ColumnSpec("versao_mais_recente", "boolean"),
        ColumnSpec("data_validade_invalida", "boolean"),
        ColumnSpec("preco_venda_zerado", "boolean"),
        ColumnSpec("preco_minimo_zerado", "boolean"),
        ColumnSpec("minimo_maior_que_venda", "boolean"),
        ColumnSpec("preco_outlier", "boolean"),
    ),
)

# ---------------------------------------------------------------------------------------------
# dim_hierarquia_comercial  (339 linhas)
# ---------------------------------------------------------------------------------------------
HIERARQUIA = DimensionContract(
    slug="hierarquia",
    nome="dim_hierarquia_comercial",
    origem_supabase="dim_hierarquia_comercial",
    chave=("id_hierarquia",),
    filtros=("id_hierarquia", "cod_representante", "cod_supervisor", "cod_gerente_regional", "cidade"),
    cluster_by=("id_hierarquia",),
    colunas=(
        ColumnSpec("id_hierarquia", "bigint", nullable=False, ge=1),
        ColumnSpec("cod_representante", "bigint", nullable=False),
        ColumnSpec("cod_gerente_regional", "bigint"),
        ColumnSpec("cod_supervisor", "bigint"),
        ColumnSpec("chave", "string"),
        ColumnSpec("grupo_gerencia", "string"),
        ColumnSpec("gerente_regional", "string"),
        ColumnSpec("supervisor", "string"),
        ColumnSpec("representante", "string"),
        ColumnSpec("cod_nome_gerente_regional", "string"),
        ColumnSpec("cod_nome_supervisor", "string"),
        ColumnSpec("cod_nome_representante", "string"),
        ColumnSpec("cidade", "string"),
    ),
)

DIMENSOES: Tuple[DimensionContract, ...] = (CLIENTE, TABELA_PRECO, HIERARQUIA)
POR_SLUG = {d.slug: d for d in DIMENSOES}
POR_NOME = {d.nome: d for d in DIMENSOES}

# Colunas de controle que o pipeline acrescenta (não fazem parte do contrato de negócio)
META_SILVER = ("_ingested_at", "_origem")
