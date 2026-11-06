# Pede Fruki — Plano de desenvolvimento em dupla

> **Versão:** 2.0 (planejamento + relatório de execução)
> **Data:** 2026-11-05
> **Status:** T01–T07 e T09–T11 concluídos; T08 parcial; T12 bloqueado; T13 parcial; T14 pendente.
> **Público:** as duas pessoas desenvolvedoras e o mentor.
> **Repositório:** https://github.com/vitoria-moraes/Projeto-final-Fruki
>
> **Atualização:** a implementação foi feita depois da versão 1.0. O relatório do que
> foi entregue, como validar, o que ainda falta e um guia de troubleshooting estão na
> **seção 15**.

---

## Como usar este documento

1. Leiam juntas as seções 1 a 8 (contexto, pendências, tarefas, contratos e Git).
2. Façam a reunião da seção **Próxima ação recomendada** antes de escrever código.
3. Cada pessoa trabalha na sua branch e consulta o **cartão da tarefa** (seção 10).
4. A Pessoa B usa o **prompt da seção 11** em outra IA para receber orientação.
5. A integração segue a **matriz da seção 12** e o **checklist da seção 13**.

> Regra de ouro: **contrato antes de código**. Se uma tarefa exigir mudar um contrato, parem e conversem.

---

## Quadro duplo (resumo)

| Pessoa A | Pessoa B | Em conjunto |
|---|---|---|
| Contrato da API + DTOs + exceções | `GlobalExceptionHandler` (400/404/503) | Fechar contratos C1–C5 |
| `RecomendacaoService` + controller fino | `GET /api/v1/indicadores` | Integração e testes de fluxo |
| Recomendador V2 (score, diversidade, motivos) | `POST /clientes` + `POST /compras` | Revisão de PRs |
| Testes do recomendador | `PedidoService` real + testes de ingestão | Aceite dos 10 requisitos |
| Pipeline Databricks (bloqueado) | Catálogo consumindo a API | Pitch e demonstração |
| README / arquitetura | `docs/API.md` + `docs/COMO_RODAR.md` | Evidências |

---

## 1. Projeto e situação atual

### 1.1 O que é e como está organizado

Sistema B2B de recomendação de bebidas Fruki para lojistas. Stack: **Spring Boot 3.2.5 / Java 21**, **Supabase (PostgreSQL) via JPA**, **Databricks via JDBC somente leitura**, **Thymeleaf** para o catálogo, **JWT** no catálogo e **WhatsApp (Evolution API)** com bot de menu.

```
Navegador ──/catalogo?token──► CatalogoInterceptor (JWT) ──► catalogo.html (mock)
WhatsApp ──webhook──► WhatsAppBotService ──► RecomendacaoPerfilService ──► Databricks/Supabase
Mentor ──GET /api/v1/recomendacoes/{id}──► Controller ──► Databricks (Gold crua)
```

**Funciona comprovadamente:** subida da aplicação, conexão Supabase (`pg-pool`), Databricks (`databricks-pool`), `GET /recomendacoes/41|48|81` com 200, `/catalogo` sem token com 401, `/api/v1/contexto` e `/feriados`, bot com menu e identificação por CNPJ.

**Parcial:** o endpoint de recomendação devolve a entidade crua (sem nome, preço, posição ou motivos); `POST /pedidos` devolve `150.00` fixo e não grava; o recomendador por perfil existe só no WhatsApp e devolve texto; o catálogo é 100% mock; os testes são frágeis.

**Não existe:** `@RestControllerAdvice`, indicadores, ingestão de clientes/compras, scoring V2 com pesos, pipeline versionado, README e migrations.

### 1.2 Situação por área

| Área | Situação | O que falta | Evidência |
|---|---|---|---|
| Configuração Spring | Concluído | — | `PostgresConfig.java`, `DatabricksConfig.java`, `application.yml` |
| Segurança | Parcial | API aberta; JWT só no catálogo | `SecurityConfig.java:15-19`; `WebConfig.java:19-20` |
| Consulta de recomendação | Parcial | DTO, nome/preço/posição, 404, teste | `RecomendacaoController.java:26-29` |
| Recomendador | Parcial (só bot) | Score/ranking/motivos no endpoint | `RecomendacaoPerfilService.java:59-71` |
| Exceções | Parcial | Handler global 400/404/503 | `RecomendacaoController.java:32-37` |
| Pedidos | Implementado, precisa correção | Persistir e calcular total real | `PedidoController.java:18-29` |
| Ingestão de clientes/compras | Não implementado | Endpoints + validação | — |
| Indicadores | Não implementado | Endpoint + métricas | — |
| Catálogo | Implementado, precisa correção | Consumir API e identidade real | `catalogo.html:386-399`, `552-576` |
| Databricks (leitura) | Concluído | — | `RecomendacaoRepository.java:45-58` |
| Pipeline Databricks | Não implementado | Notebook/job versionado | `databricks/fruki_dados/` (só biblioteca) |
| Testes | Parcial | Testes do endpoint/regras; reduzir dependência de banco | `src/test/java/...` |
| Documentação | Não implementado | README, API, execução | só `HELP.md` |
| Cache | Não implementado | `@Cacheable` | `application.yml:18-19` |

### 1.3 Divergências entre o relatório antigo e o código atual

| O relatório dizia | O código atual mostra |
|---|---|
| 29 arquivos | 38 principais + 7 de teste |
| Senha do Supabase no `application.yml` | Variáveis de ambiente (`application.yml:22-24`); segredo vive em `.idea/workspace.xml` (não versionado) |
| Nenhum recomendador | `RecomendacaoPerfilService` existe, mas só no bot |
| Filtro de categoria quebrado | Corrigido (`catalogo.html:469-476`) |
| Segurança libera tudo | Continua `permitAll`, mas o catálogo tem JWT por interceptor |

**Não foi possível verificar:** schema real do Supabase, tabela Gold real e notebooks da Vitória.

### 1.4 Pré-requisitos para começar (Fase 0)

- JDK 21 e o Maven Wrapper (`.\mvnw.cmd`) funcionando.
- IntelliJ com a Run Configuration contendo as variáveis (`SUPABASE_*`, `JWT_SECRET`, `DATABRICKS_*`).
- `scripts/env.local.ps1` **fora do Git** (o `.gitignore` já cobre) para quem roda pelo terminal.
- Token do Databricks **rotacionado** (o antigo foi exposto em um relatório).
- Acesso ao Supabase e ao workspace Databricks.

Comandos de verificação (Windows/PowerShell):

```powershell
cd projeto-final-fruki
.\mvnw.cmd -B spring-boot:run
# em outra janela:
curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8080/api/v1/recomendacoes/41   # 200
curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8080/catalogo                  # 401
```

---

## 2. Pendências mapeadas

| # | Pendência | Requisito |
|---|---|---|
| P1 | Contrato de resposta + DTOs + posição/nome/preço | 1, 5 |
| P2 | `RecomendacaoService` (controller só orquestra) | 1, 8 |
| P3 | Handler global 400/404/503/500 | 7 |
| P4 | V2: pesos, normalização 0–100, diversidade, motivos, cold start | 2, 9 |
| P5 | Indicadores (cobertura, variedade, taxa) | 4 |
| P6 | `POST /clientes` com validação | 6, 3 |
| P7 | `POST /compras` + `PedidoService` real | 5, 6, 3 |
| P8 | Catálogo consumindo API + botão de pedido | 1, 8 |
| P9 | Testes do recomendador e da ingestão | 9 |
| P10 | README, API, execução, evidências | 10 |
| P11 | Pipeline Databricks versionado | 3, 8 |
| P12 | Integração, aceite e pitch | MVP |

---

## 3. Lista de tarefas

| ID | Tarefa | Requisito | Depende de | Bloqueia | Horas |
|---|---|---|---|---|---|
| T01 | Contrato + DTOs + exceções | 1, 5 | decisão C1–C3 | T02, T03, T08 | 4 |
| T02 | `RecomendacaoService` + controller fino | 1, 8 | T01 | T04, T09 | 6 |
| T03 | `GlobalExceptionHandler` + validação | 7 | T01 | T10 | 4 |
| T04 | Recomendador V2 | 2, 9 | T02 | T09 | 10 |
| T05 | Indicadores | 4 | — | T10 | 5 |
| T06 | Ingestão de clientes | 6, 3 | — | T07, T10 | 6 |
| T07 | Ingestão de compras + pedido real | 5, 6, 3 | T06 | T08, T10 | 8 |
| T08 | Catálogo consumindo a API | 1, 8 | T02, T07 | T13 | 6 |
| T09 | Testes do recomendador | 9 | T04 | T13 | 5 |
| T10 | Testes de ingestão/indicadores | 9 | T05, T06, T07 | T13 | 4 |
| T11 | Documentação + evidências | 10 | T02, T03 | T13 | 5 |
| T12 | Pipeline Databricks | 3, 8 | Vitória | T13 | 6 (bloqueada) |
| T13 | Integração e aceite | todos | T01–T12 | T14 | 4 |
| T14 | Pitch e demonstração | entrega | T13 | — | 2 |

Detalhamento completo nos cartões (seção 10).

---

## 4. Classificação por dependência

### Categoria A — independente e paralelizável
T03 (handler, arquivo novo), T05 (indicadores), T06 (clientes) e T11 (documentos em arquivos separados). Podem começar na Fase 1 sem esperar ninguém.

### Categoria B — paralelizável após contrato
T01/T02/T04 (contratos C1–C3), T07 (C4), T08 (C1 + C6), T09/T10 (contratos de teste). Exigem o contrato fechado antes de codar.

### Categoria C — sequencial
- T01 → T02 → T04 → T09.
- T06 → T07 → T08.
- T03 depende de T01 (classes de exceção).
- T11 depende de T02/T03 para documentar o comportamento real.

### Categoria D — trabalho conjunto
Fechar C1–C5 (T01), T13 (integração/aceite), T14 (pitch) e revisão de PRs.

---

## 5. Dependência de ambiente local

| Tarefa | Sem o ambiente da outra? | O que precisa | Como isolar |
|---|---|---|---|
| T01 | SIM | documento | Markdown no repositório |
| T02 | COM RESSALVAS | Supabase/Databricks para teste real | testes com Mockito; curl no fim |
| T03 | SIM | nada | `@WebMvcTest` com controller de teste |
| T04 | SIM | — | testes unitários puros |
| T05 | COM RESSALVAS | Databricks + Supabase | mock do repository nos testes |
| T06 | COM RESSALVAS | Supabase | mock do repository + teste manual |
| T07 | COM RESSALVAS | Supabase | idem |
| T08 | COM RESSALVAS | app rodando + JWT | começar com JSON estático; integrar no fim |
| T09/T10 | SIM | — | `mvnw test` |
| T11 | SIM | — | — |
| T12 | NÃO | workspace/notebooks da Vitória | bloqueada |
| T13/T14 | NÃO | as duas máquinas | sessão conjunta |

Regra: **não adicionar H2 nem banco em memória** apenas para testar — mudaria dependências e mascararia o schema real. Usem mocks.

---

## 6. Divisão de responsabilidades

### 6.1 Pessoa A

| ID | O que desenvolve | Por quê | Arquivos | Pré-requisito | Como testa |
|---|---|---|---|---|---|
| T01 | DTOs, exceções e contratos | é a base de tudo | `application/dtos/Recomendacao*`, `domain/exception/*` | decisão conjunta | compilar + revisão |
| T02 | `RecomendacaoService` + controller | tira a regra do controller | `RecomendacaoService.java`, `RecomendacaoController.java` | T01 | curl 200/404 |
| T04 | V2 (score, diversidade, motivos) | requisito 2 | `RecomendadorV2Service.java`, `application.yml` | T02 | unitários + tabela V1×V2 |
| T09 | testes do recomendador | requisito 9 | `src/test/.../Recomendacao*Test` | T04 | `mvnw test` |
| T12 | pipeline versionado | requisitos 3/8 | `databricks/notebooks`, `job.yml` | Vitória | job rodando |
| T11 | README, arquitetura e recomendador | requisito 10 | `README.md`, `docs/arquitetura.md` | T02 | outra pessoa executa |
| T13/T14 | integração, aceite e pitch | entrega | — | todas | checklist |

**Entrega para B:** exceções + DTOs no primeiro commit (contrato compilando), JSON de exemplo e o endpoint funcionando para o catálogo.

### 6.2 Pessoa B

| ID | O que desenvolve | Por quê | Arquivos | Pré-requisito | Como testa |
|---|---|---|---|---|---|
| T03 | handler global 400/404/503/500 | requisito 7 | `GlobalExceptionHandler.java` | T01 | `@WebMvcTest` isolado |
| T05 | indicadores | requisito 4 | `IndicadoresController/Service/Repository/DTO` | — | SQL + curl |
| T06 | `POST/GET /clientes` | requisito 6 | `ClienteController/Service`, `ClienteRequestDTO` | — | curl + Supabase |
| T07 | `POST /compras` + pedido real | requisitos 5/6 | `CompraController/Service`, `PedidoService`, `PedidoController` | T06 | curl + linha no banco |
| T08 | catálogo consumindo API | requisitos 1/8 | `catalogo.html`, `CatalogoController.java` | T02/T07 | navegador |
| T10 | testes de ingestão/indicadores | requisito 9 | `src/test/.../Cliente*`, `Indicadores*` | T05–T07 | `mvnw test` |
| T11 | `docs/API.md`, `docs/COMO_RODAR.md` | requisito 10 | docs | T03 | revisão |
| T13/T14 | integração, aceite e pitch | entrega | — | todas | checklist |

**Entrega para A:** handler pronto (para A remover o `@ExceptionHandler` local), contrato dos endpoints de ingestão e os números dos indicadores.

### 6.3 Responsabilidades compartilhadas

- **Decisões técnicas:** C1–C5, nomes de classes e status codes.
- **Integração:** T13 — sessão de 2 h com as duas máquinas.
- **Testes de fluxo:** catálogo → pedido → banco; recomendação → catálogo.
- **Revisão:** cada PR é revisado pela outra pessoa (mesmo que só leia e rode).
- **Aceite:** conferir a matriz da seção 12.

### 6.4 Equilíbrio

| Critério | Pessoa A | Pessoa B |
|---|---|---|
| Quantidade de tarefas | 7 | 8 |
| Horas estimadas | ~38 | ~40 |
| Complexidade | alta (V2) | alta (persistência + catálogo) |
| Conhecimentos | scoring, testes, documentação | JPA, validação, SQL, front-end |
| Dependências externas | Vitória (pipeline) | Fernando (schema) |
| Riscos principais | fórmula e escopo | schema e joins |

A assume o risco conceitual (V2); B assume o risco de integração com banco.

---

## 7. Contratos técnicos (fechar antes de codar)

> Esta seção é a fonte oficial até a T01. Se quiserem, copiem para `docs/contratos-api.md` depois.

### C1 — Consulta de recomendação
- **Dono:** A. **Concordam:** A e B.
- `GET /api/v1/recomendacoes/{idCliente}`
- 200:

```json
{
  "idCliente": 41,
  "nomeCliente": "Mercado Central",
  "itens": [
    {
      "posicao": 1,
      "codItem": "00001",
      "nome": "Fruki Guaraná 2L",
      "preco": 7.49,
      "score": 92.0,
      "motivos": ["alta frequência"]
    }
  ]
}
```

- 404 cliente inexistente; 503 Databricks; 400 id inválido. Todos em `ProblemDetail` com `type`, `title`, `status`, `detail` e `instance`.
- `score` é **0–100**. Na T02 fica `null` (sem fórmula falsa); a T04 preenche.
- **Não pode mudar sem avisar:** nomes dos campos e status codes.

### C2 — Exceções
- Pacote `org.example.projetofinalfruki.domain.exception`.
- `ClienteNaoEncontradoException(Long id)`, `ConflitoException(String)`, `DatabricksIndisponivelException(String, Throwable)` (movida do repository).
- **Não pode mudar:** nome completo da classe (FQN) e construtores.

### C3 — Service
- `RecomendacaoResponseDTO recomendar(Long idCliente)`.
- Lança as exceções de C2; não devolve entidade.

### C4 — Ingestão
- `POST /api/v1/clientes` → `{cnpj, nomeFantasia, cidade, uf}` → 201; 400; 409 duplicado.
- `GET /api/v1/clientes/{cnpj}` → 200/404.
- `POST /api/v1/compras` → `{cnpj, codItem, quantidade, dataCompra}` → 201; 400; 404 cliente.
- `POST /api/v1/pedidos` → 201 com `valorTotal` real (substitui o `150.00`).
- Validações: CNPJ com 14 dígitos e verificadores; `codItem` com 5 dígitos; quantidade > 0; data ≤ hoje.
- **Não pode mudar:** nomes dos campos.

### C5 — Indicadores
- `GET /api/v1/indicadores` → `{coberturaPercent, variedadeItens, taxaRecomendacaoPercent, clientesComRecomendacao, clientesTotal, itensDistintos}`.
- Fórmulas: cobertura = clientes com ao menos uma recomendação / clientes cadastrados; variedade = itens distintos recomendados; taxa = média de recomendações por cliente com recomendação.

### C6 — Catálogo
- `CatalogoController` passa `clienteId` do JWT ao template; o JavaScript consome C1; o botão de pedido consome C4.

**Como verificar aderência:** rodar `curl` e comparar com esta seção; o PR só é aprovado se o JSON bater.

---

## 8. Git sem conflito

- `main` = entrega; `develop` = integração; `feat/a-T02-service`, `feat/b-T06-clientes`.
- Commits pequenos: `feat(rec): adiciona RecomendacaoService`.
- PR para `develop`, revisado pela outra pessoa; **nunca** push direto em `main`.
- Sincronizar todo dia: `git pull --rebase origin develop`.
- Conflito: quem é dono do arquivo resolve; se for compartilhado, as duas olham juntas.

| Arquivo compartilhado/sensível | Dono | Regra |
|---|---|---|
| `pom.xml` | A | B pede antes |
| `application.yml` | A | B pede antes |
| `RecomendacaoRepository.java` | A | B não altera |
| `PedidoController.java` | B | A não altera |
| `catalogo.html` | B | A não altera |
| `README.md` | A | B envia texto |
| `.gitignore` | A | — |
| `docs/evidencias/` | cada uma na sua subpasta | não sobrescrever |

---

## 9. Fases

| Fase | Objetivo | Tarefas | Quem | Critério de saída |
|---|---|---|---|---|
| 0 (1 dia) | Contratos + ambiente | C1–C5, branches, rodar app | ambas | contrato commitado; ambas com 200 no 41 |
| 1 (3–4 dias) | Independentes | A: T01; B: T03, T05, T06 | A/B | DTOs compilam; handler/indicadores/clientes testados |
| 2 (4–5 dias) | Com contrato | A: T02, T04; B: T07 | A/B | endpoint rico; compra persistida |
| 3 (2 dias) | Integração | B: T08; A: apoio | ambas | catálogo mostra recomendação e envia pedido |
| 4 (2 dias) | Testes e docs | T09, T10, T11 | A/B | `mvnw test` verde; README |
| 5 (1 dia) | Aceite e pitch | T13, T14 | ambas | checklist 100% |

---

## 10. Cartões de tarefa

### T01 — Contrato + DTOs + exceções (Pessoa A)
- **Requisito:** 1, 5. **Depende:** decisão. **Bloqueia:** T02, T03, T08.
- **Objetivo:** criar o contrato de resposta e as classes que todas as partes vão usar.
- **Por que:** sem contrato, A e B inventam estruturas diferentes.
- **Conhecimentos:** DTO, `record`, exceção, JSON, status HTTP.
- **Arquivos:** `application/dtos/ItemRecomendadoDTO.java`, `RecomendacaoResponseDTO.java`, `domain/exception/*` e a seção 7 deste documento.
- **Passos:** 1) revisar C1–C3; 2) criar os `record`s; 3) criar as exceções; 4) compilar; 5) commitar **antes** de implementar lógica.
- **Aceite:** `.\mvnw.cmd -o -DskipTests compile` OK; B revisou o contrato.
- **Entrega para B:** nomes completos das classes e o JSON de exemplo.

### T02 — `RecomendacaoService` (Pessoa A)
- **Requisito:** 1, 8. **Depende:** T01.
- **Objetivo:** transformar as linhas da Gold em resposta com posição, nome, preço e motivos.
- **Conhecimentos:** `@Service`, injeção por construtor, `Optional`, stream, regra de negócio.
- **Arquivos:** `domain/service/RecomendacaoService.java`, `RecomendacaoController.java`.
- **Passos:** 1) receber a lista do repository; 2) se vazia, checar cliente e lançar 404 ou devolver vazio; 3) mapear posição/nome/preço; 4) controller só chama o service e remove o handler local.
- **Aceite:** 200 com nome/preço/posição; 404; 200 com lista vazia.
- **Entrega para B:** endpoint pronto para a T08.

### T03 — `GlobalExceptionHandler` (Pessoa B)
- **Requisito:** 7. **Depende:** T01.
- **Objetivo:** padronizar 400, 404, 503 e 500 com `ProblemDetail`.
- **Conhecimentos:** `@RestControllerAdvice`, `@ExceptionHandler`, `ProblemDetail`.
- **Arquivos:** `application/controller/GlobalExceptionHandler.java`.
- **Passos:** 1) 404 e 503; 2) 400 de validação e de tipo; 3) 500 com log; 4) testar com um controller de teste.
- **Aceite:** `/recomendacoes/abc` retorna 400 padronizado; 404 e 503 também.
- **Entrega para A:** handler pronto para remover o tratamento local.

### T04 — Recomendador V2 (Pessoa A)
- **Requisito:** 2, 9. **Depende:** T02.
- **Objetivo:** score explicável, com pesos, normalização, diversidade e motivos.
- **Conhecimentos:** normalização, pesos, ordenação, diversidade, explicação.
- **Arquivos:** `RecomendadorV2Service.java`, `application.yml` (pesos), `docs/recomendador.md`.
- **Passos:** 1) definir pesos; 2) normalizar 0–100; 3) penalidades; 4) diversidade (máx. 2 por categoria); 5) `motivos[]`; 6) cold start por região; 7) tabela V1×V2 para 41/48/81.
- **Aceite:** score explicável e teste unitário de cada regra.
- **Entrega para B:** tabela comparativa e fórmula documentada.

### T05 — Indicadores (Pessoa B)
- **Requisito:** 4. **Depende:** —.
- **Objetivo:** calcular cobertura, variedade e taxa.
- **Conhecimentos:** SQL agregado, JDBC, DTO.
- **Arquivos:** `IndicadoresController/Service/Repository`, `IndicadoresResponseDTO`.
- **Passos:** 1) escrever as três consultas; 2) service; 3) endpoint; 4) conferir com SQL manual.
- **Aceite:** 200 com números que batem com o Databricks.
- **Entrega para A:** contrato C5 preenchido.

### T06 — Ingestão de clientes (Pessoa B)
- **Requisito:** 6, 3. **Depende:** —.
- **Objetivo:** receber, validar e persistir um novo cliente.
- **Conhecimentos:** `@PostMapping`, Bean Validation, CNPJ, JPA `save`.
- **Arquivos:** `ClienteController/Service`, `ClienteRequestDTO`, `ClienteJpaRepository`.
- **Passos:** 1) DTO validado; 2) validador de CNPJ; 3) service (duplicado → 409); 4) POST e GET.
- **Aceite:** 201 + linha no Supabase; 400; 409.
- **Entrega para A:** C4 de clientes.

### T07 — Compras + pedido real (Pessoa B)
- **Requisito:** 5, 6, 3. **Depende:** T06.
- **Objetivo:** calcular o total com a tabela de preço e gravar em `fato_vendas`.
- **Conhecimentos:** transação, `BigDecimal`, cálculo de total, JPA.
- **Arquivos:** `CompraController/Service`, `CompraRequestDTO`, `PedidoService`, `PedidoController`, `PedidoResponseDTO`.
- **Passos:** 1) validar; 2) buscar preço; 3) calcular total; 4) salvar; 5) trocar o `150.00`.
- **Aceite:** 201 com total correto; linha no banco; testes.
- **Entrega para A:** C4 completo.

### T08 — Catálogo consumindo a API (Pessoa B)
- **Requisito:** 1, 8. **Depende:** T02, T07.
- **Objetivo:** trocar o mock pela recomendação real e ligar o botão de pedido.
- **Conhecimentos:** `fetch`, DOM, claim do JWT, Thymeleaf.
- **Arquivos:** `catalogo.html`, `CatalogoController.java`.
- **Passos:** 1) passar `clienteId`; 2) remover o mock; 3) `fetch` em C1; 4) filtro; 5) botão → POST.
- **Aceite:** recomendação real na tela; pedido criado.
- **Entrega para A:** evidência em vídeo ou print.

### T09 — Testes do recomendador (Pessoa A)
- **Requisito:** 9. **Depende:** T04.
- **Arquivos:** `RecomendacaoServiceTest`, `RecomendadorV2ServiceTest`, `RecomendacaoControllerTest`.
- **Aceite:** casos 200/404/503/400, pesos, diversidade e cold start.
- **Atenção:** o `@WebMvcTest` precisa de `@MockBean JwtService` por causa do `CatalogoInterceptor`.

### T10 — Testes de ingestão/indicadores (Pessoa B)
- **Requisito:** 9. **Depende:** T05–T07.
- **Aceite:** CNPJ válido/inválido, duplicado, quantidade, preço e fórmulas.

### T11 — Documentação (A + B)
- **Requisito:** 10.
- A: `README.md`, `docs/arquitetura.md`, `docs/recomendador.md`.
- B: `docs/API.md`, `docs/COMO_RODAR.md`.
- **Aceite:** outra pessoa executa seguindo o documento.

### T12 — Pipeline Databricks (Pessoa A, bloqueada)
- **Requisito:** 3, 8. **Depende:** Vitória.
- **Entrega:** notebook + `job.yml` + SQL da Gold versionados.
- **Enquanto isso:** documentar o contrato de `fato_vendas` em `docs/pipeline.md`.

### T13 — Integração e aceite (ambas)
- Rodar a matriz da seção 12, corrigir incompatibilidades e registrar evidências.

### T14 — Pitch e demonstração (ambas)
- Problema → solução → demonstração → resultados → limitações.

---

## 11. Prompt para a colega (copiar e colar)

```text
Você é meu mentor técnico em Java/Spring Boot. Sou iniciante. Projeto: Pede Fruki
(Spring Boot 3.2.5, Java 21, Supabase/JPA, Databricks/JDBC, Thymeleaf).
Repositório: https://github.com/vitoria-moraes/Projeto-final-Fruki
Código em: projeto-final-fruki/

ESTADO ATUAL: a aplicação sobe; GET /api/v1/recomendacoes/41 retorna 200 com dados
crus da Gold; POST /pedidos é fake (150.00); não há handler global, indicadores,
ingestão, V2 nem README.

MINHA TAREFA ATUAL: [PREENCHER: T03, T05, T06, T07, T08 ou T10]
Leia o cartão da minha tarefa na seção 10 de docs/PLANEJAMENTO.md.

CONTRATOS QUE NÃO PODEM SER ALTERADOS SEM AUTORIZAÇÃO:
- GET /api/v1/recomendacoes/{idCliente} -> 200 {idCliente, nomeCliente, itens:[
  {posicao, codItem, nome, preco, score, motivos}]}; 404/503/400 em ProblemDetail.
- Exceções em org.example.projetofinalfruki.domain.exception:
  ClienteNaoEncontradoException, ConflitoException, DatabricksIndisponivelException.
- POST /api/v1/clientes {cnpj, nomeFantasia, cidade, uf} -> 201/400/409.
- POST /api/v1/compras {cnpj, codItem, quantidade, dataCompra} -> 201/400/404.
- GET /api/v1/indicadores -> {coberturaPercent, variedadeItens,
  taxaRecomendacaoPercent, clientesComRecomendacao, clientesTotal, itensDistintos}.

ARQUIVOS QUE EU POSSO ALTERAR: [PREENCHER conforme a tarefa]
ARQUIVOS QUE NÃO POSSO ALTERAR (são da Pessoa A): RecomendacaoController.java,
RecomendacaoService.java, RecomendacaoRepository.java, application.yml, pom.xml,
README.md, src/test/.../Recomendacao*.

REGRAS PARA VOCÊ, IA:
1. Explique o conceito antes de mostrar código.
2. Use linguagem simples; não presuma Java avançado.
3. Explique cada classe, método, anotação e dependência.
4. Divida em passos pequenos; um por vez.
5. Não entregue uma solução gigante de uma vez.
6. Não mude a arquitetura, nomes, campos ou endpoints.
7. Não mande alterar arquivos da Pessoa A.
8. Sempre diga como testar (comando, URL, JSON, resultado esperado).
9. Se eu errar, leia a mensagem e explique a causa; não substitua tudo.
10. Ao final, gere um resumo técnico para a integração.

MEU AMBIENTE: Windows 11, JDK 21, .\mvnw.cmd, IntelliJ, PowerShell.
SEGREDOS: nunca me peça token/senha; uso scripts/env.local.ps1 (fora do Git).

QUERO APRENDER, NÃO SÓ COPIAR. Ao final de cada etapa me pergunte:
o que essa classe faz, por que existe, como se comunica e como provo que funciona.
```

---

## 12. Integração e rastreabilidade

### Checklist de integração
- [ ] DTOs e nomes iguais ao contrato
- [ ] Assinaturas do service iguais
- [ ] Status codes iguais (200/201/400/404/409/503)
- [ ] Formato `ProblemDetail` igual
- [ ] Ingestão grava e a recomendação lê
- [ ] Catálogo consome C1 e envia C4
- [ ] `mvnw test` verde
- [ ] App sobe do zero em máquina limpa
- [ ] Nenhum segredo no `git diff`

### Matriz de rastreabilidade

| Requisito | Tarefas | Responsável | Evidência | Hoje |
|---|---|---|---|---|
| 1 Consulta | T01, T02, T08 | A/B | curl 200 com nome/preço | Parcial |
| 2 V2 | T04, T09 | A | tabela V1×V2 | Não implementado |
| 3 Novos dados | T06, T07, T12 | B/A | POST + linha no banco | Não implementado |
| 4 Indicadores | T05, T10 | B | `/indicadores` | Não implementado |
| 5 Java V2 | T02, T07 | A/B | pedido persistido | Parcial |
| 6 Ingestão | T06, T07 | B | clientes/compras | Não implementado |
| 7 Exceções | T03, T09 | B/A | 400/404/503 | Parcial |
| 8 Integração | T08, T12, T13 | ambas | fluxo ponta a ponta | Parcial |
| 9 Validação | T04, T09, T10 | A/B | testes + análise | Não implementado |
| 10 Pendências | T11 | A/B | README/API | Não implementado |
| MVP/pitch | T13, T14 | ambas | demonstração | Não implementado |

---

## 13. Checklist de entrega final

- [ ] App sobe com `.\mvnw.cmd spring-boot:run`
- [ ] `GET /recomendacoes/41` devolve o contrato C1
- [ ] 404/400/503 padronizados
- [ ] `POST /clientes` e `/compras` persistem
- [ ] `POST /pedidos` calcula total real
- [ ] `/indicadores` responde
- [ ] Catálogo sem mock
- [ ] `mvnw test` verde
- [ ] README + API + COMO_RODAR
- [ ] Evidências em `docs/evidencias/`
- [ ] Nenhum segredo versionado
- [ ] Demonstração de 5–8 min ensaiada

---

## 14. Riscos e decisões conjuntas

| Risco | Impacto | Mitigação |
|---|---|---|
| `id_cliente` × `cod_cliente` | nome do cliente não casa | tratar `Optional`; documentar; corrigir na T06/T07 |
| `cod_item` × `codProduto` | preço nulo | fallback e teste |
| Token/senha expostos | segurança | rotacionar; `env.local.ps1`; nunca no YAML |
| Sem cache / cold start | demonstração lenta | avisar no pitch; ajustar `connection-timeout` |
| Testes exigem banco | CI frágil | mocks; `@WebMvcTest` |
| Pipeline bloqueado | requisitos 3/8 incompletos | cobrar Vitória; documentar |
| Escopo de última sprint | atraso | T12 é a única bloqueada; o resto é executável |
| Duas iniciantes | retrabalho | contratos + PRs pequenos + sessões de 30 min |

**Decisões que precisam ser tomadas juntas:**

1. O `score` na T02 fica `null` até a T04?
2. O 404 vale só para cliente inexistente na base, ou também para "sem recomendação"?
3. As imagens locais no catálogo entram no MVP ou ficam para depois?
4. Quem fala com Vitória (notebooks) e Fernando (schema)?

### Acordo de trabalho sugerido
- Reunião diária de 15 min (o que fez, o que fará, o que travou).
- PR revisado em até 24 h.
- Sessão conjunta de 1 h por semana para integração.
- Ninguém altera arquivo do outro sem avisar no chat.

---

## Glossário rápido

| Termo | O que é | Analogia em C#/.NET |
|---|---|---|
| Controller | recebe HTTP e devolve resposta | Controller da Web API |
| Service | regra de negócio | camada de serviços |
| Repository | acesso ao banco | `DbContext` / repositório EF |
| Entity | classe que representa uma tabela | entidade do EF |
| DTO | objeto só para transportar dados | ViewModel |
| `ProblemDetail` | padrão de erro HTTP | `ProblemDetails` |
| JPA | mapeamento objeto-relacional | Entity Framework |
| JDBC | SQL manual | Dapper / ADO.NET |
| Mock | dublê de teste | Moq |
| PR | pedido de merge | pull request |

---

## Próxima ação recomendada

Reunião de **60–90 minutos** entre as duas, nesta ordem:

1. **Rotacionar o token do Databricks** e confirmar que `scripts/env.local.ps1` está fora do Git.
2. **Fechar os contratos C1–C5** (seção 7). Nada de código antes disso.
3. **A Pessoa A cria e commita só o esqueleto da T01** (DTOs + exceções, sem lógica) no mesmo dia — isso desbloqueia a B.
4. **Cada uma cria sua branch** (`feat/a-T01-contrato`, `feat/b-T05-indicadores`) e roda a app até ver `200` em `/recomendacoes/41`; salva a evidência.

Se quiserem, o próximo passo é transformar a seção 7 em `docs/contratos-api.md` e revisar junto com o mentor antes de liberar a implementação.

---

## 15. Relatório de execução (2026-11-05)

> A “Próxima ação recomendada” da seção anterior foi executada. O que **ainda falta**
> está na seção 15.5 — não marque como concluído o que não está.

### 15.1 Status das tarefas

| ID | Tarefa | Status | Entrega principal |
|---|---|---|---|
| T01 | Contrato + DTOs + exceções | ✅ Concluído | DTOs, exceções e `docs/API.md` |
| T02 | `RecomendacaoService` + controller fino | ✅ Concluído | serviço + controller |
| T03 | `GlobalExceptionHandler` + validação | ✅ Concluído | 400/404/405/409/503/500 |
| T04 | Recomendador V2 | ✅ Concluído | score, diversidade, motivos, cold start |
| T05 | Indicadores | ✅ Concluído | `/api/v1/indicadores` |
| T06 | Ingestão de clientes | ✅ Concluído | `POST/GET /api/v1/clientes` |
| T07 | Compras + pedido real | ✅ Concluído | `POST /compras`, `POST /pedidos` |
| T08 | Catálogo consumindo a API | 🟡 Parcial | demo + guia + meta tags; `catalogo.html` ainda mock |
| T09 | Testes do recomendador | ✅ Concluído | 3 classes de teste |
| T10 | Testes de ingestão/indicadores | ✅ Concluído | clientes, compras, pedido, indicadores e contexto |
| T11 | Documentação + evidências | ✅ Concluído | README + docs de API, recomendador, front e schema |
| T12 | Pipeline Databricks | ⛔ Bloqueado | depende da Vitória (notebooks/schema) |
| T13 | Integração e aceite | 🟡 Parcial | 41 testes verdes; runtime real pendente |
| T14 | Pitch e demonstração | ⬜ Não iniciado | — |

### 15.2 O que foi entregue, por tarefa

### T01 — Contrato + DTOs + exceções ✅
- **O que foi feito:** `RecomendacaoResponseDTO`, `ItemRecomendadoDTO`, `ContextoDTO`,
  `ProdutoDTO`, `ClienteRequest/ResponseDTO`, `CompraRequest/ResponseDTO`,
  `IndicadoresResponseDTO`, `EstacaoDTO`; exceções `ClienteNaoEncontradoException`,
  `ProdutoNaoEncontradoException`, `ConflitoException`,
  `DatabricksIndisponivelException`; contrato em `docs/API.md`.
- **Por que:** a API deixou de devolver entidade crua; o front e os testes passaram a
  depender de um contrato estável.
- **Como testar:** `.\mvnw.cmd test` (os testes de contrato usam os DTOs).

### T02 — `RecomendacaoService` + controller fino ✅
- **O que foi feito:** `RecomendacaoService` orquestra Gold + cliente + contexto +
  ranking; `RecomendacaoController` só delega; `limite` validado (1..50); 404 quando
  não há cliente nem histórico; 503 quando o Databricks cai.
- **Por que:** separa HTTP de regra de negócio e permite testar a regra sem subir a web.
- **Como testar:** `GET /api/v1/recomendacoes/41` e `GET /api/v1/recomendacoes/999999`.

### T03 — `GlobalExceptionHandler` ✅
- **O que foi feito:** tratamento único com `ProblemDetail` para 400 (validação, JSON,
  tipo), 404 (cliente/produto/rota), 405, 409 (CNPJ duplicado), 503 (Databricks e banco
  transacional) e 500 (logado, sem vazar stack trace).
- **Por que:** os erros deixaram de ser o JSON genérico do Spring e passaram a ter
  `type`, `title`, `detail`, `instance` e `erros[]`.
- **Como testar:** `GET /api/v1/recomendacoes/abc` (400), `/999999` (404),
  `POST /api/v1/clientes` com CNPJ repetido (409).

### T04 — Recomendador V2 ✅
- **O que foi feito:** `RecomendadorV2Service` com RFM (50%), regional (20%),
  sazonalidade (15%) e margem/giro (15%), penalidades (compra recente, preço outlier,
  dado incompleto), diversidade (2 por categoria), cold start e `motivos[]`/`detalhe`.
  Pesos em `app.recomendacao.*` (`RecomendacaoProperties`).
- **Por que:** entrega o requisito 2 com explicação do score e comparabilidade V1×V2.
- **Como testar:** `RecomendadorV2ServiceTest` e
  `GET /api/v1/recomendacoes/41` (confira `score`, `motivos`, `detalhe`).

### T05 — Indicadores ✅
- **O que foi feito:** `IndicadoresRepository` (SQL agregado na Gold),
  `IndicadoresService`, `GET /api/v1/indicadores` com cobertura, variedade, taxa
  (clientes com 3+ itens) e média por cliente.
- **Por que:** requisito 4; dá número para a apresentação.
- **Como testar:** `GET /api/v1/indicadores` e `IndicadoresServiceTest`.

### T06 — Ingestão de clientes ✅
- **O que foi feito:** `POST /api/v1/clientes` (CNPJ com dígitos verificadores,
  normalização, 409 para duplicado) e `GET /api/v1/clientes/{cnpj}`.
- **Por que:** requisito 6; o cliente novo fica disponível para a recomendação.
- **Como testar:** `POST` com `11.222.333/0001-81` e conferir a linha no Supabase.

### T07 — Compras + pedido real ✅
- **O que foi feito:** `CompraService` (item, quantidade, data não futura, preço da
  tabela) e `PedidoService` (vários itens, total real, persistência em `fato_vendas`);
  `PedidoController` deixou de devolver `150.00` fixo.
- **Por que:** requisito 5 e 6; o dado entra e fica disponível.
- **Como testar:** `POST /api/v1/compras` e `POST /api/v1/pedidos`; conferir a linha em
  `fato_vendas`.

### T08 — Catálogo consumindo a API 🟡
- **O que foi feito:** página `demo-recomendacoes.html` (consome recomendação, contexto
  e indicadores), meta tags `cliente-id`/`cnpj-cliente` no `catalogo.html`, CNPJ no JWT
  e guia `docs/INTEGRACAO_FRONT.md` com os trechos de `fetch`, carrinho e `POST /pedidos`.
- **O que falta:** trocar o array mock do `catalogo.html` pela API e ligar o botão de
  pedido. O guia está pronto; a vitrine em si continua mock.
- **Como testar:** abrir `/demo-recomendacoes.html` e seguir o checklist do guia.

### T09/T10 — Testes ✅
- **O que foi feito:** `RecomendadorV2ServiceTest`, `RecomendacaoServiceTest`,
  `RecomendacaoControllerTest`, `PedidoControllerTest`, `PedidoServiceTest`,
  `CompraServiceTest`, `ClienteServiceTest`, `IndicadoresServiceTest`,
  `ContextoServiceTest`, `EstacaoTest`, `MotorSazonalidadeTest`, `CnpjValidatorTest`,
  `RecomendacaoPropertiesTest`.
- **Por que:** requisito 9; valida score, diversidade, cold start, 200/404/503/400 e a
  ingestão sem depender do banco.
- **Como testar:** `.\mvnw.cmd test` — **41 testes, 0 falhas, 4 ignorados**.

### T11 — Documentação ✅
- **O que foi feito:** `README.md` (com a seção “Como executar”, que cobre o antigo
  `COMO_RODAR`), `docs/API.md`, `docs/RECOMENDADOR.md`,
  `docs/INTEGRACAO_FRONT.md`, `docs/SCHEMA.md`, `docs/evidencias/testes.md`.
- **Por que:** requisito 10; outra pessoa consegue executar e entender.
- **Como testar:** seguir o README em uma máquina limpa.

### T12 — Pipeline Databricks ⛔
- **O que foi feito:** nada de notebook/job; apenas o consumo da Gold pelo Java.
- **Por que está bloqueado:** depende dos notebooks/SQL e do schema real que a Vitória
  precisa fornecer; não há contrato de `fato_vendas` no pacote Python nem `job.yml`.
- **O que fazer:** cobrar Vitória/Fernando e versionar notebook + job (seção 7 do plano).

### T13 — Integração e aceite 🟡
- **O que foi feito:** código integrado e 41 testes verdes; contrato web validado com
  `@WebMvcTest`.
- **O que falta:** rodar contra Supabase/Databricks reais (o agente não tinha
  credenciais); os 5 testes de integração ficaram ignorados.
- **Como fechar:** `. .\scripts\env.local.ps1; .\mvnw.cmd test` e os `curl` da seção 15.4.

### T14 — Pitch ⬜
- Não iniciado. Falta roteiro de 5–8 min e ensaio.

### 15.3 Requisitos — situação após a implementação

| # | Requisito | Status | Evidência |
|---|---|---|---|
| 1 | Consulta da recomendação | ✅ | `GET /recomendacoes/{id}` com DTO, score e motivos |
| 2 | Recomendador V2 | ✅ | `RecomendadorV2Service` + `docs/RECOMENDADOR.md` |
| 3 | Tratamento de novos dados | 🟡 | validação no Java pronta; pipeline bloqueado (T12) |
| 4 | Indicadores | ✅ | `GET /api/v1/indicadores` |
| 5 | Aplicação Java V2 | ✅ | pedido real + persistência |
| 6 | Ingestão consolidada | ✅ | `POST /clientes` e `POST /compras` |
| 7 | Regras e exceções | ✅ | `GlobalExceptionHandler` |
| 8 | Integração completa | 🟡 | REST/bot/contexto prontos; catálogo mock e pipeline pendentes |
| 9 | Validação da recomendação | 🟡 | testes + análise em `RECOMENDADOR.md`; falta análise com dados reais |
| 10 | Pendências finais | ✅ | README, API, schema e evidências (sem Flyway) |
| MVP/pitch | Demonstração | 🟡 | demo técnica pronta; pitch não |

### 15.4 Como validar (ordem recomendada)

```powershell
# 1. testes (não precisa de banco)
cd projeto-final-fruki
.\mvnw.cmd test          # 41 testes, 0 falhas, 4 ignorados

# 2. subir com as variáveis
. .\scripts\env.local.ps1
.\mvnw.cmd -B spring-boot:run

# 3. em outra janela
curl.exe -s "http://localhost:8080/api/v1/recomendacoes/41" | ConvertFrom-Json | ConvertTo-Json -Depth 6
curl.exe -s "http://localhost:8080/api/v1/indicadores"
curl.exe -s "http://localhost:8080/api/v1/contexto?cidade=Gramado&uf=RS"
curl.exe -s "http://localhost:8080/api/v1/contexto/estacao?data=2026-12-01"
curl.exe -s "http://localhost:8080/api/v1/contexto/feriados?ano=2026&cidade=Gramado"

# 4. demo no navegador
start http://localhost:8080/demo-recomendacoes.html
```

**Aceite:** 200 no `/recomendacoes/41` (ou `404` se o cliente não existir), `score`
entre 0 e 100, no máximo 2 itens por categoria, catálogo 401 sem token.

### 15.5 O que ainda NÃO está concluído

1. **T08 — catálogo real:** `catalogo.html` continua mock. Use
   `docs/INTEGRACAO_FRONT.md` para trocar (fetch, carrinho por `codItem`,
   `POST /pedidos`).
2. **T12 — pipeline Databricks:** sem notebook/job/`fato_vendas` versionado; bloqueado
   pela Vitória.
3. **T13 — runtime real:** não validei contra Supabase/Databricks (sem credenciais).
   Os 5 testes de integração estão ignorados até você rodar com `env.local.ps1`.
4. **T14 — pitch:** não iniciado.
5. **Autenticação da API** e **cache** não estavam no plano e continuam pendentes.
6. **Migrations:** não há Flyway; o schema está documentado em `docs/SCHEMA.md`.

### 15.6 Se algo der errado (troubleshooting)

| Sintoma | Causa provável | Onde olhar | Comando/consulta |
|---|---|---|---|
| App não sobe / `APPLICATION FAILED` | variável ausente, `JWT_SECRET` curto, banco fora | `README.md` seção 2; `scripts/env.example.ps1` | `. .\scripts\env.local.ps1` |
| 503 em `/recomendacoes` | Databricks: host, token, warehouse, tabela ou coluna | `RecomendacaoRepository` (agora loga a causa), `application.yml` | ver log `Caused by`; testar SQL no Databricks |
| 404 em `/recomendacoes/41` | cliente não existe na base **e** Gold vazia | `RecomendacaoService` | `select * from cliente where cod_cliente=41;` |
| `preco: null` | `cod_item` ≠ `cod_produto` | `CatalogoService`, `docs/SCHEMA.md` | `select * from tabela_preco where cod_produto like '%1%';` |
| 409 em `/clientes` | CNPJ já cadastrado | `ClienteService` | consultar `cliente` por CNPJ |
| 400 com `erros[]` | validação | corpo da resposta; `GlobalExceptionHandler` | conferir campo/mensagem |
| 500/503 em `/clientes` ou `/compras` | tabela/coluna divergente | `docs/SCHEMA.md` | `information_schema.columns` |
| Clima sempre `FALLBACK` | chave OpenWeather ausente/inválida | `OpenWeatherService` | `OPENWEATHER_KEY` |
| Feriados vazios/lentos | BrasilAPI fora | `FeriadoService` (cache/fallback) | log `BrasilAPI indisponível` |
| Front não mostra recomendação | JWT/meta tags/console | `docs/INTEGRACAO_FRONT.md`; demo | abrir DevTools → Network |
| Testes ignorados | sem `SUPABASE_JDBC_URL`/`JWT_SECRET` | `@EnabledIfEnvironmentVariable` | rodar com `env.local.ps1` |
| Nome/cidade `null` | `id_cliente` ≠ `cod_cliente` | `docs/SCHEMA.md` | conferir as duas bases |

**Palavras-chave para pesquisar no código:** `RecomendadorV2Service`,
`RecomendacaoService`, `GlobalExceptionHandler`, `CatalogoService`,
`ContextoService`, `FeriadoService`, `OpenWeatherService`, `IndicadoresRepository`,
`RecomendacaoProperties`, `CnpjValidator`.

### 15.7 Decisões técnicas (e o porquê)

- **DTO em vez de entidade:** contrato estável e sem vazar detalhes do banco.
- **Service em vez de regra no controller:** testável e reutilizável (bot + API).
- **Heurística de categoria/perfil:** o schema não tem categoria nem segmento; a
  inferência está isolada em `CategoriaProduto`/`PerfilRegional` para trocar quando o
  dado existir.
- **Margem proxy:** não há margem real; o valor por categoria é explícito e documentado.
- **Fallback de clima/feriado:** a demo não pode quebrar se a API externa cair.
- **Testes de integração condicionais:** `mvn test` passa em qualquer máquina; quem tem
  credenciais roda tudo.
- **Sem dependências novas:** o build continua offline-friendly.

### 15.8 Evidências

- Compilação: **72 fontes principais + 17 de teste**.
- Testes: **41 executados, 0 falhas, 0 erros, 4 ignorados**.
- Registro: `docs/evidencias/testes.md`.
- Execução anterior da app: `docs/evidencias/etapa5-6/README.md`.

### 15.9 Manutenção deste documento

Quando T08, T12, T13 ou T14 avançarem, atualize a tabela 15.1 e a seção 15.5. Não
marque como concluído sem evidência (teste, `curl` ou print).

