# Guia do painel de testes (`/painel.html`)

O painel é uma página única, sem login, que chama a API real e mostra **status HTTP,
corpo da resposta e erros** em cada aba. Serve para você (e para o mentor) validar
tudo o que o backend faz, sem Postman.

> **Pré-requisitos**
> 1. Aplicação rodando (`.\mvnw.cmd spring-boot:run` ou ▶ no IntelliJ).
> 2. Variáveis do Supabase/Databricks carregadas.
> 3. Opcional: massa de teste (`db/seed_supabase.sql` + `db/seed_gold_databricks.sql`)
>    para ter os IDs 1001–1020.

Abra: **http://localhost:8080/painel.html**

---

## 1. Visão geral

- O chip no topo mostra **API online / API 503 / API offline**. Ele é atualizado a cada
  chamada.
- Os quatro cartões levam direto às funcionalidades.
- O “Roteiro de 10 minutos” é a ordem sugerida de teste.
- O campo **Cliente em uso** (na lateral) guarda o último ID no navegador e preenche a
  aba de recomendação.

### Atalhos

| Atalho | O que faz |
|---|---|
| `Recomendação 41` | abre a aba já consultando o cliente 41 |
| `Recomendação 1001` | consulta o cliente de teste 1001 (se a massa estiver carregada) |
| `Contexto Gramado` | abre o contexto de Gramado/RS |
| `Simular WhatsApp` | abre o chat do bot |

---

## 2. Recomendação

**O que testa:** `GET /api/v1/recomendacoes/{idCliente}?limite=`

1. Informe o `idCliente` (ex.: `41` ou `1001`) e o `limite` (1 a 50).
2. Clique em **Consultar**.
3. Observe:
   - **nome do cliente** e cidade/UF (vêm do Supabase, quando o `cod_cliente` casa);
   - **perfil** (Mercado Central, Super Litoral, Conveniência Serra, Distribuidora Vale);
   - faixa de **contexto** (estação, temperatura, feriado próximo);
   - cada item com **posição, categoria, preço, score 0–100, barra, motivos e detalhe**
     (`rfm`, `regional`, `sazonalidade`, `margem`, `penalidade`).
4. Use **Adicionar ao pedido** para levar o item ao carrinho.
5. **Copiar JSON** copia a resposta completa.

**Testes de erro**

| Entrada | Esperado |
|---|---|
| `999999` | 404 com `ProblemDetail` (`cliente-nao-encontrado`) |
| `abc` | 400 (`requisicao-invalida`) |
| Databricks fora | 503 (`databricks-indisponivel`) |

> O `score` é recalculado pelo V2; o `score_recomendacao` da Gold não é usado como
> verdade final. Por isso o mesmo cliente pode ter ordem diferente da V1.

---

## 3. Contexto do Sul

**O que testa:** `/contexto`, `/contexto/estacao`, `/contexto/feriados`, `/contexto/mensagem`.

1. Ajuste **cidade**, **UF** e **data** (a data já vem preenchida com hoje).
2. **Contexto** mostra temperatura, estação, feriado próximo e a lista de eventos.
3. **Estação** calcula a estação do Hemisfério Sul para a data.
4. **Feriados** lista nacionais (BrasilAPI) + estaduais/municipais/eventos do RS/SC.
5. **Mensagem** gera o texto que o bot usaria.

**Experimentos**

| Cidade | O que observar |
|---|---|
| `Gramado` / RS | inverno/serra e eventos como Natal Luz |
| `Capão da Canoa` / RS | litoral e temporada de praia |
| `Lajeado` / RS | vale e aniversário da cidade (26/01) |

> Sem `OPENWEATHER_KEY`, `fonteClima` vem `FALLBACK` e a temperatura é a climatologia
> mensal da região. O endpoint continua funcionando.

---

## 4. Indicadores

**O que testa:** `GET /api/v1/indicadores`

Clique em **Carregar indicadores** e interprete:

| Indicador | Significado |
|---|---|
| Cobertura | % de clientes cadastrados que têm ao menos 1 recomendação |
| Variedade | quantidade de `cod_item` distintos recomendados |
| Taxa de recomendação | % de clientes com recomendação que têm 3+ itens |
| Média por cliente | recomendações por cliente que tem recomendação |

Se o Databricks estiver fora, aparece 503; o corpo do erro fica visível abaixo.

---

## 5. Catálogo

**O que testa:** `GET /api/v1/produtos?limite=100`

1. Clique em **Carregar**.
2. Busque por nome (`guaraná`, `água`, `cerveja`) e/ou filtre por categoria.
3. Clique em **Adicionar** para levar ao carrinho.
4. Vá para a aba **Pedido** para finalizar.

> O catálogo vem de `tabela_preco` (código, nome, categoria inferida e preço). Se um
> produto não tiver preço, aparece `—`, mas ele ainda pode ser adicionado.

---

## 6. Clientes

**O que testa:** `POST /api/v1/clientes` e `GET /api/v1/clientes/{cnpj}`

### Cadastrar

1. Preencha `codCliente` (número, ex.: `9001`), CNPJ, nome, cidade e UF.
2. `codHierarquia` é opcional e numérico.
3. Clique em **Cadastrar**.
4. Esperado: **201** com o cliente criado; o ID vira o “cliente em uso”.
5. Repita o mesmo CNPJ: **409** (`conflito`).
6. CNPJ com dígito errado: **400** (`CNPJ inválido`).

### Consultar

1. Informe o CNPJ (com ou sem máscara) e clique em **Buscar**.
2. Esperado: **200** com os dados ou **404** se não existir.

---

## 7. Compras

**O que testa:** `POST /api/v1/compras`

1. CNPJ de um cliente existente.
2. `codItem` de um produto existente (ex.: `00001`).
3. Quantidade ≥ 1 e data (não futura).
4. `valorUnitario` opcional: se vazio, usa `tabela_preco.preco_base`.
5. Clique em **Registrar compra**.

Esperado: **201** com `valorTotal = quantidade × preço`. O erro 404 indica CNPJ
inexistente; 400 indica item/preço/data inválidos.

---

## 8. Pedido

**O que testa:** `POST /api/v1/pedidos`

1. Adicione itens no **Catálogo** ou na **Recomendação**.
2. Na aba **Pedido**, confira o carrinho e o total estimado.
3. Informe o **CNPJ** de um cliente existente.
4. Clique em **Enviar pedido**.

Esperado: **201** com `codigoPedido`, `status`, `valorTotal` real e `dataCriacao`.
O carrinho é limpo após o sucesso. O total oficial é o do backend (não o estimado).

**Erros comuns:** 404 (CNPJ não cadastrado), 400 (item sem preço/quantidade inválida).

---

## 9. WhatsApp

**O que testa:** `POST /mock/whatsapp/enviar-mensagem` (o mesmo cérebro do webhook).

1. Telefone de teste (ex.: `5551999998888`).
2. Digite `oi` e clique em **Enviar**.
3. O bot pede o CNPJ. Envie um CNPJ real da base (ex.: o que você cadastrou).
4. Depois teste:
   - `catalogo` → link com JWT;
   - `recomendação` → texto com sugestões;
   - `tabela de precos` → prévia de preços;
   - `menu` → menu principal;
   - `sair` → encerra a sessão.

> A sessão fica em memória no servidor. Se reiniciar a aplicação, começa do zero.

---

## 10. Erros (ProblemDetail)

Cada botão dispara um cenário e mostra o corpo:

| Botão | Chamada | Esperado |
|---|---|---|
| 404 cliente inexistente | `GET /api/v1/recomendacoes/999999` | 404 `cliente-nao-encontrado` |
| 400 id inválido | `GET /api/v1/recomendacoes/abc` | 400 `requisicao-invalida` |
| 409 CNPJ duplicado | `POST /api/v1/clientes` com CNPJ da massa | 409 `conflito` |
| 401 catálogo sem JWT | `GET /catalogo` | 401 |
| 400 corpo inválido | `POST /api/v1/clientes` com `{}` | 400 com `erros[]` |

Confira no JSON: `type`, `title`, `status`, `detail`, `instance` e, na validação,
`erros[{campo,mensagem}]`.

---

## 11. Roteiro de aceite (para o mentor)

1. Abrir `/painel.html` e ver o chip **API online**.
2. Consultar `41` e conferir nome, perfil, score, motivos.
3. Consultar `1001` (se a massa existir) e comparar o perfil litoral.
4. Abrir Contexto de Gramado e de Capão da Canoa e comparar estação/eventos.
5. Abrir Indicadores e ler os quatro números.
6. Cadastrar um cliente novo (201) e repetir (409).
7. Registrar uma compra (201) e ver o total.
8. Adicionar dois itens no catálogo, finalizar um pedido (201).
9. Simular WhatsApp: `oi` → CNPJ → `recomendação`.
10. Disparar 404/400/401 e conferir o ProblemDetail.

Se os 10 passarem, o fluxo ponta a ponta está demonstrado.

---

## 12. Se algo der errado

| Sintoma | Onde olhar |
|---|---|
| Chip “API offline” | aplicação não está rodando ou porta diferente de 8080 |
| 503 em recomendação | Databricks: host, token, warehouse, tabela/colunas; veja o log |
| 404 em recomendação | cliente não existe **e** Gold vazia; veja `cliente`/Gold |
| `preco: null` | `cod_item` não casou com `cod_produto`; veja `CatalogoService` |
| 400 em compra/pedido | quantidade, data futura, item sem preço, CNPJ inválido |
| 409 em cliente | CNPJ já cadastrado |
| Chat não responde | app reiniciou (sessão em memória) ou CNPJ não existe |
| Página sem estilo | abra via `http://localhost:8080/painel.html` (não `file://`) |

Logs úteis: `RecomendacaoRepository` (Databricks), `GlobalExceptionHandler`,
`ClienteService`, `PedidoService`, `CatalogoService`.

---

## 13. Limitações do painel

- Não autentica: usa a API aberta (o JWT protege só `/catalogo`).
- O carrinho é local (localStorage), não é um carrinho persistido no servidor.
- O total exibido no carrinho é estimado; o oficial é o retorno de `POST /pedidos`.
- Não substitui os testes automatizados (`.\mvnw.cmd test`); é a validação manual.
