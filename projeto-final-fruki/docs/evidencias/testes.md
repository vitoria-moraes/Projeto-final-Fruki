# Evidência — testes automatizados

- **Data:** 2026-11-05
- **Ambiente:** Windows 11, JDK 21.0.12.1, Spring Boot 3.2.5
- **Comando:** `.\mvnw.cmd test`

## Resultado

```text
Tests run: 41, Failures: 0, Errors: 0, Skipped: 4
BUILD SUCCESS
```

Os 4 ignorados são testes de integração que exigem `SUPABASE_JDBC_URL` e
`JWT_SECRET` no ambiente (anotados com `@EnabledIfEnvironmentVariable`). Sem essas
variáveis eles são pulados em vez de falhar.

## Cobertura por classe de teste

| Teste | O que valida |
|---|---|
| `RecomendadorV2ServiceTest` | score 0..100, diversidade (máx. 2/categoria), penalidade de compra recente, cold start |
| `RecomendacaoServiceTest` | 200 com histórico, 404 sem cliente/histórico, 503 do Databricks, enriquecimento com nome |
| `RecomendacaoControllerTest` | contrato JSON, 200/404/503/400 (`@WebMvcTest`) |
| `PedidoControllerTest` | 201 e total calculado (`@WebMvcTest`) |
| `PedidoServiceTest` | total real, cliente inexistente |
| `CompraServiceTest` | total pelo preço, 404/400 |
| `ClienteServiceTest` | cadastro, CNPJ inválido, 409 duplicado, listagem |
| `IndicadoresServiceTest` | cobertura, taxa, média, base vazia |
| `ContextoServiceTest` | verão + feriado, inverno, sem feriado |
| `EstacaoTest` | as quatro estações no Hemisfério Sul |
| `MotorSazonalidadeTest` | mensagens de feriado, calor e inverno |
| `CnpjValidatorTest` | CNPJ válido/inválido, normalização |
| `RecomendacaoPropertiesTest` | binding dos pesos/limites do `application.yml` |

## Como repetir

```powershell
cd projeto-final-fruki
.\mvnw.cmd test
```

Para rodar só uma classe:

```powershell
.\mvnw.cmd test -Dtest=RecomendadorV2ServiceTest
```

## Teste de integração (opcional)

Com as variáveis carregadas:

```powershell
. .\scripts\env.local.ps1
.\mvnw.cmd test
```

Aí os 4 testes de contexto/webhook também executam (exigem Supabase e JWT_SECRET).
