# Massa de teste — clientes, compras e Gold

Scripts prontos para você popular o banco e ter **20 clientes** para testar a API.
Nada é executado automaticamente: você cola o SQL no Supabase/Databricks.

> ⚠️ **Use somente em ambiente de teste/demonstração.** Os CNPJs são fictícios
> (válidos apenas no dígito verificador). Não rode em produção.

## Arquivos

| Arquivo | Onde rodar | O que faz |
|---|---|---|
| `db/seed_supabase.sql` | Supabase → SQL Editor | **102 produtos** (catálogo completo), 20 clientes (IDs 1001–1020) e 40 compras em `fato_vendas` |
| `db/seed_clientes.sql` | Supabase → SQL Editor | **somente** os clientes (1001–1020 + 9001), sem depender de `tabela_preco` |
| `db/seed_produtos.sql` | Supabase → SQL Editor | **102 produtos** do portfólio Fruki (`00001`–`00124`) para a massa de 80 |
| `db/seed_80_clientes.sql` | Supabase → SQL Editor | **80 clientes** (IDs 2001–2080), CNPJs únicos e cidades variadas |
| `db/seed_80_compras.sql` | Supabase → SQL Editor | 160 compras (2 por cliente) em `fato_vendas` |
| `db/seed_80_gold.sql` | Databricks → SQL Editor | 368 linhas na Gold (4–5 itens por cliente) |
| `db/seed_gold_databricks.sql` | Databricks → SQL Editor | 60 linhas na Gold para os IDs 1001–1020 |

> **Se o seed completo falhar** (ex.: `tabela_preco` com schema diferente), rode
> primeiro `db/seed_clientes.sql`. Ele cria os IDs 1001–1020 **e o 9001** com CNPJ
> `11222333000181`, que é o CNPJ usado nos exemplos do painel. O `ON CONFLICT` torna
> a execução repetível.

## Ordem de execução

1. **Supabase:** abra o SQL Editor e rode `db/seed_supabase.sql`.
2. **Databricks:** abra o SQL Editor e rode `db/seed_gold_databricks.sql`.
   - Se a sua tabela tiver outro nome, ajuste as duas ocorrências de
     `workspace.default.gold_recomendacao_b2b`.
3. Confira:

```sql
-- Supabase
select cod_cliente, nome_fantasia, cidade, uf
from cliente
where cod_cliente between 1001 and 1020
order by cod_cliente;

-- Databricks
select id_cliente, count(*) as itens
from workspace.default.gold_recomendacao_b2b
where id_cliente between 1001 and 1020
group by id_cliente
order by id_cliente;
```

## Clientes criados

| IDs | Perfil esperado | Cidades |
|---|---|---|
| 1001–1004, 1019 | Super Litoral | Capão da Canoa, Tramandaí, Torres, Balneário Camboriú, Florianópolis |
| 1005–1008 | Conveniência Serra | Gramado, Canela, Caxias do Sul, Bento Gonçalves |
| 1009–1012, 1020 | Distribuidora Vale | Lajeado, Estrela, Teutônia, Encantado, Joinville |
| 1013–1018 | Mercado Central | Porto Alegre, Canoas, Santa Cruz, Novo Hamburgo, São Leopoldo, Gravataí |

### O que esperar no ranking

| ID | Perfil | Topo esperado |
|---|---|---|
| 1001 | Litoral | Água, energético, guaraná |
| 1005 | Serra | Cerveja, suco, guaraná |
| 1009 | Vale | Guaraná, água, suco |
| 1013 | Central | Guaraná, cola, suco |

O score exato muda com a estação/clima/feriado do dia — o **perfil** e a **ordem por
categoria** é que devem se manter.

## Como consultar os 20 de uma vez (PowerShell)

```powershell
1..20 | ForEach-Object {
    $id = 1000 + $_
    try {
        $r = Invoke-RestMethod "http://localhost:8080/api/v1/recomendacoes/$id?limite=3" -TimeoutSec 120
        $top = $r.itens | Select-Object -First 1
        "{0} | {1} | {2} | top: {3} ({4})" -f $id, $r.nomeCliente, $r.perfil, $top.nome, $top.score
    } catch {
        "{0} | ERRO {1}" -f $id, $_.Exception.Message
    }
}
```

Saída esperada: 20 linhas com nome, perfil e o item de topo.

## Massa de 80 clientes (2001–2080)

Para ter **80 recomendações diferentes**, rode nesta ordem:

1. `db/seed_produtos.sql` (Supabase) — garante o catálogo completo (`00001`–`00124`), com os mesmos códigos, nomes e preços exibidos no front.
2. `db/seed_80_clientes.sql` (Supabase) — 80 clientes, um por cidade.
3. `db/seed_80_compras.sql` (Supabase) — histórico em `fato_vendas` para o bot.
4. `db/seed_80_gold.sql` (Databricks) — 4–5 itens de Gold por cliente.

Distribuição das cidades:

| Perfil | IDs | Cidades |
|---|---|---|
| Super Litoral | 2001–2020 | Capão da Canoa, Tramandaí, Torres, Cidreira, Xangri-lá, ... Balneário Camboriú, Florianópolis, São Francisco do Sul |
| Conveniência Serra | 2021–2035 | Gramado, Canela, Caxias do Sul, Bento Gonçalves, ... Flores da Cunha |
| Distribuidora Vale | 2036–2050 | Lajeado, Estrela, Teutônia, Encantado, ... Ilópolis |
| Mercado Central | 2051–2080 | Porto Alegre, Canoas, ... Osório, Joinville, Blumenau, ... Caçador |

Cada cliente tem um CNPJ **único e válido** (dígitos verificadores conferidos) e um
histórico próprio (quantidades, valores, datas e scores diferentes).

### Ver todos os CNPJs

- **No painel:** aba **Todos os CNPJs** (`/painel.html`) → lista `codCliente`, nome,
  CNPJ, cidade/UF e botões **Testar** e **Copiar**. Vem de
  `GET /api/v1/clientes?limite=500`.
- **No SQL:**
  ```sql
  select cod_cliente, nome_fantasia, cnpj, cidade, uf
  from cliente where cod_cliente between 2001 and 2080 order by cod_cliente;
  ```
- **No arquivo:** `db/seed_80_clientes.sql`.

### Consultar os 80 de uma vez

```powershell
2001..2080 | ForEach-Object {
    try {
        $r = Invoke-RestMethod "http://localhost:8080/api/v1/recomendacoes/$_?limite=3" -TimeoutSec 120
        $top = $r.itens | Select-Object -First 1
        "{0} | {1} | {2} | top: {3} ({4})" -f $_, $r.nomeCliente, $r.perfil, $top.nome, $top.score
    } catch {
        "{0} | ERRO {1}" -f $_, $_.Exception.Message
    }
}
```

### Limpar a massa de 80

```sql
-- Supabase
delete from fato_vendas where cod_cliente::text between '2001' and '2080';
delete from cliente where cod_cliente between 2001 and 2080;

-- Databricks
delete from workspace.default.gold_recomendacao_b2b where id_cliente between 2001 and 2080;
```

## Testes de comportamento

| Cenário | Como testar | Resultado esperado |
|---|---|---|
| Cliente com histórico | `GET /recomendacoes/1001` | 200, itens com `motivos` e `detalhe` |
| Cliente sem histórico (cold start) | crie um cliente 3001 **sem** linha na Gold | 200, itens vindos do catálogo + motivo "sem histórico" |
| Cliente inexistente | `GET /recomendacoes/999999` | 404 em `ProblemDetail` |
| ID inválido | `GET /recomendacoes/abc` | 400 |
| Catálogo sem token | `GET /catalogo` | 401 |
| Indicadores | `GET /api/v1/indicadores` | cobertura/variedade/taxa |
| Ingestão | `POST /clientes` com CNPJ novo | 201; repetir → 409 |
| Compra | `POST /compras` com item `00001` | 201 com total |
| Pedido | `POST /pedidos` com 2 itens | 201 com total real |

### Cold start (cliente sem Gold)

```sql
-- Supabase: cliente que NÃO existe na Gold
insert into cliente (cod_cliente, nome_fantasia, cnpj, cidade, uf)
values (3001, 'Mercado Cold Start', '10000021000160', 'Porto Alegre', 'RS')
on conflict do nothing;
```

```powershell
curl.exe -s "http://localhost:8080/api/v1/recomendacoes/3001" | ConvertFrom-Json | ConvertTo-Json -Depth 5
```

Deve vir **200** com itens do catálogo e o motivo `sem histórico`.

## Limpeza

```sql
-- Supabase
delete from fato_vendas where cod_cliente between 1001 and 1020;
delete from cliente    where cod_cliente between 1001 and 1020;
delete from tabela_preco where id like 'seed-%';

-- Databricks
delete from workspace.default.gold_recomendacao_b2b where id_cliente between 1001 and 1020;
```

## Observações

1. Se `tabela_preco.id` for numérico no seu banco, troque `seed-00001` por um número
   (ex.: `90001`).
2. Se `fato_vendas` tiver colunas `NOT NULL` além de `cod_cliente`, `data_pedido`,
   `valor_total`, `status` e `resumo_itens`, ajuste o `INSERT`.
3. Os CNPJs foram gerados com dígitos verificadores válidos, mas são fictícios.
4. O `DELETE` da Gold toca **apenas** os IDs 1001–1020; 41/48/81/112 ficam intactos.
