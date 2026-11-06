# Integração do front com a API

O catálogo atual (`templates/catalogo.html`) usa um array mock. Abaixo está o caminho
para trocar o mock pela API real, sem quebrar o layout.

> Já existe uma página funcional de referência: **`/demo-recomendacoes.html`**.
> Ela mostra o JSON, o contexto e os indicadores em cards. Use-a para testar antes de
> mexer no catálogo.

---

## 1. Dados do lojista no HTML

`CatalogoController` já envia `clienteId` e `cnpjCliente` (vindos do JWT). O template
tem:

```html
<meta name="cliente-id" th:content="${clienteId}">
<meta name="cnpj-cliente" th:content="${cnpjCliente}">
```

Leia no JavaScript:

```js
const clienteId = document.querySelector('meta[name="cliente-id"]').content;
const cnpjCliente = document.querySelector('meta[name="cnpj-cliente"]').content;
```

---

## 2. Buscar a recomendação

```js
async function buscarRecomendacoes(id) {
  const resposta = await fetch(`/api/v1/recomendacoes/${id}?limite=8`, {
    headers: { "Accept": "application/json" }
  });
  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}));
    throw new Error(erro.detail || `HTTP ${resposta.status}`);
  }
  return resposta.json(); // { idCliente, nomeCliente, perfil, contexto, itens: [...] }
}
```

Cada item tem:

```json
{
  "posicao": 1,
  "codItem": "00001",
  "nome": "Fruki Guaraná 2L",
  "categoria": "Refrigerantes",
  "preco": 7.49,
  "score": 92.4,
  "motivos": ["compra recorrente (8 pedidos)", "perfil Mercado Central"],
  "detalhe": { "rfm": 0.873, "regional": 0.85, "sazonalidade": 0.8, "margem": 0.6, "penalidade": 0 }
}
```

---

## 3. Renderizar os cards

Substitua `renderizarRecomendacoes()` por algo assim:

```js
function renderizarRecomendacoesApi(rec) {
  const container = document.getElementById('container-recomendacoes');
  container.innerHTML = rec.itens.map(item => `
    <div class="col">
      <div class="card card-produto h-100 shadow-sm border-0">
        <div class="card-img-container">
          <span class="badge-destaque">#${item.posicao} · ${item.score}</span>
          <img src="${imagemDoProduto(item.nome)}"
               alt="${item.nome}"
               onerror="this.src='https://placehold.co/200x200/006633/ffffff?text=Fruki'">
        </div>
        <div class="card-body p-4 d-flex flex-column justify-content-between">
          <div>
            <span class="badge bg-light text-success fw-bold mb-2">${item.categoria}</span>
            <h5 class="fw-bold text-dark mb-1">${item.nome}</h5>
            <ul class="text-muted small mb-3">
              ${(item.motivos || []).map(m => `<li>${m}</li>`).join('')}
            </ul>
          </div>
          <div class="d-flex align-items-center justify-content-between pt-3 border-top">
            <div>
              <span class="text-muted fs-7 block">Preço B2B</span>
              <span class="fs-5 fw-extrabold text-success">
                ${item.preco == null ? '—' : item.preco.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
              </span>
            </div>
            <button onclick="adicionarAoCarrinhoApi('${item.codItem}', '${item.nome}', ${item.preco})"
                    class="btn btn-fruki d-flex align-items-center gap-2">
              <i class="fa-solid fa-plus"></i><span>Adicionar</span>
            </button>
          </div>
        </div>
      </div>
    </div>`).join('');
}

async function carregarRecomendacoes() {
  const id = document.querySelector('meta[name="cliente-id"]').content;
  if (!id) return; // sem JWT: mantém o mock
  try {
    const rec = await buscarRecomendacoes(id);
    renderizarRecomendacoesApi(rec);
  } catch (e) {
    console.warn('Recomendação indisponível, mantendo mock:', e.message);
  }
}
```

Chame `carregarRecomendacoes()` no `window.onload`.

---

## 4. Carrinho por `codItem`

O carrinho atual usa `id` numérico. Troque a chave para `codItem` (string):

```js
function adicionarAoCarrinhoApi(codItem, nome, preco) {
  const existente = carrinho.find(i => i.codItem === codItem);
  if (existente) existente.quantidade += 1;
  else carrinho.push({ codItem, nome, preco, quantidade: 1 });
  atualizarCarrinhoDrawer();
}
```

Ajuste `alterarQuantidade` e o total para usar `codItem`/`preco` (o código atual já
calcula `preco * quantidade`; só a busca muda).

---

## 5. Enviar o pedido para a API

```js
async function enviarPedidoApi() {
  if (!carrinho.length) return;
  const body = {
    cnpjCliente: document.querySelector('meta[name="cnpj-cliente"]').content,
    itens: carrinho.map(i => ({ codigoProduto: i.codItem, quantidade: i.quantidade }))
  };

  const resposta = await fetch('/api/v1/pedidos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}));
    alert('Falha ao enviar: ' + (erro.detail || resposta.status));
    return;
  }
  const pedido = await resposta.json();
  alert(`Pedido ${pedido.codigoPedido} criado. Total: R$ ${pedido.valorTotal}`);
  carrinho = [];
  atualizarCarrinhoDrawer();
}
```

O total exibido no drawer é estimado; o total oficial é o retornado pela API.

---

## 6. Catálogo completo pela API (opcional)

`GET /api/v1/produtos?limite=50` devolve `{ codItem, nome, categoria, preco }`.
Use para substituir o array `produtosFruki` e o filtro de categoria:

```js
const produtos = await fetch('/api/v1/produtos?limite=100').then(r => r.json());
```

O filtro passa a comparar `produto.categoria` (string) em vez do objeto mock.

**Imagens:** a API não devolve imagem. Crie um mapa nome → arquivo local:

```js
const IMAGENS = {
  'Fruki Guaraná 2L': '/images/Refri fruki/guarana1-5l.webp',
  'Água da Pedra 500ml': '/images/Agua fruki/fruki-500ml-agua-s-gas-gotas.webp'
  // ...
};
const imagemDoProduto = (nome) => IMAGENS[nome] || '/images/brand/produto-placeholder.svg';
```

---

## 7. Faixa de contexto (clima/estação/feriado)

```js
const ctx = await fetch('/api/v1/contexto?cidade=Porto%20Alegre&uf=RS').then(r => r.json());
document.getElementById('banner-contexto').textContent =
  `${ctx.estacaoDescricao} · ${ctx.temperatura} °C · ${ctx.feriadoProximo ? 'feriado próximo' : 'sem feriado'}`;
```

---

## 8. Erros e resiliência

- `404`: cliente sem cadastro → mostre "fale com o atendimento".
- `503`: Databricks fora → mantenha o mock e exiba um aviso discreto.
- `400`: validação → mostre `erros[]` (campo/mensagem).
- Sem `clienteId` (JWT ausente): não chame a API; mantenha o mock.

```js
try {
  const rec = await buscarRecomendacoes(id);
  renderizarRecomendacoesApi(rec);
} catch (e) {
  // fallback: renderizarRecomendacoes() do mock atual
}
```

---

## 9. Checklist de integração

- [ ] Meta tags `cliente-id` e `cnpj-cliente` presentes
- [ ] `fetch` de `/api/v1/recomendacoes/{id}` funcionando
- [ ] Cards com nome, categoria, preço, score e motivos
- [ ] Carrinho usando `codItem`
- [ ] `POST /api/v1/pedidos` retornando 201 e total real
- [ ] Fallback para mock em caso de 503
- [ ] Filtro de categoria pela API (se trocar o catálogo)
- [ ] Imagens mapeadas para os `.webp` locais
