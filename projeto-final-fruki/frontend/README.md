# Front-end do catálogo Pede Fruki

Este front React foi integrado ao Spring Boot. O catálogo publicado é servido em
`/catalogo` pelo template Thymeleaf protegido pelo JWT; os arquivos compilados ficam
em `src/main/resources/static/catalogo-app`.

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

## Dados e funções

- `GET /api/v1/produtos?limite=200` fornece os produtos e preços cadastrados.
- `GET /api/v1/recomendacoes/{clienteId}?limite=8` alimenta recomendações e o combo
  sugerido para aquele cliente.
- `POST /api/v1/pedidos` grava os itens e calcula o total oficial.
- As imagens vêm das famílias em `src/main/resources/static/images`; nomes/categorias
  são associados às imagens locais e usam o SVG de produto genérico como fallback.

O combo sugerido usa os três itens mais bem classificados. Ele não aplica desconto,
pois a API de pedidos atual não possui contrato de promoção; o total é confirmado pelo
backend com os preços da tabela.
