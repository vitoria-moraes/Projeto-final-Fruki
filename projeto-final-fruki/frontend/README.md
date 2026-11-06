# Front-end do catálogo Pede Fruki

Front React integrado ao Spring Boot. O catálogo publicado é servido em `/catalogo`
pelo template Thymeleaf protegido pelo JWT; os arquivos compilados ficam em
`src/main/resources/static/catalogo-app`.

## Desenvolvimento e compilação

```powershell
cd frontend
npm ci
npm run build
cd ..
. .\scripts\env.local.ps1
.\mvnw.cmd spring-boot:run
```

O build coloca JavaScript e CSS na pasta estática consumida pelo template.

## Catálogo

- `src/data/catalog.js` concentra o portfólio completo: **102 produtos**, 7 categorias,
  nomes normalizados, volume, fardos e preços de referência.
- `src/data/catalogBuilder.js` cruza os produtos da API com o catálogo local:
  - produtos da API têm **preço da tabela** e código real para pedido;
  - itens que só existem no catálogo local aparecem com **preço de referência** e
    são finalizados como pedido demonstrativo até a tabela ser confirmada;
  - imagens, fardos e categorias vêm de um único lugar, sem duplicar cartas.
- A página tem busca sem acento, filtro por categoria com contagem, ordenação,
  carregamento em lotes, carrinho persistido por CNPJ, sugestões do motor e painel
  de histórico.

## Dados e funções

- `GET /api/v1/produtos?limite=200` fornece os produtos e preços cadastrados.
- `GET /api/v1/recomendacoes/{clienteId}?limite=8` alimenta recomendações e o combo
  sugerido para aquele cliente.
- `POST /api/v1/pedidos` grava os itens e calcula o total oficial.
- As imagens vêm das famílias em `src/main/resources/static/images`; nomes/categorias
  são associados às imagens locais e usam o SVG de produto genérico como fallback.

## Catálogo no banco

O arquivo `db/seed_produtos.sql` contém os mesmos 102 produtos do front (códigos,
nomes e preços) para que a tabela `tabela_preco` fique alinhada ao catálogo exibido.
Quando a API responde, o preço cadastrado nela tem prioridade sobre o preço de
referência do front.

O combo sugerido usa os três itens mais bem classificados. Ele não aplica desconto,
pois a API de pedidos atual não possui contrato de promoção; o total é confirmado pelo
backend com os preços da tabela.
