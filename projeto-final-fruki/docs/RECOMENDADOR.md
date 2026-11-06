# Recomendador V2 — como funciona e por que

Este documento explica a lógica do motor, a inspiração em motores B2B de bebidas,
a fórmula, os pesos, como ajustar e o que ainda não é possível fazer com os dados atuais.

---

## 1. Objetivo

Dado um lojista (`idCliente`), produzir uma lista **ordenada**, **diversa** e
**explicável** de produtos, combinando:

1. o que ele já comprou (histórico da Gold);
2. onde ele está (perfil regional);
3. quando a consulta acontece (estação, clima, feriados/eventos);
4. o valor comercial do item (margem/giro);
5. regras de negócio (não repetir compra recente, não concentrar categoria).

O resultado sai com `score` de 0 a 100, `motivos[]` e `detalhe` por fator — para o
lojista entender e para o grupo defender a decisão.

---

## 2. Inspiração: como grandes bebidas recomendam

Motores B2B de bebidas (como os usados por grandes marcas em portais de pedido)
costumam combinar cinco pilares:

| Pilar | O que faz | O que temos no Pede Fruki |
|---|---|---|
| Histórico de compra | RFM / afinidade item-cliente | Gold com quantidade, valor, frequência e recência |
| Sortimento e afinidade | cesta de compra, cross-sell | regras de categoria (refri ↔ água, energético ↔ cerveja) |
| Demanda local | clima, estação, eventos | OpenWeather + estação + feriados RS/SC |
| Regras comerciais | margem, estoque, promoção | proxy de margem por categoria |
| Explicabilidade | "por que recomendei" | `motivos[]` + `detalhe` |

**O que não temos:** matriz usuário-item densa (para filtragem colaborativa), margem
real, estoque, preço por promoção e elasticidade. O V2 usa heurísticas documentadas no
lugar desses dados — e deixa o encaixe pronto para quando eles existirem.

---

## 3. Fórmula

```
bruto = wRFM·RFM + wREG·regional + wSAZ·sazonalidade + wMAR·margem
score = clamp(100·bruto − penalidades, 0, 100)
```

Pesos padrão (configuráveis em `application.yml`, bloco `app.recomendacao.pesos`):

| Fator | Peso | Faixa |
|---|---|---|
| RFM | 0,50 | 0..1 |
| Regional | 0,20 | 0..1 |
| Sazonalidade | 0,15 | 0..1 |
| Margem/giro | 0,15 | 0..1 |

A soma dos pesos é 1,0. O `bruto` fica em 0..1; multiplicado por 100 vira 0..100;
as penalidades são subtraídas em pontos.

---

## 4. Fatores

### 4.1 RFM (0..1)

Para cada item do histórico:

```
recência   = 1 / (1 + diasDesdeUltimaCompra / 30)
frequência = min(1, frequenciaPedidos / 10)
monetário  = log1p(valorTotalGasto) / log1p(maiorValorDaLista)
quantidade = log1p(totalQuantidadeComprada) / log1p(maiorQuantidadeDaLista)

RFM = 0,35·recência + 0,30·frequência + 0,20·monetário + 0,15·quantidade
```

- Recência usa decaimento suave: 0 dias ≈ 1,0; 30 dias ≈ 0,5; 90 dias ≈ 0,25.
- Frequência satura em 10 pedidos.
- Monetário/quantidade são normalizados **dentro da lista do cliente** (evita
  distorção entre lojistas de portes diferentes).
- Se o dado estiver ausente, o componente vale 0.

### 4.2 Regional (0..1)

O perfil é inferido pela cidade/UF (`PerfilRegional`):

| Perfil | Cidades (exemplos) | Categorias favorecidas |
|---|---|---|
| Mercado Central | Porto Alegre, Canoas, Santa Cruz | refrigerantes, sucos |
| Super Litoral | Capão da Canoa, Tramandaí, Torres, BC | águas, energéticos, cervejas |
| Conveniência Serra | Gramado, Canela, Caxias, Bento | cervejas, sucos |
| Distribuidora Vale | Lajeado, Estrela, Teutônia, Encantado | refrigerantes, sucos, águas |

Matriz completa em `PerfilRegional.aderencia(...)`.

### 4.3 Sazonalidade (0..1)

Base por estação do ano (Hemisfério Sul) + ajustes de contexto:

| Estação | Favorece |
|---|---|
| Verão | águas, refrigerantes, cervejas |
| Outono | sucos, refrigerantes |
| Inverno | sucos, refrescos |
| Primavera | refrigerantes, energéticos |

Ajustes:
- temperatura ≥ 28 °C → +0,08 para águas/energéticos/cervejas;
- temperatura ≤ 12 °C → +0,08 para sucos/refrigerantes;
- feriado/evento nos próximos 10 dias → +0,06 para refrigerantes/cervejas/águas;
- o resultado é limitado a 0..1.

### 4.4 Margem/giro (0..1)

Enquanto não há margem real, usamos um proxy por categoria:

| Categoria | Proxy |
|---|---|
| Energéticos | 0,90 |
| Cervejas | 0,80 |
| Sucos | 0,65 |
| Refrigerantes | 0,60 |
| Refrescos | 0,55 |
| Águas / Tônicas | 0,50 |
| Outros | 0,40 |

Quando a base tiver margem, basta trocar `CategoriaProduto.getMargemProxy()` por uma
consulta ao dado real.

---

## 5. Penalidades

| Regra | Pontos | Motivo |
|---|---|---|
| Comprado nos últimos 7 dias | −15 | evita repetir o que acabou de sair |
| Preço > 2× a mediana da categoria | −20 | protege contra preço fora da curva |
| Preço ausente na tabela | −5 | sinaliza dado incompleto |

Configuráveis em `app.recomendacao.penalidades.*`.

---

## 6. Diversidade

Depois de ordenar por score, o motor percorre a lista e mantém **no máximo 2 itens por
categoria** (`app.recomendacao.diversidade.max-por-categoria`). Isso evita uma vitrine
com 10 refrigerantes e nenhuma água.

---

## 7. Cold start (cliente sem histórico)

Se a Gold não tem linhas para o cliente **mas o cliente existe na base**, o motor:

1. usa o catálogo (`tabela_preco`) como candidatos;
2. aplica RFM neutro (0,50);
3. ordena por regional + sazonalidade + margem;
4. marca o motivo `"sem histórico: seleção por perfil regional"`.

Se o cliente também não existe na base, a API devolve **404** (não uma tela vazia).

---

## 8. Explicabilidade

Cada item devolve:

- `motivos[]`: até 4 frases legíveis ("compra recorrente (8 pedidos)", "perfil Super
  Litoral", "estação: Verão", "feriado próximo: antecipe o pedido").
- `detalhe`: contribuição de cada fator (0..1) e a penalidade total aplicada.

Exemplo de cálculo:

```
RFM = 0,35·0,652 + 0,30·0,800 + 0,20·1,000 + 0,15·1,000 = 0,818
regional = 0,85 (Mercado Central, refrigerantes)
sazonalidade = 0,80 (Primavera + feriado)
margem = 0,60

bruto = 0,50·0,818 + 0,20·0,85 + 0,15·0,80 + 0,15·0,60
      = 0,409 + 0,170 + 0,120 + 0,090 = 0,789
score = 100·0,789 − 0 = 78,9
```

Se o mesmo item tivesse sido comprado há 3 dias: `78,9 − 15 = 63,9`.

---

## 9. Como ajustar

`src/main/resources/application.yml`:

```yaml
app:
  recomendacao:
    limite-padrao: 10
    janela-compra-recente-dias: 7
    pesos:
      rfm: 0.50
      regional: 0.20
      sazonalidade: 0.15
      margem: 0.15
    penalidades:
      comprado-recente: 15.0
      preco-outlier: 20.0
      dado-incompleto: 5.0
    diversidade:
      max-por-categoria: 2
```

Sugestões de experimento:
- mais foco em recompra: `rfm: 0.60`, `sazonalidade: 0.10`;
- mais foco regional (litoral no verão): `regional: 0.30`, `rfm: 0.40`;
- menos penalidade de compra recente: `comprado-recente: 5`.

Depois de mudar, rode `.\mvnw.cmd test` e compare a saída para os IDs 41/48/81.

---

## 10. Limitações e próximos passos

1. **Sem filtragem colaborativa**: a base não tem matriz densa usuário-item. Com mais
   histórico, dá para adicionar "quem comprou X também comprou Y".
2. **Sem margem real**: o proxy por categoria é uma aproximação.
3. **Sem estoque/disponibilidade**: o motor pode recomendar item sem saldo.
4. **Sem elasticidade de preço**: não modela quanto a demanda cai com aumento de preço.
5. **Sem A/B test**: a comparação V1×V2 é analítica, não experimental.
6. **Sem cache distribuído**: cada consulta vai ao Databricks (cold start pode demorar).
7. **`id_cliente` × `cod_cliente`**: o enriquecimento com nome/cidade é best-effort.

Cada uma dessas lacunas está mapeada para uma evolução futura; nenhuma impede a demo.

---

## 11. Como validar

- Testes unitários: `RecomendadorV2ServiceTest` (score, diversidade, penalidade, cold start).
- Teste de serviço: `RecomendacaoServiceTest` (200/404/503).
- Teste de contrato: `RecomendacaoControllerTest` (`@WebMvcTest`).
- Manual:

```powershell
curl.exe -s "http://localhost:8080/api/v1/recomendacoes/41" | ConvertFrom-Json | ConvertTo-Json -Depth 6
```

Confira: `score` entre 0 e 100, `posicao` sequencial, no máximo 2 itens da mesma
categoria, `motivos` preenchidos e `detalhe` coerente com a fórmula.
