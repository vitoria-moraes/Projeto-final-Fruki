import { useEffect, useMemo, useState } from 'react';
import Header from '../components/Header/Header';
import CatalogHeader from '../components/Catalog/CatalogHeader';
import SearchBar from '../components/Catalog/SearchBar';
import CategoryFilter from '../components/Catalog/CategoryFilter';
import ProductCard from '../components/Catalog/ProductCard';
import FloatingCart from '../components/Catalog/FloatingCart';
import CartModal from '../components/Catalog/CartModal';
import Footer from '../components/Footer/Footer';
import {
  createOrder,
  getContext,
  getDemo,
  getProducts,
  getPurchaseHistory,
  getRecommendations,
} from '../api';
import { CATALOG_BY_CODE } from '../data/catalog';
import {
  buildCatalog,
  categoryOptions,
  formatMoney,
  productFromApi,
  sortProducts,
} from '../data/catalogBuilder';
import { normalizeText } from '../utils/productFormatting';

const DEFAULT_CATEGORY = 'todos';
const PRODUCTS_PER_PAGE = 24;

function readClientMeta(name) {
  return document.querySelector(`meta[name="${name}"]`)?.content?.trim() || '';
}

function readStoredCart(cnpj) {
  try {
    const key = `pede-fruki-cart-${cnpj || 'demo'}`;
    const raw = JSON.parse(localStorage.getItem(key) || '[]');
    if (!Array.isArray(raw)) return {};
    return Object.fromEntries(raw.filter((item) => item?.codItem).map((item) => [item.codItem, item]));
  } catch {
    return {};
  }
}

function nextHoliday(context) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return (context?.feriados || [])
    .filter((holiday) => holiday.date && new Date(`${holiday.date}T00:00:00`) >= today)
    .sort((a, b) => a.date.localeCompare(b.date))[0];
}

function contextLabel(context) {
  if (!context) return 'Consultando o calendário da sua região…';
  const parts = [context.estacaoDescricao || context.estacao || 'Contexto local'];
  if (context.temperatura != null) parts.push(`${context.temperatura} °C`);
  const holiday = nextHoliday(context);
  if (holiday?.date) {
    parts.push(`Próximo feriado: ${new Date(`${holiday.date}T00:00:00`).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long' })}`);
  } else {
    parts.push('Calendário nacional consultado');
  }
  return parts.join(' · ');
}

function recommendationProduct(item) {
  const product = productFromApi(item);
  if (!product) return null;
  return {
    ...product,
    score: item.score,
    posicao: item.posicao,
    motivos: item.motivos || [],
    detail: item.detalhe,
  };
}

export default function Catalogo() {
  const clientId = readClientMeta('cliente-id');
  const cnpjCliente = readClientMeta('cnpj-cliente');
  const clientName = readClientMeta('nome-cliente') || 'Lojista';
  const region = readClientMeta('regiao-cliente');
  const regionParts = (region || '').split(' - ');
  const city = readClientMeta('cidade-cliente') || regionParts[0] || 'Porto Alegre';
  const uf = readClientMeta('uf-cliente') || regionParts[1] || 'RS';

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(DEFAULT_CATEGORY);
  const [sortBy, setSortBy] = useState('relevancia');
  const [visibleCount, setVisibleCount] = useState(PRODUCTS_PER_PAGE);
  const [apiProducts, setApiProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [productsWarning, setProductsWarning] = useState('');
  const [reloadToken, setReloadToken] = useState(0);
  const [cartItems, setCartItems] = useState(() => readStoredCart(cnpjCliente));
  const [recommendations, setRecommendations] = useState([]);
  const [recommendationMessage, setRecommendationMessage] = useState('Carregando sugestões para sua loja…');
  const [recommendationLoading, setRecommendationLoading] = useState(Boolean(clientId));
  const [context, setContext] = useState(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [orderMessage, setOrderMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [purchaseHistory, setPurchaseHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');

  const products = useMemo(() => buildCatalog(apiProducts), [apiProducts]);
  const categories = useMemo(() => categoryOptions(products), [products]);

  useEffect(() => {
    let active = true;
    setLoadingProducts(true);
    setProductsWarning('');
    getProducts()
      .then((rows) => {
        if (!active) return;
        setApiProducts(Array.isArray(rows) ? rows : []);
      })
      .catch((error) => {
        if (!active) return;
        setApiProducts([]);
        setProductsWarning(`Não foi possível sincronizar os preços da tabela agora (${error.message}). O catálogo completo segue disponível com os preços de referência.`);
      })
      .finally(() => {
        if (active) setLoadingProducts(false);
      });
    return () => { active = false; };
  }, [reloadToken]);

  useEffect(() => {
    try {
      localStorage.setItem(`pede-fruki-cart-${cnpjCliente || 'demo'}`, JSON.stringify(Object.values(cartItems)));
    } catch {
      // Armazenamento indisponível: o carrinho continua funcionando em memória.
    }
  }, [cartItems, cnpjCliente]);

  useEffect(() => {
    if (!clientId) {
      setRecommendationLoading(false);
      setRecommendationMessage('Acesse pelo link enviado pelo atendimento para ver as sugestões personalizadas da sua loja.');
      return undefined;
    }

    let active = true;
    setRecommendationLoading(true);
    setRecommendationMessage('Carregando sugestões para sua loja…');

    getRecommendations(clientId)
      .then((result) => {
        if (!active) return;
        const items = (result.itens || []).map(recommendationProduct).filter(Boolean);
        setRecommendations(items);
        setRecommendationMessage(items.length ? '' : 'Ainda não há sugestões para exibir.');
        if (result.contexto) setContext(result.contexto);
      })
      .catch((error) => {
        if (!active) return;
        getDemo()
          .then((demo) => {
            if (!active) return;
            const items = (demo.recomendacoes?.itens || []).map(recommendationProduct).filter(Boolean);
            setRecommendations(items);
            setRecommendationMessage(items.length
              ? 'Sugestões demonstrativas carregadas para você conhecer o catálogo.'
              : 'Ainda não há sugestões para exibir.');
          })
          .catch(() => {
            if (!active) return;
            setRecommendationMessage(error.status === 404
              ? 'Não encontramos o histórico desta loja. Fale com o atendimento para liberar as sugestões.'
              : 'O motor de sugestões está indisponível agora. Você pode explorar o catálogo completo abaixo.');
          });
      })
      .finally(() => { if (active) setRecommendationLoading(false); });

    return () => { active = false; };
  }, [clientId]);

  useEffect(() => {
    if (context) return undefined;
    let active = true;
    getContext(city, uf)
      .then((value) => { if (active) setContext(value); })
      .catch(() => {});
    return () => { active = false; };
  }, [city, uf, context]);

  useEffect(() => {
    setVisibleCount(PRODUCTS_PER_PAGE);
  }, [searchTerm, selectedCategory, sortBy]);

  const filteredProducts = useMemo(() => {
    const term = normalizeText(searchTerm);
    const list = products.filter((product) => {
      if (selectedCategory !== DEFAULT_CATEGORY && product.category !== selectedCategory) return false;
      if (!term) return true;
      return normalizeText(`${product.name} ${product.categoryName} ${product.code || ''}`).includes(term);
    });
    return sortProducts(list, sortBy);
  }, [products, searchTerm, selectedCategory, sortBy]);

  const visibleProducts = filteredProducts.slice(0, visibleCount);
  const remainingProducts = Math.max(filteredProducts.length - visibleProducts.length, 0);

  const comboItems = recommendations.filter((product) => product.orderable).slice(0, 3);
  const comboPrice = comboItems.reduce((sum, product) => sum + (product.price || 0) * product.packSize, 0);
  const cartArray = Object.values(cartItems);
  const totalItems = cartArray.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = cartArray.reduce((sum, item) => sum + (item.price || 0) * item.packSize * item.quantity, 0);
  const referenceCount = products.filter((product) => product.priceSource === 'referencia').length;
  const featuredCount = products.filter((product) => product.featured).length;

  const heroShowcase = useMemo(() => ['00001', '00002', '00003']
    .map((code) => products.find((product) => product.code === code) || CATALOG_BY_CODE.get(code))
    .filter(Boolean), [products]);

  const hasFilters = Boolean(searchTerm) || selectedCategory !== DEFAULT_CATEGORY || sortBy !== 'relevancia';
  const catalogHref = '#catalogo';

  function clearFilters() {
    setSearchTerm('');
    setSelectedCategory(DEFAULT_CATEGORY);
    setSortBy('relevancia');
  }

  function handleUpdateQuantity(product, newQuantity) {
    if (!product?.orderable) {
      setOrderMessage(`${product?.name || 'Este produto'} está no catálogo, mas o código e o preço precisam ser confirmados na tabela antes de entrar no pedido.`);
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
    const local = () => {
      try {
        return JSON.parse(localStorage.getItem(key) || '[]');
      } catch {
        return [];
      }
    };
    try {
      const history = cnpjCliente ? await getPurchaseHistory(cnpjCliente) : [];
      setPurchaseHistory([...local(), ...history]);
    } catch (error) {
      if (local().length) setPurchaseHistory(local());
      else {
        try { setPurchaseHistory((await getDemo()).compras || []); }
        catch { setHistoryError(error.message); }
      }
    } finally {
      setHistoryLoading(false);
    }
  }

  async function finalizarPedido() {
    if (!cartArray.length) return;
    setSubmitting(true);
    setOrderMessage('');
    try {
      if (!cnpjCliente) {
        setOrderMessage('Este catálogo está em modo demonstração. Abra o link enviado pelo bot para finalizar o pedido da sua loja.');
        return;
      }
      const demonstracao = cartArray.some((item) => item.demonstracao || item.priceSource === 'referencia' || item.price == null);
      if (demonstracao) {
        const pedido = {
          id: `DEMO-${Date.now()}`,
          dataPedido: new Date().toISOString(),
          status: 'Pedido demonstrativo concluído',
          demonstracao: true,
          valorTotal: totalPrice,
          resumoItens: cartArray.map((item) => `${item.quantity} fardo(s) de ${item.name}`).join(' · '),
        };
        const key = `pede-fruki-demo-orders-${cnpjCliente}`;
        const previous = JSON.parse(localStorage.getItem(key) || '[]');
        localStorage.setItem(key, JSON.stringify([pedido, ...previous]));
        setCartItems({});
        setOrderMessage('Pedido demonstrativo concluído! Itens com preço de referência ficam salvos neste navegador e não são enviados ao ERP até a tabela ser confirmada.');
        setIsCartOpen(false);
        return;
      }
      const result = await createOrder(cnpjCliente, cartArray.map(({ codItem, quantity, packSize }) => ({ codItem, quantidade: quantity * packSize })));
      setCartItems({});
      setOrderMessage(`Pedido ${result.codigoPedido} criado com sucesso. Total confirmado: ${formatMoney(result.valorTotal)}.`);
      setIsCartOpen(false);
    } catch {
      setOrderMessage('Não conseguimos concluir o pedido agora. Confira a conexão e tente novamente em instantes.');
    } finally {
      setSubmitting(false);
    }
  }

  if (!clientId) {
    return (
      <main className="fruki-access min-h-screen grid place-items-center p-6">
        <section className="w-full max-w-xl rounded-[2rem] border border-white/40 bg-white/95 p-10 text-center shadow-2xl backdrop-blur">
          <img src="/images/brand/logo-fruki.svg" alt="Pede Fruki" className="mx-auto mb-6 h-16" />
          <span className="fruki-kicker text-emerald-700">Catálogo B2B</span>
          <h1 className="mt-3 text-3xl font-black text-[#064b2b]">Acesse pelo link do atendimento</h1>
          <p className="mt-3 text-base leading-7 text-slate-600">O catálogo personalizado precisa do link JWT enviado pelo bot do Pede Fruki para carregar sua tabela de preços e suas sugestões.</p>
          <a href="mailto:contato@fruki.com.br" className="fruki-btn-primary mt-7 inline-flex">Falar com o atendimento</a>
        </section>
      </main>
    );
  }

  return (
    <div id="topo" className="fruki-page min-h-screen pb-28 text-slate-800">
      <Header
        userName={clientName}
        region={region}
        onProfileClick={openProfile}
        cartCount={totalItems}
        onCartClick={() => setIsCartOpen(true)}
      />

      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <section className="fruki-hero relative overflow-hidden rounded-[2rem] px-6 py-9 text-white shadow-2xl sm:px-10 md:py-12">
          <div className="fruki-glow absolute -right-16 -top-24 h-80 w-80 rounded-full bg-lime-200/20 blur-3xl" />
          <div className="relative grid items-center gap-10 lg:grid-cols-[minmax(0,1.25fr)_minmax(300px,.75fr)]">
            <div>
              <p className="fruki-kicker text-lime-200">Abastecimento B2B · Pede Fruki</p>
              <h1 className="mt-4 max-w-3xl text-3xl font-black leading-[1.05] tracking-tight sm:text-5xl">
                Todo o portfólio Fruki,
                <span className="block text-lime-200">do jeito que a sua loja precisa.</span>
              </h1>
              <p className="mt-5 max-w-2xl text-base font-medium leading-7 text-white/90 sm:text-lg">
                Refrigerantes, águas, energéticos, cervejas, refrescos, sucos e tônicas em um só catálogo.
                Confira o preço por unidade, monte o fardo e envie o pedido para o atendimento.
              </p>
              <div className="mt-7 flex flex-wrap gap-3">
                <a href={catalogHref} className="fruki-btn-primary">Explorar catálogo</a>
                <a href="#recomendacoes" className="fruki-btn-ghost">Ver sugestões para sua loja</a>
              </div>
              <div className="mt-7 inline-flex max-w-full flex-wrap items-center gap-2 rounded-2xl border border-white/20 bg-white/10 px-4 py-3 text-sm text-white/90 backdrop-blur">
                <span className="text-base">📍</span>
                <span>{region || 'Sua região'}</span>
                <span className="text-white/40">•</span>
                <span>{contextLabel(context)}</span>
              </div>
            </div>

            <div className="fruki-hero-showcase hidden lg:block" aria-hidden="true">
              <div className="fruki-showcase-orbit" />
              {heroShowcase.map((product, index) => (
                <div key={product.code} className={`fruki-showcase-card fruki-showcase-card-${index + 1}`}>
                  <img src={product.image} alt="" />
                </div>
              ))}
              <div className="fruki-showcase-stat">
                <strong>+100</strong>
                <span>opções no portfólio</span>
              </div>
              <div className="fruki-showcase-badge">Pedido mínimo? Fale com o atendimento.</div>
            </div>
          </div>

          <div className="relative mt-8 grid gap-3 sm:grid-cols-3">
            <div className="fruki-stat"><strong>{products.length}</strong><span>produtos no catálogo</span></div>
            <div className="fruki-stat"><strong>{Math.max(categories.length - 1, 0)}</strong><span>categorias para explorar</span></div>
            <div className="fruki-stat"><strong>{featuredCount}</strong><span>destaques de giro</span></div>
          </div>
        </section>

        {productsWarning && (
          <div role="status" className="mt-6 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm text-amber-950 shadow-sm">
            <span>{productsWarning}</span>
            <button type="button" onClick={() => setReloadToken((value) => value + 1)} className="rounded-lg bg-amber-900 px-4 py-2 font-bold text-white hover:bg-amber-800">
              Tentar sincronizar novamente
            </button>
          </div>
        )}
        {orderMessage && (
          <div role="status" className="mt-6 flex items-start justify-between gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm text-emerald-950 shadow-sm">
            <span>{orderMessage}</span>
            <button type="button" onClick={() => setOrderMessage('')} aria-label="Fechar aviso" className="text-lg leading-none text-emerald-800">×</button>
          </div>
        )}

        <section id="recomendacoes" className="fruki-section mt-8 rounded-[1.75rem] p-5 shadow-sm sm:p-7">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="fruki-kicker text-emerald-700">Motor de recomendação</p>
              <h2 className="mt-2 text-2xl font-black text-[#064b2b] sm:text-3xl">Feito para {clientName}</h2>
              <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">Sugestões escolhidas para o perfil da sua loja, com os preços da tabela e os motivos de cada indicação.</p>
            </div>
            <span className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-xs font-bold uppercase tracking-wide text-emerald-800">
              Perfil: {region || 'personalizado'}
            </span>
          </div>

          {recommendationMessage && !recommendationLoading && (
            <p className="mt-5 rounded-xl bg-slate-50 p-4 text-sm text-slate-600">{recommendationMessage}</p>
          )}
          {recommendationLoading && (
            <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 3 }, (_, index) => <div key={index} className="h-80 animate-pulse rounded-2xl bg-slate-100" />)}
            </div>
          )}
          {!recommendationLoading && recommendations.length > 0 && (
            <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {recommendations.map((product) => (
                <ProductCard
                  key={`rec-${product.codItem}`}
                  product={product}
                  quantity={cartItems[product.codItem]?.quantity || 0}
                  onUpdateQuantity={handleUpdateQuantity}
                  recommended
                />
              ))}
            </div>
          )}
        </section>

        {comboItems.length > 0 && (
          <section id="combo" className="fruki-combo mt-7 overflow-hidden rounded-[1.75rem] p-5 shadow-sm sm:p-7">
            <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)] lg:items-center">
              <div>
                <span className="rounded-full bg-amber-200 px-3 py-1 text-[11px] font-black uppercase tracking-wide text-amber-950">Combo personalizado · {clientName}</span>
                <h2 className="mt-3 text-2xl font-black text-[#173b25] sm:text-3xl">Reposição pronta para o seu mix</h2>
                <p className="mt-2 max-w-xl text-sm leading-6 text-slate-700">Reúne os itens recomendados para a sua loja. O valor soma o preço unitário pelo número de unidades de cada fardo, sem desconto aplicado.</p>
                <p className="mt-4 text-2xl font-black text-emerald-800">{formatMoney(comboPrice)} <span className="text-xs font-semibold text-slate-500">soma dos 3 itens</span></p>
                <button type="button" onClick={addCombo} className="fruki-btn-primary mt-4">Adicionar combo ao pedido</button>
              </div>
              <div className="grid gap-3 sm:grid-cols-3">
                {comboItems.map((product) => (
                  <div key={`combo-${product.codItem}`} className="flex flex-col rounded-2xl border border-white bg-white/90 p-3 text-center shadow-sm">
                    <img src={product.image} alt="" className="mx-auto h-24 w-full object-contain" />
                    <p className="mt-2 line-clamp-2 min-h-10 text-sm font-bold text-slate-800">{product.name}</p>
                    <p className="mt-1 text-xs font-extrabold text-emerald-800">Fardo c/ {product.packSize}: {formatMoney(product.price * product.packSize)}</p>
                  </div>
                ))}
              </div>
            </div>
          </section>
        )}

        <section id="catalogo" className="mt-10 scroll-mt-24">
          <CatalogHeader total={products.length} categories={Math.max(categories.length - 1, 0)} />
          <div className="fruki-toolbar mt-5 rounded-2xl border border-white/15 bg-white/10 p-3 backdrop-blur">
            <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px]">
              <SearchBar value={searchTerm} onChange={setSearchTerm} onClear={() => setSearchTerm('')} />
              <label className="flex items-center gap-2 rounded-xl border border-white/30 bg-white px-3 text-sm font-semibold text-slate-700">
                <span className="shrink-0 text-xs uppercase tracking-wide text-slate-500">Ordenar</span>
                <select value={sortBy} onChange={(event) => setSortBy(event.target.value)} className="w-full bg-transparent py-3 outline-none">
                  <option value="relevancia">Destaques primeiro</option>
                  <option value="nome">Nome (A–Z)</option>
                  <option value="menor-preco">Menor preço</option>
                  <option value="maior-preco">Maior preço</option>
                  <option value="categoria">Categoria</option>
                </select>
              </label>
            </div>
          </div>

          <CategoryFilter categories={categories} selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory} />

          <div className="mb-4 flex flex-wrap items-center justify-between gap-3 text-sm font-semibold text-white/90">
            <span>
              {loadingProducts && !apiProducts.length
                ? 'Montando o catálogo completo…'
                : `${filteredProducts.length} ${filteredProducts.length === 1 ? 'produto encontrado' : 'produtos encontrados'}${selectedCategory !== DEFAULT_CATEGORY ? ` em ${categories.find((category) => category.id === selectedCategory)?.name || selectedCategory}` : ''}`}
            </span>
            <div className="flex items-center gap-3">
              <span className="hidden sm:inline">{referenceCount} com preço de referência</span>
              {hasFilters && <button type="button" onClick={clearFilters} className="rounded-full border border-white/40 px-4 py-1.5 text-xs font-bold text-white hover:bg-white/10">Limpar filtros</button>}
            </div>
          </div>

          {visibleProducts.length ? (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {visibleProducts.map((product) => (
                <ProductCard
                  key={product.codItem || product.id}
                  product={product}
                  quantity={cartItems[product.codItem]?.quantity || 0}
                  onUpdateQuantity={handleUpdateQuantity}
                />
              ))}
            </div>
          ) : (
            <div className="rounded-[1.5rem] bg-white p-10 text-center shadow-sm">
              <div className="mx-auto grid h-16 w-16 place-items-center rounded-full bg-emerald-50 text-3xl">🔎</div>
              <h3 className="mt-4 text-xl font-black text-[#064b2b]">Nenhum produto encontrado</h3>
              <p className="mt-2 text-sm text-slate-600">Tente outro termo ou remova os filtros para ver o portfólio completo.</p>
              <button type="button" onClick={clearFilters} className="fruki-btn-primary mt-5">Ver todos os produtos</button>
            </div>
          )}

          {remainingProducts > 0 && (
            <div className="mt-8 text-center">
              <button type="button" onClick={() => setVisibleCount((value) => value + PRODUCTS_PER_PAGE)} className="rounded-full border border-white/50 bg-white/10 px-6 py-3 text-sm font-bold text-white backdrop-blur transition hover:bg-white/20">
                Mostrar mais {Math.min(remainingProducts, PRODUCTS_PER_PAGE)} produtos
              </button>
              <p className="mt-2 text-xs font-medium text-white/70">{visibleProducts.length} de {filteredProducts.length} produtos exibidos</p>
            </div>
          )}
        </section>
      </main>

      <Footer />

      <FloatingCart totalItems={totalItems} totalPrice={totalPrice} onCheckout={() => setIsCartOpen(true)} />
      <CartModal
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        cartItems={cartItems}
        onUpdateQuantity={handleUpdateQuantity}
        onConfirmOrder={finalizarPedido}
        recommendations={recommendations}
        submitting={submitting}
        onClearCart={() => setCartItems({})}
        onContinueShopping={() => { setIsCartOpen(false); document.getElementById('catalogo')?.scrollIntoView({ behavior: 'smooth' }); }}
      />

      {profileOpen && (
        <div className="fixed inset-0 z-[70] flex justify-end bg-slate-950/50 backdrop-blur-sm" onMouseDown={(event) => { if (event.target === event.currentTarget) setProfileOpen(false); }}>
          <section role="dialog" aria-modal="true" aria-label="Painel da loja" className="h-full w-full max-w-xl overflow-y-auto bg-[#f7faf6] p-6 shadow-2xl sm:p-8">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="fruki-kicker text-emerald-800">Perfil identificado</p>
                <h2 className="mt-1 text-3xl font-black text-[#064b2b]">{clientName}</h2>
                <p className="mt-1 text-base font-medium text-slate-700">{cnpjCliente} · {region || 'Cliente Pede Fruki'}</p>
              </div>
              <button type="button" onClick={() => setProfileOpen(false)} aria-label="Fechar perfil" className="rounded-full bg-white px-3 py-1 text-2xl text-slate-600 shadow">×</button>
            </div>

            <div className="mt-7 rounded-2xl bg-[#006633] p-5 text-white">
              <p className="text-sm text-white/75">Sugestões prontas para adicionar</p>
              <p className="mt-1 text-3xl font-black">{recommendations.length}</p>
              <button type="button" onClick={() => { setProfileOpen(false); document.getElementById('recomendacoes')?.scrollIntoView({ behavior: 'smooth' }); }} className="mt-4 rounded-xl bg-white px-4 py-2 text-sm font-bold text-[#006633]">Ver recomendações</button>
            </div>

            <div className="mt-7 grid gap-3 sm:grid-cols-2">
              <div className="rounded-2xl border border-emerald-100 bg-white p-4"><p className="text-xs font-bold uppercase tracking-wide text-slate-500">Itens no catálogo</p><p className="mt-1 text-2xl font-black text-[#064b2b]">{products.length}</p></div>
              <div className="rounded-2xl border border-emerald-100 bg-white p-4"><p className="text-xs font-bold uppercase tracking-wide text-slate-500">No seu pedido</p><p className="mt-1 text-2xl font-black text-[#064b2b]">{totalItems} fardos</p></div>
            </div>

            <section className="mt-7">
              <h3 className="text-lg font-extrabold text-[#064b2b]">Últimas compras</h3>
              <p className="mt-1 text-sm text-slate-500">Pedidos registrados para este CNPJ.</p>
              {historyLoading && <p className="mt-4 rounded-xl bg-white p-4 text-sm text-slate-500">Carregando histórico…</p>}
              {historyError && <p role="alert" className="mt-4 rounded-xl bg-amber-50 p-4 text-sm text-amber-900">Histórico indisponível: {historyError}</p>}
              {!historyLoading && !historyError && purchaseHistory.length === 0 && <p className="mt-4 rounded-xl bg-white p-4 text-sm text-slate-500">Nenhuma compra anterior encontrada.</p>}
              <div className="mt-4 space-y-3">
                {purchaseHistory.map((purchase) => (
                  <article key={purchase.id} className="rounded-2xl border border-emerald-100 bg-white p-4">
                    <div className="flex justify-between gap-3"><strong className="text-sm text-[#064b2b]">{purchase.status || 'Pedido'}</strong><strong className="text-sm text-emerald-800">{formatMoney(purchase.valorTotal)}</strong></div>
                    <p className="mt-1 text-xs text-slate-500">{purchase.dataPedido ? new Date(purchase.dataPedido).toLocaleString('pt-BR') : ''}</p>
                    <p className="mt-3 text-sm text-slate-700">{purchase.resumoItens || 'Itens do pedido'}</p>
                  </article>
                ))}
              </div>
            </section>
          </section>
        </div>
      )}
    </div>
  );
}
