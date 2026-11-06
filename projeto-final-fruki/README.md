# Pede Fruki — Backend de recomendação B2B

Sistema de recomendação personalizada de bebidas Fruki para lojistas (estabelecimentos B2B).
O backend expõe uma API REST, consulta a camada analítica (Databricks/Gold) e a base
transacional (Supabase/PostgreSQL), aplica um **recomendador híbrido e explicável** e
devolve um ranking pronto para o catálogo.

> Documentação complementar: [`docs/API.md`](docs/API.md) ·
> [`docs/RECOMENDADOR.md`](docs/RECOMENDADOR.md) ·
> [`docs/INTEGRACAO_FRONT.md`](docs/INTEGRACAO_FRONT.md) ·
> [`docs/SCHEMA.md`](docs/SCHEMA.md) ·
> [`docs/MASSA_DE_TESTE.md`](docs/MASSA_DE_TESTE.md) ·
> [`docs/GUIA_PAINEL.md`](docs/GUIA_PAINEL.md) ·
> [`docs/PLANEJAMENTO.md`](docs/PLANEJAMENTO.md)

---

## 1. Arquitetura

```
Navegador/Postman
      │
      ▼
Spring Boot 3.2.5 (Java 21)
 ├─ Controllers .......... recebem HTTP e devolvem DTOs
 ├─ Services ............. regra de negócio (recomendação, ingestão, indicadores, contexto)
 ├─ RecomendadorV2 ...... score híbrido + diversidade + explicação
 ├─ Repositories
 │   ├─ JPA .............. Supabase (cliente, tabela_preco, fato_vendas)
 │   └─ JDBC ............. Databricks (Gold de recomendações, somente leitura)
 └─ Integrações externas . OpenWeather (clima) e BrasilAPI (feriados), com cache/fallback
      │
      ▼
Supabase (transacional)        Databricks (analítico)
```

### Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Validation, Security) |
| Banco transacional | PostgreSQL (Supabase) via Hibernate/JPA |
| Camada analítica | Databricks via JDBC (`databricks-jdbc 2.6.36`) |
| Front | Thymeleaf + HTML/JS (catálogo) |
| Testes | JUnit 5, Mockito, MockMvc |
| Build | Maven Wrapper (`mvnw`) |

---

## 2. Como executar

### 2.1 Pré-requisitos

- JDK 21
- Acesso ao Supabase e ao Databricks (variáveis de ambiente)
- Windows/PowerShell, Linux ou macOS

### 2.2 Variáveis de ambiente

Copie o template e preencha **sem commitar**:

```powershell
Copy-Item .\scripts\env.example.ps1 .\scripts\env.local.ps1
notepad .\scripts\env.local.ps1
. .\scripts\env.local.ps1      # carrega e valida
```

| Variável | Para que serve |
|---|---|
| `SUPABASE_JDBC_URL` | URL JDBC do PostgreSQL |
| `SUPABASE_USER` / `SUPABASE_PASSWORD` | credenciais |
| `JWT_SECRET` | assinatura do link do catálogo (mínimo 32 caracteres) |
| `DATABRICKS_HOST` | host do workspace (sem `https://` e sem `:443`) |
| `DATABRICKS_HTTP_PATH` | `/sql/1.0/warehouses/<id>` |
| `DATABRICKS_TOKEN` | token do Databricks |
| `DATABRICKS_RECOMENDACAO_TABLE` | catálogo.schema.tabela da Gold |
| `OPENWEATHER_KEY` | opcional; sem ela o clima usa climatologia |
| `EVOLUTION_*` | opcional; envio real de WhatsApp |

> Nunca coloque token/senha no `application.yml`. O `.gitignore` cobre
> `scripts/env.local.ps1`.

### 2.3 Subir a aplicação

```powershell
cd projeto-final-fruki
.\mvnw.cmd -B spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### 2.4 Se estiver no IntelliJ

`Run` → `Edit Configurations` → `ProjetoFinalFrukiApplication` → **Environment variables**
e cadastre as variáveis acima. O botão ▶ usa essa configuração (persistente) e evita
perder variáveis a cada terminal novo.

### 2.5 Demonstração no navegador

- **Painel de testes (recomendado):** **http://localhost:8080/painel.html** — testa
  recomendação, contexto, indicadores, catálogo, clientes, compras, pedido, WhatsApp e
  erros em uma única página. Guia em [`docs/GUIA_PAINEL.md`](docs/GUIA_PAINEL.md).
- **Demo simples:** **http://localhost:8080/demo-recomendacoes.html** — mostra só a
  recomendação, o contexto e os indicadores.

As duas páginas consomem a API real e servem de referência para integrar o catálogo.

---

## 3. Endpoints

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/v1/recomendacoes/{idCliente}?limite=10` | Ranking V2 explicado |
| GET | `/api/v1/produtos?limite=50` | Catálogo da tabela de preços |
| GET | `/api/v1/produtos/{codItem}` | Um produto |
| GET | `/api/v1/indicadores` | Cobertura, variedade, taxa |
| GET | `/api/v1/contexto?cidade=...&uf=RS` | Clima + estação + feriados |
| GET | `/api/v1/contexto/estacao?data=2026-12-01` | Estação do ano (Hemisfério Sul) |
| GET | `/api/v1/contexto/feriados?ano=2026&cidade=...` | Feriados/eventos do Sul |
| GET | `/api/v1/contexto/mensagem?cidade=...` | Texto sazonal (bot) |
| POST | `/api/v1/clientes` | Cadastra lojista (CNPJ validado) |
| GET | `/api/v1/clientes/{cnpj}` | Consulta lojista |
| POST | `/api/v1/compras` | Registra compra item a item |
| POST | `/api/v1/pedidos` | Pedido real com total calculado |
| POST | `/api/v1/webhook/whatsapp` | Webhook da Evolution API |
| POST | `/mock/whatsapp/enviar-mensagem` | Simula o bot sem WhatsApp |
| GET | `/catalogo?token=...` | Catálogo web (JWT) |

Exemplos completos em [`docs/API.md`](docs/API.md).

---

## 4. Recomendação V2 (resumo)

```
score = 100 * (0,50·RFM + 0,20·regional + 0,15·sazonalidade + 0,15·margem/giro)
        − penalidades (compra recente, preço outlier, dado incompleto)
```

- **RFM** (50%): recência, frequência, valor e quantidade, normalizados 0..1.
- **Regional** (20%): quatro perfis (Mercado Central, Super Litoral, Conveniência Serra,
  Distribuidora Vale) inferidos pela cidade/UF.
- **Sazonalidade** (15%): estação do ano + temperatura + feriado/evento próximo.
- **Margem/giro** (15%): proxy por categoria (até existir margem real).
- **Diversidade:** no máximo 2 itens por categoria.
- **Cold start:** sem histórico, ranqueia o catálogo pelo perfil regional/estação.
- **Explicação:** cada item traz `motivos[]` e `detalhe` com a contribuição de cada fator.

Detalhes, pesos configuráveis e limitações em [`docs/RECOMENDADOR.md`](docs/RECOMENDADOR.md).

---

## 5. Modelo de dados

| Tabela | Uso |
|---|---|
| `cliente` | cadastro do lojista (`cod_cliente` bigint PK, `cnpj`, `nome_fantasia`, `cidade`, `uf`) |
| `tabela_preco` | catálogo/preço (`id` PK, `cod_produto`, `nome_produto`, `preco_base`) |
| `fato_vendas` | compras/pedidos (`id` PK, `cod_cliente` texto, `data_pedido`, `valor_total`, `status`, `resumo_itens`) |
| Gold (Databricks) | `id_cliente`, `cod_item`, `total_quantidade_comprada`, `valor_total_gasto`, `ultima_data_compra`, `frequencia_pedidos`, `score_recomendacao` |

> O schema real pode variar. Antes de rodar a ingestão, confira
> [`docs/SCHEMA.md`](docs/SCHEMA.md) e valide as colunas no Supabase.

---

## 6. Testes

```powershell
.\mvnw.cmd test
```

- **41 testes**, 0 falhas, 4 ignorados (integração que exige Supabase/JWT_SECRET).
- Cobertura: recomendador (score, diversidade, penalidade, cold start), serviço de
  recomendação (200/404/503), controllers (`@WebMvcTest`), CNPJ, estação, contexto,
  ingestão, indicadores e o motor sazonal.

Resultados e como repetir em [`docs/evidencias/testes.md`](docs/evidencias/testes.md).

---

## 7. Estrutura

```
src/main/java/org/example/projetofinalfruki
├── application
│   ├── controller   (REST + handler global + catálogo/whatsapp)
│   └── dtos         (contratos de entrada/saída)
├── config           (RecomendacaoProperties, SecurityConfig, DataSeeder)
├── domain
│   ├── exception    (404, 409, 503, produto)
│   ├── model        (Categoria, PerfilRegional, Estação, RecomendacaoB2B)
│   ├── service      (recomendador V2, recomendação, catálogo, ingestão, indicadores, contexto)
│   └── util         (CNPJ, texto)
└── infrastructure
    ├── config       (Postgres, Databricks, Web)
    ├── db           (JPA + JDBC/Databricks)
    ├── external     (OpenWeather)
    ├── messaging    (WhatsApp)
    └── security     (JWT)
```

---

## 8. Limitações conhecidas

1. O schema do Supabase não é versionado por migrations; ver [`docs/SCHEMA.md`](docs/SCHEMA.md).
2. `id_cliente` (Gold) e `cod_cliente` (Supabase) podem não coincidir; o enriquecimento
   com nome/cidade é best-effort.
3. `cod_item` pode estar com ou sem zeros à esquerda; o `CatalogoService` tenta as
   variações, mas o preço pode ficar nulo.
4. A margem real não existe na base; usamos um proxy por categoria.
5. Sem cache distribuído: cada consulta vai ao Databricks (cold start pode levar minutos).
6. A API de recomendação não exige autenticação (o JWT protege apenas `/catalogo`).
7. O catálogo web ainda usa dados mock; a integração está documentada e há uma demo
   funcional em `/demo-recomendacoes.html`.
8. O pipeline Databricks (notebook/job) não está versionado neste repositório.

### Dados de teste

> Devido a restrições de acesso, a tabela Gold oficial não pôde ser usada no ambiente
> local. Para fins de teste e demonstração, foram criadas tabelas mock (`cliente`,
> `tabela_preco`) no Supabase e uma tabela (`workspace.default.gold_recomendacao_b2b`)
> no Databricks.

Scripts prontos em [`db/`](db/) e guia em [`docs/MASSA_DE_TESTE.md`](docs/MASSA_DE_TESTE.md)
(20 clientes de teste, IDs 1001–1020, com compras e linhas na Gold).

---

## 9. Segurança

- Segredos **somente** em variáveis de ambiente / `scripts/env.local.ps1` (ignorado).
- Rotacione tokens que já tenham sido expostos.
- O histórico do Git não deve conter credenciais; confira antes de cada push.
- `SecurityConfig` libera a API para a demo; em produção, proteja os endpoints.
