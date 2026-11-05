# Evidência — Etapas 5 e 6 (subir a aplicação e consultar a recomendação)

- **Data:** 2026-11-05
- **Ambiente:** Windows 11, JDK 21.0.12.1, Spring Boot 3.2.5, Maven Wrapper
- **Workspace Databricks:** `dbc-****` (mascarado de propósito)
- **Tabela consultada:** `workspace.default.gold_recomendacao_b2b` (mock, via `DATABRICKS_RECOMENDACAO_TABLE`)

## 1. Inicialização

Trechos do log (host/credenciais omitidos):

```text
2026-11-05T02:45:16.471  INFO ... Tomcat started on port 8080 (http) with context path ''
2026-11-05T02:45:16.479  INFO ... Started ProjetoFinalFrukiApplication in 5.365 seconds
2026-11-05T02:45:33.358  INFO ... databricks-pool - Start completed.
```

- `pg-pool` foi inicializado (o `EntityManagerFactory` só é criado se o Supabase conectar).
- Nenhuma linha `APPLICATION FAILED TO START` nem `ERROR` de conexão.

## 2. Consultas executadas

| Requisição | Resultado |
|---|---|
| `GET /api/v1/contexto?cidade=Porto Alegre` | 200 |
| `GET /api/v1/recomendacoes/41` | 200 — 3 itens |
| `GET /api/v1/recomendacoes/48` | 200 |
| `GET /api/v1/recomendacoes/81` | 200 |
| `GET /catalogo` (sem token) | 401 — JWT do catálogo ativo |
| `GET /api/v1/contexto/feriados?ano=2026` | OK |
| `POST /api/v1/pedidos` | **a repetir** — 400; suspeita de JSON malformado pelo PowerShell (aspas removidas) |
| `POST /mock/whatsapp/enviar-mensagem` | **a repetir** — mesmo 400 |

## 3. Resposta de `GET /api/v1/recomendacoes/41`

```json
[
  {"idCliente":41,"codItem":"00001","totalQuantidadeComprada":120,"valorTotalGasto":950.50,
   "ultimaDataCompra":"2026-10-20","frequenciaPedidos":8,"scoreRecomendacao":0.92},
  {"idCliente":41,"codItem":"00002","totalQuantidadeComprada":60,"valorTotalGasto":480.00,
   "ultimaDataCompra":"2026-09-30","frequenciaPedidos":5,"scoreRecomendacao":0.75},
  {"idCliente":41,"codItem":"00003","totalQuantidadeComprada":30,"valorTotalGasto":300.00,
   "ultimaDataCompra":"2026-08-15","frequenciaPedidos":3,"scoreRecomendacao":0.40}
]
```

Conclusão: o caminho **Java → Databricks (JDBC) → Java** funciona ponta a ponta.

## 4. Limitações observadas nesta execução

1. **OpenWeather com chave inválida** — `401 Unauthorized` no console; o serviço cai no fallback de 25 °C
   (`OpenWeatherService.java`), então a regra de "dia quente > 28 °C" nunca dispara sem chave válida.
2. **Resposta crua** — o endpoint devolve `cod_item`/score da Gold, sem nome do produto, preço ou posição.
   É o objeto do P1 (`RecomendacaoResponseDTO` + `RecomendacaoService`).
3. **Endpoint sem autenticação** — qualquer `idCliente` pode ser consultado; o JWT protege apenas `/catalogo`.
4. **POSTs ainda não validados** — o `curl.exe` no Windows PowerShell remove as aspas do JSON; repetir com
   `Invoke-RestMethod` + `ConvertTo-Json` (ver `docs/` ou o chat).

## 5. Próximos passos

- P1: DTO + service + handler global (400/404/503) + teste `@WebMvcTest`.
- Guardar também a saída dos POSTs depois de refeitos.
