# 🟢 Pede Fruki - Ecossistema Digital B2B

O **Pede Fruki** é uma solução B2B desenvolvida para o **Desafio Geração Caldeira 2026**. O projeto integra um catálogo inteligente via web e um chatbot automatizado pelo WhatsApp, otimizando o processo de vendas, recomendações ativas e análise de dados para lojistas parceiros da Fruki Bebidas.

## 🚀 Funcionalidades Principais

*   **Chatbot WhatsApp (Evolution API):** Autoatendimento rápido para lojistas diretamente pelo WhatsApp, integrado via Webhook.
*   **Catálogo Web Inteligente:** Interface visual fluida construída com Thymeleaf para navegação de produtos e fechamento de pedidos.
*   **Motor de Sazonalidade:** Algoritmo que cruza dados de localização, clima (dias quentes > 28°C) e feriados para gerar recomendações personalizadas de bebidas.
*   **Segurança Avançada:** Rotas sensíveis e catálogo web protegidos por autenticação JWT (JSON Web Token).
*   **Gestão de Pedidos:** Validação rigorosa de CNPJ, consulta de tabelas de preços em tempo real e cálculo exato do total da compra.
*   **Data Warehouse & Analytics:** Integração de dados transacionais com o Databricks (Lakehouse) para geração de insights, relatórios de vendas e inteligência de negócios (BI).

## 🛠️ Tecnologias Utilizadas

*   **Linguagem:** Java 21
*   **Framework:** Spring Boot 3
*   **Banco de Dados Transacional:** Supabase (PostgreSQL) com JPA/Hibernate
*   **Data & Analytics:** Databricks SQL Serverless
*   **Mensageria / Gateway:** Evolution API
*   **Front-end:** Thymeleaf, HTML5, CSS3
*   **Segurança:** Spring Security, JWT
*   **Testes:** JUnit 5, MockMvc

## 🏗️ Arquitetura do Sistema

1.  **Interfaces de Entrada:** O lojista interage via WhatsApp ou pelo Catálogo Web.
2.  **Gateway (Webhook):** A Evolution API escuta o WhatsApp e envia payloads JSON para a rota `/api/v1/webhook/whatsapp`.
3.  **Camada de Serviços:** O Spring Boot processa a regra de negócio (`PedidoService`, `CatalogoService`, `MotorSazonalidadeService`).
4.  **Persistência:** Dados operacionais (Clientes, Preços, Pedidos) são gravados em tempo real no Supabase.
5.  **Analytics (ETL):** Dados históricos alimentam a `FatoVendasRepository` no Databricks para consumo analítico.

## ⚙️ Como Executar Localmente

### Pré-requisitos
*   Java 21 instalado (JDK).
*   Maven.
*   PowerShell (para rodar o script de variáveis locais no Windows).

### Configuração de Variáveis de Ambiente
Crie um arquivo chamado `env.local.ps1` na pasta `scripts/` (este arquivo é ignorado pelo Git por segurança) com as seguintes chaves:

```powershell
$env:SUPABASE_JDBC_URL = 'jdbc:postgresql://[aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require](https://aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require)'
$env:SUPABASE_USER = 'postgres.<id-do-projeto>'$env:SUPABASE_PASSWORD = '<sua-senha>'
$env:JWT_SECRET = '<token-de-32-caracteres>'$env:JWT_EXPIRATION_MINUTES = '7200'
$env:DATABRICKS_HOST = 'dbc-<id>.cloud.databricks.com'$env:DATABRICKS_HTTP_PATH = '/sql/1.0/warehouses/<id-do-warehouse>'
$env:DATABRICKS_TOKEN = '<seu-token-databricks>'$env:DATABRICKS_RECOMENDACAO_TABLE = 'fruki.gold.gold_recomendacao_b2b'
