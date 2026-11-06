# Pede Fruki — Guia do projeto e passo a passo

> **Versão:** 1.0 (guia de uso e estudo)
> **Data:** 2026-11-05
> **Status:** backend e painel de testes prontos; 41 testes automatizados verdes.
> **Este documento substitui o antigo `PLANEJAMENTO.md`.** A fase de planejamento
> terminou; aqui está o que existe, como rodar, como usar na tela e o que está congelado.
> **Público:** as duas pessoas do grupo e o mentor.

---

## 1. O que é o projeto

Sistema B2B de recomendação de bebidas Fruki para lojistas. O backend:

- consulta a **Gold** no Databricks (histórico de compras);
- consulta o **Supabase/PostgreSQL** (cliente, preço, vendas);
- aplica um **recomendador V2** (RFM + região + sazonalidade + margem − penalidades);
- devolve um ranking explicável (score 0–100, motivos e detalhe por fator);
- permite **ingestão** de cliente, compra e pedido;
- calcula **indicadores** (cobertura, variedade, taxa);
- expõe **contexto do Sul** (clima, estação, feriados/eventos);
- tem um **painel web** para testar tudo isso sem Postman.

Stack: **Spring Boot 3.2.5 / Java 21**, JPA (Supabase), JDBC (Databricks),
Thymeleaf, JWT no catálogo, WhatsApp (Evolution API), H2 nos testes de JPA.

---

## 2. O que está pronto e usável

### 2.1 Endpoints

| Método | Rota | Para que serve |
|---|---|---|
| GET | `/api/v1/recomendacoes/{idCliente}?limite=10` | ranking V2 explicado |
| GET | `/api/v1/produtos?limite=50` | catálogo da tabela de preços |
| GET | `/api/v1/produtos/{codItem}` | um produto |
| GET | `/api/v1/indicadores` | cobertura, variedade, taxa, média |
| GET | `/api/v1/clientes?limite=200` | lista de clientes (painel) |
| GET | `/api/v1/clientes/{cnpj}` | cliente por CNPJ |
| POST | `/api/v1/clientes` | cadastro (CNPJ validado, 409 duplicado) |
| POST | `/api/v1/compras` | compra item a item (grava `fato_vendas`) |
| POST | `/api/v1/pedidos` | pedido com total real (grava `fato_vendas`) |
| GET | `/api/v1/contexto?cidade=&uf=` | clima + estação + feriados |
| GET | `/api/v1/contexto/estacao?data=` | estação do Hemisfério Sul |
| GET | `/api/v1/contexto/feriados?ano=&cidade=` | feriados/eventos do Sul |
| GET | `/api/v1/contexto/mensagem?cidade=` | texto sazonal para o bot |
| POST | `/api/v1/webhook/whatsapp` | webhook da Evolution API |
| POST | `/mock/whatsapp/enviar-mensagem` | simula o bot sem WhatsApp |
| GET | `/catalogo?token=` | catálogo web (JWT) |

Erros seguem `ProblemDetail` (400/404/405/409/503/500).

### 2.2 Telas

| URL | O que é | Precisa de JWT? |
|---|---|---|
| `/painel.html` | **console de testes** com 11 abas | não |
| `/demo-recomendacoes.html` | demo simples de recomendação/contexto/indicadores | não |
| `/catalogo?token=...` | catálogo B2B (Thymeleaf) | sim |

O painel é a forma mais completa de ver o sistema funcionando. Ele chama a API real e
mostra status HTTP, corpo e erros.

### 2.3 Massa de teste

| Arquivo | Onde rodar | Conteúdo |
|---|---|---|
| `db/seed_produtos.sql` | Supabase | 10 produtos (`00001`–`00010`) |
| `db/seed_clientes.sql` | Supabase | 1001–1020 + 9001 (Pub Hundte) |
| `db/seed_supabase.sql` | Supabase | 20 clientes + produtos + compras |
| `db/seed_80_clientes.sql` | Supabase | **80 clientes** (2001–2080), CNPJs e cidades únicos |
| `db/seed_80_compras.sql` | Supabase | 160 compras |
| `db/seed_80_gold.sql` | Databricks | 368 linhas de Gold para os 80 |
| `db/seed_gold_databricks.sql` | Databricks | 60 linhas para 1001–1020 |

Guia detalhado: `docs/MASSA_DE_TESTE.md`.
No painel, a aba **Todos os CNPJs** lista o que está no banco (`GET /api/v1/clientes`).

---

## 3. Rodar na sua máquina (passo a passo)

### 3.1 Pré-requisitos

- JDK 21 (`java -version`)
- Git
- IntelliJ IDEA (recomendado) ou terminal
- Acesso ao Supabase e ao Databricks (peça as credenciais ao grupo; **não** comite)

### 3.2 Clonar e configurar

```powershell
git clone https://github.com/vitoria-moraes/Projeto-final-Fruki.git
cd Projeto-final-Fruki\projeto-final-fruki
Copy-Item .\scripts\env.example.ps1 .\scripts\env.local.ps1
notepad .\scripts\env.local.ps1
. .\scripts\env.local.ps1
```

Preencha no `env.local.ps1`:

| Variável | Para que serve |
|---|---|
| `SUPABASE_JDBC_URL` | URL JDBC do PostgreSQL |
| `SUPABASE_USER` / `SUPABASE_PASSWORD` | credenciais |
| `JWT_SECRET` | 32+ caracteres (link do catálogo) |
| `DATABRICKS_HOST` | host sem `https://` e sem `:443` |
| `DATABRICKS_HTTP_PATH` | `/sql/1.0/warehouses/<id>` |
| `DATABRICKS_TOKEN` | token |
| `DATABRICKS_RECOMENDACAO_TABLE` | `catalogo.schema.tabela` da Gold |
| `OPENWEATHER_KEY` | opcional; sem ela o clima usa climatologia |

O `env.local.ps1` está no `.gitignore`. **Nunca** coloque token no `application.yml`.

### 3.3 Rodar os testes

```powershell
.\mvnw.cmd test
```

Esperado: **41 testes, 0 falhas, 4 ignorados**. Os 4 ignorados são integrações que
exigem Supabase/JWT no ambiente; com o `env.local.ps1` carregado eles também rodam.

### 3.4 Subir a aplicação

```powershell
.\mvnw.cmd -B spring-boot:run
```

Ou, no IntelliJ, abra `ProjetoFinalFrukiApplication` e clique no ▶ (com as variáveis na
Run Configuration). Espere:

```text
Started ProjetoFinalFrukiApplication in ... seconds
```

### 3.5 Popular o banco (opcional, mas recomendado)

No SQL Editor do Supabase, na ordem:

1. `db/seed_produtos.sql`
2. `db/seed_80_clientes.sql` (ou `seed_clientes.sql` para poucos)
3. `db/seed_80_compras.sql`

No Databricks:

4. `db/seed_80_gold.sql` (ou `seed_gold_databricks.sql`)

Depois abra **http://localhost:8080/painel.html**.

---

## 4. Passo a passo da tela (painel)

### 4.1 Visão geral

- O chip no topo mostra **API online / API 503 / API offline**.
- Os cartões levam às funcionalidades.
- O campo **Cliente em uso** guarda o último ID no navegador.

### 4.2 Recomendação

1. Informe `41` (ou `2001`) e clique em **Consultar**.
2. Veja nome, cidade/UF, perfil, contexto e os itens com **posição, categoria, preço,
   score 0–100, barra, motivos e detalhe** (`rfm`, `regional`, `sazonalidade`,
   `margem`, `penalidade`).
3. **Adicionar ao pedido** leva o item ao carrinho.
4. **Copiar JSON** copia a resposta.

Teste de erro: `999999` → 404; `abc` → 400; Databricks fora → 503.

### 4.3 Contexto do Sul

1. Cidade `Gramado`, UF `RS` → **Contexto**: estação, temperatura, feriado próximo.
2. Troque para `Capão da Canoa` e compare.
3. **Estação** usa a data escolhida.
4. **Feriados** lista nacionais + RS/SC + eventos.
5. **Mensagem** gera o texto do bot.

Sem `OPENWEATHER_KEY`, `fonteClima` é `FALLBACK` (climatologia) e o endpoint continua
funcionando.

### 4.4 Indicadores

**Carregar indicadores** mostra cobertura, variedade, taxa (clientes com 3+ itens) e
média por cliente.

### 4.5 Catálogo

**Carregar** → busca/filtro por categoria → **Adicionar** ao carrinho.

### 4.6 Clientes

1. Preencha `codCliente` (número, ex.: `9001`), CNPJ, nome, cidade, UF.
2. **Cadastrar** → 201; repetir CNPJ → 409; CNPJ inválido → 400.
3. **Buscar** por CNPJ → 200/404.

### 4.7 Todos os CNPJs

1. A aba carrega `GET /api/v1/clientes?limite=500`.
2. Busque por ID, nome, CNPJ ou cidade.
3. **Testar** abre a recomendação daquele cliente.
4. **Copiar** copia o CNPJ para usar em compras/pedidos.

Com o seed de 80, aparecem **2001–2080**; com o de 20, **1001–1020 + 9001**.

### 4.8 Compras

CNPJ existente + `codItem` (ex.: `00001`) + quantidade + data → **Registrar compra**.
201 com total; 404 se o CNPJ não existir; 400 se item/data/preço inválidos.

### 4.9 Pedido

1. Adicione itens no Catálogo ou na Recomendação.
2. Informe o CNPJ e clique em **Enviar pedido**.
3. 201 com `codigoPedido` e `valorTotal` real; o carrinho é limpo.

### 4.10 WhatsApp

1. Telefone `5551999998888`, mensagem `oi` → o bot pede CNPJ.
2. Envie um CNPJ da base.
3. Teste `catalogo`, `recomendação`, `tabela de precos`, `menu`, `sair`.

A sessão fica em memória no servidor; reiniciar a aplicação zera.

### 4.11 Erros

Botões disparam 404, 400, 409, 401 e 400 de validação. Confira o `ProblemDetail`
(`type`, `title`, `status`, `detail`, `instance`, `erros[]`).

---

## 5. Roteiro de demonstração (10 minutos)

1. Abrir `/painel.html` e mostrar **API online**.
2. Recomendação `41`: nome, perfil, score, motivos.
3. Recomendação `2001` (litoral) × `2021` (serra): comparar o topo.
4. Contexto `Gramado` × `Capão da Canoa`.
5. Indicadores.
6. Cadastrar um cliente (201) e repetir (409).
7. Registrar compra e finalizar pedido (201).
8. WhatsApp: `oi` → CNPJ → `recomendação`.
9. Erros: 404/400/401 com ProblemDetail.

---

## 6. O que está congelado (não mudar sem combinar)

Estes contratos foram fechados; mudá-los quebra o painel, os testes ou o front:

1. **Resposta da recomendação** é objeto com `idCliente`, `nomeCliente`, `cidade`, `uf`,
   `perfil`, `contexto`, `itens[]`; cada item tem `posicao`, `codItem`, `nome`,
   `categoria`, `preco`, `score`, `motivos[]`, `detalhe`.
2. **Status HTTP:** 200/201/400/404/405/409/503/500.
3. **Erros** em `ProblemDetail`; validação com `erros[{campo,mensagem}]`.
4. **`codCliente` é número** no cadastro; `cliente.cod_cliente` é `Long`.
5. **`fato_vendas.cod_cliente` é tratado como texto** (`String`) e convertido no serviço.
   Se o seu banco for `bigint`, alinhe entidade + repository antes de mudar.
6. **Fórmula V2 e pesos** (`app.recomendacao.pesos.*`) — mudar exige atualizar
   `docs/RECOMENDADOR.md` e os testes.
7. **Tabela Gold** configurável por `DATABRICKS_RECOMENDACAO_TABLE`; não hardcode.
8. **Catálogo `/catalogo`** exige JWT; o painel não.

---

## 7. Relatório de dúvidas (para levar à IA)

| # | Dúvida | Por que existe | Onde olhar | O que perguntar à IA |
|---|---|---|---|---|
| 1 | `cliente.cod_cliente` é `Long`, mas `fato_vendas.cod_cliente` é `String`? | as tabelas podem ter tipos diferentes; a `cliente` é BIGINT e a `fato_vendas` foi criada como texto | `ClienteEntity`, `PedidoEntity`, `docs/SCHEMA.md` | "Confira os tipos reais e me diga se preciso alinhar as duas entidades" |
| 2 | De onde vem a categoria do produto? | `tabela_preco` não tem coluna de categoria | `CategoriaProduto.fromNome` | "Explique a heurística e como adicionar uma categoria nova" |
| 3 | Por que a margem é um "proxy"? | não há margem real na base | `CategoriaProduto.getMargemProxy` | "Como trocar o proxy por margem real?" |
| 4 | Por que o catálogo `/catalogo` ainda é mock? | o front real depende da Julliane; o painel cobre os testes | `catalogo.html`, `docs/INTEGRACAO_FRONT.md` | "Como integrar o catálogo à API sem quebrar o layout?" |
| 5 | Por que não há cache nem autenticação na API? | decisão de MVP; o JWT protege só o catálogo | `SecurityConfig`, `application.yml` | "Quais os riscos e como evoluir?" |
| 6 | Por que o pipeline não está versionado? | depende dos notebooks/schema da Vitória | `databricks/fruki_dados` | "O que falta para versionar o job?" |
| 7 | Como funciona o cold start? | cliente sem histórico precisa de resposta | `RecomendadorV2Service`, `RecomendacaoService` | "Explique o fluxo e os limites" |
| 8 | O que significa cada campo de `detalhe`? | explicar o score | `docs/RECOMENDADOR.md` | "Mostre o cálculo para um item real" |
| 9 | Por que 4 testes são ignorados? | exigem Supabase/JWT no ambiente | `@EnabledIfEnvironmentVariable` | "Como rodar a suíte completa?" |
| 10 | O que fazer se a Gold tiver outro nome/colunas? | ambiente pode variar | `application.yml`, `RecomendacaoRepository` | "Me guie no ajuste sem quebrar o contrato" |

**O que ainda não está pronto (não fingir que está):** catálogo real consumindo a API,
pipeline Databricks versionado, autenticação da API, cache distribuído e análise da
recomendação com dados reais.

---

## 8. Prompt para a IA da colega

Copie o bloco abaixo e cole na IA que ela usa (com o repositório aberto ou anexado):

```text
Você é meu mentor técnico em Java/Spring Boot. Sou iniciante e estou entrando no
projeto Pede Fruki agora. Quero APRENDER e VALIDAR, não reescrever.

CONTEXTO
- Repositório: https://github.com/vitoria-moraes/Projeto-final-Fruki
- Código: pasta projeto-final-fruki/
- Stack: Spring Boot 3.2.5, Java 21, Supabase/JPA, Databricks/JDBC, Thymeleaf, JWT.
- Leia primeiro: docs/GUIA_DO_PROJETO.md, README.md, docs/API.md,
  docs/RECOMENDADOR.md, docs/MASSA_DE_TESTE.md, docs/GUIA_PAINEL.md e
  src/main/java/org/example/projetofinalfruki/domain/service/RecomendadorV2Service.java.

MINHA TAREFA
1. Me guiar para rodar localmente (Windows/PowerShell): clonar, configurar
   scripts/env.local.ps1, subir a app e abrir /painel.html.
2. Rodar .\mvnw.cmd test e explicar o resultado (esperado: 41 testes, 0 falhas,
   4 ignorados).
3. Me explicar a arquitetura e o fluxo de uma recomendação, classe por classe.
4. Me acompanhar na aba Recomendação, Contexto, Indicadores, Clientes, Compras,
   Pedido, WhatsApp e Erros do painel, dizendo o que observar.
5. Apontar o que eu posso melhorar sem quebrar os contratos congelados da seção 6
   do guia.
6. Montar um plano de estudo de 5 dias com base no que eu não souber.

REGRAS
- Explique o conceito antes de mostrar código.
- Passos pequenos, um por vez.
- Não mude contratos, endpoints, tipos ou schema sem eu confirmar.
- Não peça nem reproduza tokens/senhas.
- Sempre diga como testar (comando, URL, JSON esperado).
- Se não puder verificar (banco/credenciais), diga "não foi possível verificar".
- No fim, gere um resumo do que eu aprendi e das dúvidas que ficaram.
```

### Perguntas que ela pode fazer depois

- "Explique o caminho de `GET /recomendacoes/2001` até o JSON."
- "Por que o score do item 1 é maior que o do item 2?"
- "O que acontece se o Databricks cair?"
- "Como adicionar um produto novo em `tabela_preco` e vê-lo no catálogo?"
- "Como rodar só o `RecomendadorV2ServiceTest`?"
- "O que eu preciso saber para mexer no catálogo sem quebrar o JWT?"

---

## 9. Se algo der errado

| Sintoma | Causa provável | Onde olhar |
|---|---|---|
| Chip `API offline` | app não está rodando ou porta ≠ 8080 | terminal/IntelliJ; `server.port` |
| 503 em `/recomendacoes` | Databricks: host, token, warehouse, tabela/colunas | log do `RecomendacaoRepository`; `Caused by` |
| 404 em `/recomendacoes/{id}` | cliente não existe **e** Gold vazia | `select * from cliente where cod_cliente=...` |
| `preco: null` | `cod_item` ≠ `cod_produto` | `CatalogoService`; `tabela_preco` |
| 503 em `/compras`/`/pedidos` | `fato_vendas` com schema/tipo divergente | `Caused by` no console; `docs/SCHEMA.md` |
| 404 em `/compras` | CNPJ não cadastrado | `select ... from cliente where cnpj=...` |
| 409 em `/clientes` | CNPJ duplicado | `ClienteService` |
| Bot não responde | app reiniciou (sessão em memória) | reinicie a conversa com `oi` |
| Testes ignorados | sem `SUPABASE_JDBC_URL`/`JWT_SECRET` | carregue `env.local.ps1` |

---

## 10. Checklist de aprendizado

- [ ] Sei subir a aplicação e abrir o painel.
- [ ] Sei consultar uma recomendação e explicar `score`, `motivos` e `detalhe`.
- [ ] Sei comparar dois perfis regionais no contexto.
- [ ] Sei cadastrar cliente, comprar e fechar pedido.
- [ ] Sei simular o WhatsApp.
- [ ] Sei provocar e ler um `ProblemDetail`.
- [ ] Sei rodar os testes e interpretar os 4 ignorados.
- [ ] Sei apontar o que ainda não está pronto.

---

## 11. Comandos úteis

```powershell
# testes
.\mvnw.cmd test

# um teste específico
.\mvnw.cmd test -Dtest=RecomendadorV2ServiceTest

# subir
.\mvnw.cmd -B spring-boot:run

# consultar
curl.exe -s "http://localhost:8080/api/v1/recomendacoes/2001?limite=5" | ConvertFrom-Json | ConvertTo-Json -Depth 6

# lista de CNPJs
curl.exe -s "http://localhost:8080/api/v1/clientes?limite=500" | ConvertFrom-Json | Select-Object -First 5

# script de smoke test (app rodando)
.\scripts\testar-api.ps1
```

**Documentos irmãos:** `docs/GUIA_PAINEL.md` (tela), `docs/API.md` (contratos),
`docs/RECOMENDADOR.md` (fórmula), `docs/MASSA_DE_TESTE.md` (dados),
`docs/SCHEMA.md` (tabelas), `docs/INTEGRACAO_FRONT.md` (front).
