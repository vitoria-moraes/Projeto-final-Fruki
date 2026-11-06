import { useEffect, useMemo, useState } from 'react';
import Header from '../components/Header/Header';
import CatalogHeader from '../components/Catalog/CatalogHeader';
import SearchBar from '../components/Catalog/SearchBar';
import CategoryFilter from '../components/Catalog/CategoryFilter';
import ProductCard from '../components/Catalog/ProductCard';
import FloatingCart from '../components/Catalog/FloatingCart';
import CartModal from '../components/Catalog/CartModal';
import { createOrder, getContext, getDemo, getProducts, getRecommendations, getPurchaseHistory } from '../api';
import productImages from '../data/productImages.json';
import { canonicalCategory, formatProductName, normalizeText } from '../utils/productFormatting';

const placeholder = '/images/brand/produto-placeholder.svg';
const formatMoney = (value) => value == null || Number.isNaN(Number(value))
  ? 'Preço indisponível'
  : Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const normalize = normalizeText;
const ignoredWords = new Set(['fruki', 'bebida', 'bebidas', 'produto', 'garrafa', 'pet', 'lata', 'fardo', 'pack', 'com', 'sem', 'de', 'da', 'do', 'e']);

function unitsPerPack(name = '') {
  const explicit = String(name).match(/(?:c\/?\s*|com\s+)(\d{1,2})\s*(?:un|unid|unidades)?\b/i);
  if (explicit) return Number(explicit[1]);
  const normalized = normalize(name);
  if (/\b350\s?ml\b/.test(normalized)) return 12;
  if (/\b473\s?ml\b/.test(normalized)) return 6;
  if (/\b500\s?ml\b/.test(normalized)) return 12;
  return 6;
}

function volumeInMl(value = '') {
  const text = String(value).toLowerCase().replace(',', '.');
  const encodedDecimal = text.match(/(\d+)[_-](\d+)\s*l\b/);
  if (encodedDecimal) return Math.round(Number(`${encodedDecimal[1]}.${encodedDecimal[2]}`) * 1000);
  const match = text.match(/(\d+(?:\.\d+)?)\s*(ml|l)\b/) || text.match(/(?:pet|_)(\d{3,4})(?:\b|_)/);
  if (!match) return null;
  if (!match[2]) return Number(match[1]);
  const amount = Number(match[1].replace(',', '.'));
  return Math.round(match[2] === 'l' ? amount * 1000 : amount);
}

function estimatedUnitPrice(categoryName = '', productName = '') {
  const category = normalize(categoryName);
  const volume = volumeInMl(productName);
  if (!volume) return null;
  const tiers = category.includes('agua')
    ? [[250, 0.99], [360, 1.29], [550, 1.69], [1000, 2.49], [1600, 3.49], [2500, 4.49], [6000, 7.49]]
    : category.includes('energet')
      ? [[300, 4.99], [500, 7.99], [1000, 11.99], [2200, 14.99]]
      : category.includes('cervej')
        ? [[360, 4.49], [500, 5.99], [1000, 10.99]]
        : category.includes('refres') || category.includes('suco')
          ? [[250, 2.49], [550, 4.99], [1100, 8.49], [2200, 13.99]]
          : category.includes('tonic')
            ? [[400, 2.99], [700, 4.49], [1700, 6.49]]
            : [[250, 1.49], [300, 2.49], [400, 2.99], [700, 4.99], [1600, 7.99], [2200, 9.49], [3500, 12.49]];
  return tiers.find(([maxVolume]) => volume <= maxVolume)?.[1] ?? tiers[tiers.length - 1][1];
}

function nextHoliday(context) {
  const today = new Date(); today.setHours(0, 0, 0, 0);
  return (context?.feriados || []).filter((holiday) => holiday.date && new Date(`${holiday.date}T00:00:00`) >= today)
    .sort((a, b) => a.date.localeCompare(b.date))[0];
}

function imageFor(product) {
  const terms = normalize(product.nome).split(/[^a-z0-9]+/).filter((word) => word.length > 1 && !ignoredWords.has(word));
  const category = normalize(product.categoria);
  const directory = category.includes('agua') ? '/agua fruki/'
    : category.includes('energet') ? '/energetico fruki/'
      : category.includes('cervej') ? '/cervejas fruki/'
        : category.includes('tonic') ? '/tonicas fruki/'
          : category.includes('refres') ? '/sucos citrus fruki/'
            : category.includes('suco') ? '/sucos fruki/'
              : category.includes('refri') ? '/refri fruki/' : null;
  if (!directory) return placeholder;

  const targetVolume = volumeInMl(product.nome);
  const candidates = productImages.filter((path) => normalize(path).includes(directory));
  const exactVolume = targetVolume == null ? [] : candidates.filter((path) => volumeInMl(path) === targetVolume);
  const namedExactVolume = exactVolume.filter((path) => {
    const file = normalize(path.split('/').pop());
    return terms.some((term) => file.includes(term));
  });
  const pool = namedExactVolume.length ? namedExactVolume : candidates;
  const ranked = pool.map((path) => {
    const file = normalize(path.split('/').pop());
    const matches = terms.filter((term) => file.includes(term)).length;
    const volumeMatch = targetVolume == null || volumeInMl(path) === targetVolume;
    return { path, score: matches * 3 + (volumeMatch ? 8 : 0) };
  }).sort((a, b) => b.score - a.score);
  return ranked[0]?.path || placeholder;
}

function toProduct(item) {
  const category = canonicalCategory(item.categoria);
  return {
    id: String(item.codItem),
    codItem: String(item.codItem),
    name: formatProductName(item.nome, item.categoria),
    category: category.id,
    categoryName: category.name,
    description: 'Produto selecionado para sua loja.',
    price: item.preco == null ? estimatedUnitPrice(item.categoria, item.nome) : Number(item.preco),
    packSize: unitsPerPack(item.nome),
    image: imageFor(item),
    // Disponibilidade visual do catálogo não depende de dados de demonstração.
    // O botão de compra continua condicionado a código/preço reais abaixo.
    available: true,
    orderable: Boolean(item.codItem),
    demonstracao: Boolean(item.demonstracao) || String(item.codItem).startsWith('MOCK-'),
  };
}

const familyName = (path) => canonicalCategory(path.split('/')[2]?.replace(/\bfruki\b/i, '').trim() || 'Outros').name;
function imageTitle(path) {
  const raw = path.split('/').pop().replace(/\.[^.]+$/, '');
  return formatProductName(raw, familyName(path)) || raw;
}

function readClientMeta(name) {
  return document.querySelector(`meta[name="${name}"]`)?.content?.trim() || '';
}

export default function Catalogo() {
  const clientId = readClientMeta('cliente-id');
  const cnpjCliente = readClientMeta('cnpj-cliente');
  const clientName = readClientMeta('nome-cliente') || 'Lojista';
  const region = readClientMeta('regiao-cliente');
  const city = readClientMeta('cidade-cliente') || 'Porto Alegre';
  const uf = readClientMeta('uf-cliente') || 'RS';

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('todos');
  const [cartItems, setCartItems] = useState({});
  const [products, setProducts] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [context, setContext] = useState(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [recommendationMessage, setRecommendationMessage] = useState('Carregando sugestões para sua loja…');
  const [pageError, setPageError] = useState('');
  const [orderMessage, setOrderMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [purchaseHistory, setPurchaseHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');

  useEffect(() => {
    let active = true;
    getProducts()
      .then((rows) => { if (active) setProducts(rows.map(toProduct)); })
      .catch((error) => { if (active) setPageError(`Não foi possível carregar o catálogo: ${error.message}`); })
      .finally(() => { if (active) setLoadingProducts(false); });

    if (!clientId) {
      setRecommendationMessage('Acesse pelo link enviado pelo bot para ver as sugestões personalizadas.');
      return () => { active = false; };
    }

    getRecommendations(clientId)
      .then((result) => {
        if (!active) return;
        setRecommendations((result.itens || []).map((item) => ({ ...toProduct(item), score: item.score, posicao: item.posicao, motivos: item.motivos || [] })));
        setRecommendationMessage(result.itens?.length ? '' : 'Ainda não há sugestões para exibir.');
        if (result.contexto) setContext(result.contexto);
      })
      .catch((error) => {
        if (!active) return;
        getDemo().then((demo) => {
          if (!active) return;
          setRecommendations((demo.recomendacoes?.itens || []).map((item) => ({ ...toProduct(item), score: item.score, posicao: item.posicao, motivos: item.motivos || [] })));
          setRecommendationMessage('Sugestões selecionadas para sua loja.');
        }).catch(() => setRecommendationMessage(error.status === 404
          ? 'N\u00e3o encontramos seu hist\u00f3rico/cadastro para recomenda\u00e7\u00f5es. Fale com o atendimento.'
          : 'Motor indispon\u00edvel: confirme que o Spring Boot est\u00e1 rodando em localhost:8080.'));
      });

    if (!context) getContext(city, uf).then((value) => { if (active) setContext(value); }).catch(() => {});
    return () => { active = false; };
  }, [clientId, city, uf]);

  const categories = useMemo(() => [
    { id: 'todos', name: 'Todos' },
    ...Array.from(new Map([...products.map((product) => canonicalCategory(product.categoryName)), ...productImages.map((image) => canonicalCategory(familyName(image)))].map((category) => [category.id, category])).values()).sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')),
  ], [products]);

  const imageGallery = useMemo(() => {
    const matched = new Set(products.map((product) => product.image));
    return productImages.filter((image) => !matched.has(image)).map((image, index) => ({
      id: `image-${index}`, codItem: `IMG-${String(index + 1).padStart(4, '0')}`, name: imageTitle(image), category: canonicalCategory(familyName(image)).id,
      categoryName: familyName(image), description: 'Consulte a disponibilidade deste produto.',
      price: estimatedUnitPrice(familyName(image), imageTitle(image)), packSize: unitsPerPack(imageTitle(image)), image, available: true, orderable: true,
    }));
  }, [products]);
  const fullCatalog = useMemo(() => [...products, ...imageGallery], [products, imageGallery]);

  const filteredProducts = useMemo(() => fullCatalog.filter((product) => {
    const matchesCategory = selectedCategory === 'todos' || product.category === selectedCategory;
    const matchesSearch = normalize(`${product.name} ${product.categoryName}`).includes(normalize(searchTerm));
    return matchesCategory && matchesSearch;
  }), [fullCatalog, selectedCategory, searchTerm]);

  const comboItems = recommendations.filter((product) => product.orderable).slice(0, 3);
  const comboPrice = comboItems.reduce((sum, product) => sum + (product.price || 0) * product.packSize, 0);
  const cartArray = Object.values(cartItems);
  const totalItems = cartArray.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = cartArray.reduce((sum, item) => sum + (item.price || 0) * item.packSize * item.quantity, 0);

  function handleUpdateQuantity(product, newQuantity) {
    if (!product?.orderable) {
      setOrderMessage(`${product?.name || 'Este produto'} está disponível no catálogo; o código e o preço precisam ser cadastrados antes de incluí-lo no pedido.`);
      return;
    }
    setCartItems((previous) => {
      const updated = { ...previous };
      if (newQuantity <= 0) delete updated[product.codItem];
      else updated[product.codItem] = { ...product, quantity: newQuantity };
      return updated;
    });
    setOrderMessage('');
  }

  function addCombo() {
    comboItems.forEach((product) => handleUpdateQuantity(product, (cartItems[product.codItem]?.quantity || 0) + 1));
  }

  async function openProfile() {
    setProfileOpen(true);
    setHistoryLoading(true);
    setHistoryError('');
    const key = `pede-fruki-demo-orders-${cnpjCliente}`;
    const local = () => JSON.parse(localStorage.getItem(key) || '[]');
    try {
      const history = await getPurchaseHistory(cnpjCliente);
      setPurchaseHistory([...local(), ...history]);
    } catch (error) {
      if (local().length) setPurchaseHistory(local());
      else {
        try { setPurchaseHistory((await getDemo()).compras || []); }
        catch { setHistoryError(error.message); }
      }
    } finally { setHistoryLoading(false); }
  }

  async function finalizarPedido() {
    if (!cnpjCliente) { setOrderMessage('O link não contém o CNPJ do lojista. Gere um novo link pelo bot.'); return; }
    if (!cartArray.length) return;
    setSubmitting(true);
    setOrderMessage('');
    try {
      const demonstracao = cartArray.some((item) => String(item.codItem).startsWith('MOCK-') || String(item.codItem).startsWith('IMG-') || item.price == null || item.demonstracao);
      if (demonstracao) {
        const pedido = { id: `DEMO-${Date.now()}`, dataPedido: new Date().toISOString(), status: 'Pedido concluído', demonstracao: true,
          valorTotal: cartArray.reduce((sum, item) => sum + (item.price || 0) * item.packSize * item.quantity, 0),
          resumoItens: cartArray.map((item) => `${item.quantity} fardo(s) de ${item.name}`).join(' \u00b7 ') };
        const key = `pede-fruki-demo-orders-${cnpjCliente}`;
        const previous = JSON.parse(localStorage.getItem(key) || '[]');
        localStorage.setItem(key, JSON.stringify([pedido, ...previous]));
        setCartItems({});
        setOrderMessage('Pedido concluído! Parabéns! Em breve você receberá a nota fiscal.');
        setIsCartOpen(false);
        return;
      }
      const result = await createOrder(cnpjCliente, cartArray.map(({ codItem, quantity, packSize }) => ({ codItem, quantidade: quantity * packSize })));
      setCartItems({});
      setOrderMessage(`Pedido ${result.codigoPedido} criado com sucesso. Total confirmado: ${formatMoney(result.valorTotal)}.`);
      setIsCartOpen(false);
    } catch {
      setOrderMessage('Não conseguimos concluir o pedido agora. Tente novamente em instantes.');
    } finally { setSubmitting(false); }
  }

  if (!clientId) return (
    <main className="min-h-screen grid place-items-center bg-[#f4f7f3] p-6">
      <section className="max-w-lg rounded-3xl bg-white p-10 text-center shadow-xl">
        <img src="/images/brand/logo-fruki.svg" alt="Pede Fruki" className="mx-auto mb-6 h-14" />
        <h1 className="text-2xl font-extrabold text-[#004d2c]">Acesse pelo link do bot</h1>
        <p className="mt-3 text-slate-600">O catálogo personalizado precisa do link JWT enviado pelo atendimento.</p>
      </section>
    </main>
  );

  return (
    <div className="fruki-page min-h-screen pb-28 text-slate-800">
      <Header userName={clientName} region={region} onProfileClick={openProfile} />
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <section className="fruki-hero relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-[#004b2b] via-[#006a3b] to-[#0f8a50] px-6 py-10 text-white shadow-xl sm:px-10 md:py-14">
          <div className="fruki-glow absolute -right-12 -top-16 h-64 w-64 rounded-full bg-lime-200/20 blur-2xl" />
          <div className="relative max-w-3xl">
            <p className="mb-3 text-xs font-bold uppercase tracking-[.22em] text-lime-200">Abastecimento B2B Pede Fruki</p>
            <h1 className="text-3xl font-black leading-tight sm:text-5xl">O mix certo para o seu negócio.</h1>
            <p className="mt-4 max-w-2xl text-lg font-medium leading-8 text-white sm:text-xl">Veja o catálogo completo, confira os preços por unidade e monte seus fardos. As sugestões são escolhidas para o perfil da sua loja.</p>
            {context && <div className="mt-6 inline-flex flex-wrap items-center gap-2 rounded-full border border-white/20 bg-white/10 px-4 py-2 text-sm">
              <span>☀️ {context.estacaoDescricao || context.estacao || 'Contexto local'}</span>
              {context.temperatura != null && <span>· {context.temperatura} °C</span>}
              <span>· {nextHoliday(context)?.date ? 'Próximo feriado: ' + new Date(nextHoliday(context).date + 'T00:00:00').toLocaleDateString('pt-BR', { day: 'numeric', month: 'long' }) : 'Calendário nacional consultado'}</span>
            </div>}
          </div>
        </section>

        {pageError && <div role="alert" className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">{pageError}</div>}
        {orderMessage && <div role="status" className="mt-6 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900">{orderMessage}</div>}

        <section id="recomendacoes" className="mt-9 rounded-3xl border border-emerald-100 bg-white p-5 shadow-sm sm:p-7">
          <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
            <div><p className="text-xs font-bold uppercase tracking-widest text-emerald-700">Motor de recomendação</p>
              <h2 className="mt-1 text-2xl font-extrabold text-[#004d2c]">Feito para {clientName}</h2>
              <p className="mt-1 text-sm text-slate-500">Sugestões escolhidas para o perfil da sua loja, prontas para adicionar ao pedido.</p></div>
            <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-800">Perfil: {region || 'personalizado'}</span>
          </div>
          {recommendationMessage && <p className="rounded-xl bg-slate-50 p-4 text-sm text-slate-600">{recommendationMessage}</p>}
          {recommendations.length > 0 && <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {recommendations.map((product) => <ProductCard key={`rec-${product.codItem}`} product={product} quantity={cartItems[product.codItem]?.quantity || 0} onUpdateQuantity={handleUpdateQuantity} recommended />)}
          </div>}
        </section>

        {comboItems.length > 0 && <section className="mt-7 overflow-hidden rounded-3xl border border-amber-200 bg-gradient-to-r from-amber-50 via-white to-lime-50 p-5 shadow-sm sm:p-7">
          <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
            <div className="max-w-xl">
              <span className="rounded-full bg-amber-200 px-3 py-1 text-xs font-bold uppercase tracking-wide text-amber-950">Combo personalizado · {clientName}</span>
              <h2 className="mt-3 text-2xl font-extrabold text-[#173b25]">Combo de reposição recomendado</h2>
              <p className="mt-2 text-base leading-7 text-slate-700">Reúne os itens recomendados para sua loja. O total soma o preço unitário pelo número de unidades de cada fardo.</p>
              <p className="mt-4 text-xl font-black text-emerald-800">{formatMoney(comboPrice)} <span className="text-xs font-medium text-slate-500">soma dos itens, sem desconto aplicado</span></p>
              <button type="button" onClick={addCombo} className="mt-4 rounded-xl bg-[#006633] px-5 py-3 text-sm font-bold text-white shadow transition hover:bg-[#004d26]">Adicionar combo ao pedido</button>
            </div>
            <div className="grid w-full gap-3 sm:grid-cols-3 md:max-w-xl">
              {comboItems.map((product) => <div key={`combo-${product.codItem}`} className="rounded-2xl border border-white bg-white/90 p-3 shadow-sm">
                <img src={product.image} alt="" className="mx-auto h-24 w-full object-contain" />
                <p className="mt-2 line-clamp-2 text-sm font-bold text-slate-800">{product.name}</p>
                <p className="mt-1 text-sm font-extrabold text-emerald-800">Fardo c/ {product.packSize}: {formatMoney(product.price * product.packSize)}</p>
              </div>)}
            </div>
          </div>
        </section>}

        <section className="mt-10">
          <CatalogHeader />
          <div className="mt-5"><SearchBar onSearch={setSearchTerm} /></div>
          <CategoryFilter categories={categories} selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory} />
          <div className="mb-4 flex items-center justify-between text-base font-bold text-white"><span>{loadingProducts ? 'Carregando produtos' + String.fromCharCode(8230) : filteredProducts.length + ' produtos no catálogo'}</span><span>Valores por unidade e por fardo</span></div>
          {loadingProducts ? <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">{Array.from({ length: 8 }, (_, index) => <div key={index} className="h-80 animate-pulse rounded-2xl bg-white" />)}</div>
            : filteredProducts.length ? <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {filteredProducts.map((product) => <ProductCard key={product.codItem || product.id} product={product} quantity={cartItems[product.codItem]?.quantity || 0} onUpdateQuantity={handleUpdateQuantity} />)}
            </div> : <div className="rounded-2xl bg-white p-10 text-center text-slate-500">Nenhum produto corresponde à busca ou categoria.</div>}
        </section>
      </main>

      <FloatingCart totalItems={totalItems} totalPrice={totalPrice} onCheckout={() => setIsCartOpen(true)} />
      <CartModal isOpen={isCartOpen} onClose={() => setIsCartOpen(false)} cartItems={cartItems} onUpdateQuantity={handleUpdateQuantity} onConfirmOrder={finalizarPedido} recommendations={recommendations} submitting={submitting} />
      {profileOpen && <div className="fixed inset-0 z-[70] flex justify-end bg-slate-950/50 backdrop-blur-sm" onMouseDown={(event) => { if (event.target === event.currentTarget) setProfileOpen(false); }}>
        <section role="dialog" aria-modal="true" aria-label="Painel da loja" className="h-full w-full max-w-xl overflow-y-auto bg-[#f7faf6] p-6 shadow-2xl sm:p-8">
          <div className="flex items-start justify-between"><div><p className="text-sm font-bold uppercase tracking-widest text-emerald-800">Perfil identificado</p><h2 className="mt-1 text-3xl font-black text-[#064b2b]">{clientName}</h2><p className="mt-1 text-base font-medium text-slate-700">{cnpjCliente} · {region || 'Cliente Pede Fruki'}</p></div><button type="button" onClick={() => setProfileOpen(false)} aria-label="Fechar perfil" className="rounded-full bg-white px-3 py-1 text-2xl text-slate-600 shadow">×</button></div>
          <div className="mt-7 rounded-2xl bg-[#006633] p-5 text-white"><p className="text-sm text-white/75">Recomendações prontas para adicionar</p><p className="mt-1 text-3xl font-black">{recommendations.length}</p><button onClick={() => { setProfileOpen(false); document.getElementById('recomendacoes')?.scrollIntoView({ behavior: 'smooth' }); }} className="mt-4 rounded-xl bg-white px-4 py-2 text-sm font-bold text-[#006633]">Ver recomendações</button></div>
          <section className="mt-7"><div className="flex items-center gap-2"><h3 className="text-lg font-extrabold text-[#064b2b]">Últimas compras</h3></div><p className="mt-1 text-sm text-slate-500">Pedidos registrados para este CNPJ.</p>
            {historyLoading && <p className="mt-4 rounded-xl bg-white p-4 text-sm text-slate-500">Carregando histórico…</p>}
            {historyError && <p role="alert" className="mt-4 rounded-xl bg-amber-50 p-4 text-sm text-amber-900">Histórico indisponível: {historyError}</p>}
            {!historyLoading && !historyError && purchaseHistory.length === 0 && <p className="mt-4 rounded-xl bg-white p-4 text-sm text-slate-500">Nenhuma compra anterior encontrada.</p>}
            <div className="mt-4 space-y-3">{purchaseHistory.map((purchase) => <article key={purchase.id} className="rounded-2xl border border-emerald-100 bg-white p-4"><div className="flex justify-between gap-3"><strong className="text-sm text-[#064b2b]">{purchase.status || 'Pedido'}</strong><strong className="text-sm text-emerald-800">{formatMoney(purchase.valorTotal)}</strong></div><p className="mt-1 text-xs text-slate-500">{purchase.dataPedido ? new Date(purchase.dataPedido).toLocaleString('pt-BR') : ''}</p><p className="mt-3 text-sm text-slate-700">{purchase.resumoItens || 'Itens do pedido'}</p></article>)}</div>
          </section>
        </section>
      </div>}
    </div>
  );
}
