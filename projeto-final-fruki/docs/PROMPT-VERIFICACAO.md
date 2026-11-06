# Prompt para uma IA verificar o projeto

Copie o bloco abaixo e cole em outra IA que tenha acesso ao repositório
(ou use junto com o projeto aberto). Ele foi escrito para uma IA **revisora**,
não para reescrever o código.

```text
Você é um revisor técnico sênior de Java/Spring Boot. Sua tarefa é AUDITAR e
TESTAR o projeto Pede Fruki, sem reescrever a arquitetura e sem commitar nada.

CONTEXTO
- Repositório: https://github.com/vitoria-moraes/Projeto-final-Fruki
- Código: pasta projeto-final-fruki/
- Stack: Spring Boot 3.2.5, Java 21, Supabase (PostgreSQL/JPA), Databricks (JDBC),
  Thymeleaf, JWT no catálogo, WhatsApp (Evolution API).
- Estado: o backend foi implementado (recomendador V2, contexto do Sul, ingestão,
  indicadores, tratamento de erros, testes e documentação).
- O que NÃO está pronto: catálogo `catalogo.html` ainda mock; pipeline Databricks
  (notebook/job) não versionado; runtime real contra Supabase/Databricks não validado
  nesta máquina.

ARQUIVOS-CHAVE PARA LER ANTES DE OPINAR
- README.md
- docs/API.md
- docs/RECOMENDADOR.md
- docs/INTEGRACAO_FRONT.md
- docs/SCHEMA.md
- docs/PLANEJAMENTO.md (seção 15 — relatório de execução)
- src/main/java/org/example/projetofinalfruki/domain/service/RecomendadorV2Service.java
- src/main/java/org/example/projetofinalfruki/domain/service/RecomendacaoService.java
- src/main/java/org/example/projetofinalfruki/application/controller/GlobalExceptionHandler.java
- src/test/java/org/example/projetofinalfruki/

O QUE VOCÊ DEVE FAZER (nesta ordem)
1. Compilar: `.\mvnw.cmd -B -DskipTests test-compile`.
2. Rodar os testes: `.\mvnw.cmd -B test`. Esperado: 41 testes, 0 falhas, 4 ignorados.
3. Conferir o contrato real de:
   - `GET /api/v1/recomendacoes/{id}` (200/404/400/503)
   - `POST /api/v1/clientes` (201/400/409)
   - `POST /api/v1/compras` (201/400/404)
   - `POST /api/v1/pedidos` (201/400/404)
   - `GET /api/v1/indicadores`
   - `GET /api/v1/contexto`, `/estacao`, `/feriados`
   Compare o JSON real com `docs/API.md`.
4. Verificar se o recomendador:
   - normaliza score 0..100;
   - aplica diversidade (máx. 2/categoria);
   - trata cold start;
   - devolve `motivos[]` e `detalhe`.
5. Verificar se o tratamento de erros devolve ProblemDetail com `type/title/status/detail`.
6. Apontar divergências entre o relatório (seção 15 do PLANEJAMENTO) e o código.
7. NÃO alterar código, NÃO commitar, NÃO usar credenciais. Se precisar rodar a app,
   peça ao usuário para subir com as variáveis dele.

COMO TESTAR SEM TERMINAL (sugira ao usuário)
- IntelliJ: abrir `docs/api.http` e clicar no ▶ de cada requisição (HTTP Client).
- IntelliJ: clicar no ▶ ao lado de cada classe de teste.
- Navegador: `http://localhost:8080/demo-recomendacoes.html`.
- PowerShell: `.\scripts\testar-api.ps1` (com a app rodando).
- Postman/Insomnia: importar os exemplos de `docs/API.md`.

FORMATO DA SUA RESPOSTA
1. Veredito geral (o que está sólido / o que está frágil).
2. Tabela: item verificado | evidência | status (OK / ATENÇÃO / FALHA).
3. Falhas encontradas, com arquivo:linha e o comando para reproduzir.
4. Riscos que não são bugs (schema, join key, sem cache, API aberta).
5. Próximos 3 passos objetivos (sem reescrever o projeto).
6. Perguntas ao usuário, se faltar informação.

REGRAS
- Não invente endpoints, campos ou tabelas: confira no código.
- Não diga que algo passou sem rodar ou citar a evidência.
- Não peça nem reproduza tokens/senhas.
- Se algo não puder ser verificado (banco/credenciais), diga explicitamente
  "não foi possível verificar" e explique o que falta.
```

## Sugestões de perguntas para fazer à IA

- “Rode a suíte e me diga exatamente quais testes cobrem o recomendador.”
- “Compare o JSON de `/recomendacoes/41` com o contrato de `docs/API.md`.”
- “Liste os cenários de erro que ainda não têm teste.”
- “Explique por que `score` pode vir baixo para um item com RFM alto.”
- “O que quebra se `cod_item` vier sem zero à esquerda?”
