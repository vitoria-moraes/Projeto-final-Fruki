# API — Pede Fruki

Base: `http://localhost:8080`
Formato: JSON (`Content-Type: application/json`)
Erros: `ProblemDetail` (RFC 7807) com `type`, `title`, `status`, `detail`, `instance`.

---

## 1. Recomendações

### `GET /api/v1/recomendacoes/{idCliente}?limite=10`

Consulta o ranking V2 do lojista. `limite` vai de 1 a 50 (padrão 10).

**200 — exemplo**

```json
{
  "idCliente": 41,
  "nomeCliente": "Mercado Central",
  "cidade": "Porto Alegre",
  "uf": "RS",
  "perfil": "Mercado Central",
  "contexto": {
    "cidade": "Porto Alegre",
    "uf": "RS",
    "data": "2026-11-05",
    "estacao": "PRIMAVERA",
    "estacaoDescricao": "Primavera",
    "temperatura": 22.0,
    "fonteClima": "FALLBACK",
    "feriadoProximo": false,
    "descricao": "Primavera em Porto Alegre (RS), 22,0 °C (climatologia da região).",
    "feriados": []
  },
  "itens": [
    {
      "posicao": 1,
      "codItem": "00001",
      "nome": "Fruki Guaraná 2L",
      "categoria": "Refrigerantes",
      "preco": 7.49,
      "score": 92.4,
      "motivos": [
        "compra recorrente (8 pedidos)",
        "compra recente (há 16 dias)",
        "perfil Mercado Central",
        "estação: Primavera"
      ],
      "detalhe": {
        "rfm": 0.873,
        "regional": 0.85,
        "sazonalidade": 0.8,
        "margem": 0.6,
        "penalidade": 0.0
      }
    }
  ]
}
```

**Erros**

| Código | Quando |
|---|---|
| 400 | `idCliente` não numérico, ≤ 0 ou `limite` fora de 1..50 |
| 404 | cliente não existe na base e não há histórico na Gold |
| 503 | Databricks indisponível (token, warehouse, rede) |

```powershell
curl.exe -s "http://localhost:8080/api/v1/recomendacoes/41" | ConvertFrom-Json | ConvertTo-Json -Depth 6
```

---

## 2. Produtos

### `GET /api/v1/produtos?limite=50`

```json
[
  { "codItem": "00001", "nome": "Fruki Guaraná 2L", "categoria": "Refrigerantes", "preco": 7.49 }
]
```

### `GET /api/v1/produtos/{codItem}`

200 com o produto; 404 se não existir.

---

## 3. Indicadores

### `GET /api/v1/indicadores`

```json
{
  "clientesTotal": 2925,
  "clientesComRecomendacao": 410,
  "coberturaPercent": 14.0,
  "itensDistintos": 87,
  "totalRecomendacoes": 1240,
  "taxaRecomendacaoPercent": 62.2,
  "mediaItensPorCliente": 3.0
}
```

- **cobertura**: clientes com ao menos 1 recomendação / clientes cadastrados.
- **variedade**: quantidade de `cod_item` distintos recomendados.
- **taxa**: clientes com 3+ itens / clientes com recomendação.
- **média**: recomendações por cliente com recomendação.

503 se o Databricks estiver fora.

---

## 4. Contexto (clima, estação, feriados)

### `GET /api/v1/contexto?cidade=Porto%20Alegre&uf=RS`

Retorna o mesmo objeto `contexto` acima. Sem `OPENWEATHER_KEY`, `fonteClima` é
`FALLBACK` e a temperatura vem da climatologia mensal da região.

### `GET /api/v1/contexto/estacao?data=2026-12-01`

```json
{ "data": "2026-12-01", "estacao": "PRIMAVERA", "descricao": "Primavera (Hemisfério Sul) em 01/12/2026" }
```

### `GET /api/v1/contexto/feriados?ano=2026&cidade=Gramado`

```json
[
  { "date": "2026-10-24", "name": "Abertura do Natal Luz de Gramado", "type": "TURISMO_SERRA", "estado": "RS", "cidade": "Gramado" }
]
```

Sem `ano`, retorna o ano atual e o seguinte. Fonte: BrasilAPI (nacionais) + curadoria
(estaduais, municipais e eventos do RS/SC), com cache de 12h e fallback.

### `GET /api/v1/contexto/mensagem?cidade=Porto%20Alegre`

Texto pronto para o bot do WhatsApp.

---

## 5. Clientes

### `POST /api/v1/clientes`

```json
{
  "codCliente": 41,
  "cnpj": "11.222.333/0001-81",
  "nomeFantasia": "Mercado Central",
  "cidade": "Porto Alegre",
  "uf": "RS",
  "codHierarquia": 123
}
```

**201** com o cliente criado e header `Location`.

| Código | Quando |
|---|---|
| 400 | campo inválido / CNPJ com dígito errado |
| 409 | CNPJ já cadastrado |

### `GET /api/v1/clientes/{cnpj}`

200 ou 404.

### `GET /api/v1/clientes?limite=200`

Lista os clientes cadastrados (1 a 500), ordenados por `codCliente`. Usado pela aba
**Todos os CNPJs** do painel.

```json
[
  { "codCliente": 2001, "cnpj": "20000001000143", "nomeFantasia": "Mercado Capão da Canoa 2001",
    "cidade": "Capão da Canoa", "uf": "RS", "codHierarquia": null }
]
```

---

## 6. Compras

### `POST /api/v1/compras`

```json
{
  "cnpj": "11.222.333/0001-81",
  "codItem": "00001",
  "quantidade": 3,
  "dataCompra": "2026-11-05",
  "valorUnitario": null
}
```

**201**

```json
{
  "id": 7,
  "cnpj": "11222333000181",
  "nomeFantasia": "Mercado Central",
  "codItem": "00001",
  "quantidade": 3,
  "valorUnitario": 7.49,
  "valorTotal": 22.47,
  "dataCompra": "2026-11-05"
}
```

- `valorUnitario` é opcional; se ausente, usa `tabela_preco.preco_base`.
- 400: quantidade ≤ 0, item inválido, data futura, preço inexistente.
- 404: CNPJ não cadastrado.

---

## 7. Pedidos

### `POST /api/v1/pedidos`

```json
{
  "cnpjCliente": "11.222.333/0001-81",
  "itens": [
    { "codigoProduto": "00001", "quantidade": 2 },
    { "codigoProduto": "00002", "quantidade": 1 }
  ]
}
```

**201**

```json
{
  "codigoPedido": "FRK-5-1730000000000",
  "status": "CRIADO",
  "valorTotal": 37.88,
  "dataCriacao": "2026-11-05T20:00:00",
  "mensagem": "Pedido registrado com sucesso."
}
```

O total é calculado com a tabela de preços e o pedido é gravado em `fato_vendas`.
400/404 conforme validação e cliente.

---

## 8. WhatsApp

### `POST /api/v1/webhook/whatsapp`

Payload da Evolution API (`messages.upsert`). Configure o header `X-Webhook-Token`
quando `EVOLUTION_WEBHOOK_TOKEN` estiver definido.

### `POST /mock/whatsapp/enviar-mensagem`

```json
{ "telefone": "5551999998888", "mensagem": "oi" }
```

Resposta:

```json
{ "remetente": "Bot Fruki", "paraTelefone": "5551999998888", "resposta": "🥤 *Bem-vindo..." }
```

---

## 9. Catálogo

`GET /catalogo?token=<JWT>` — a página exige o token gerado pelo bot. Sem token: 401.
A demo de integração fica em `/demo-recomendacoes.html` (não exige token).

---

## 10. Catálogo de erros

| Código | `type` | Significado |
|---|---|---|
| 400 | `requisicao-invalida` | validação, JSON inválido, parâmetro errado |
| 404 | `cliente-nao-encontrado` | cliente inexistente |
| 404 | `produto-nao-encontrado` | produto inexistente |
| 404 | `nao-encontrado` | rota inexistente |
| 405 | `metodo-nao-permitido` | verbo HTTP errado |
| 409 | `conflito` | CNPJ duplicado |
| 503 | `databricks-indisponivel` | camada analítica fora |
| 500 | `erro-interno` | erro inesperado (logado) |

Exemplo de 400:

```json
{
  "type": "https://pedefruki.local/erros/requisicao-invalida",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos.",
  "instance": "/api/v1/clientes",
  "erros": [
    { "campo": "cnpj", "mensagem": "CNPJ é obrigatório" }
  ]
}
```
