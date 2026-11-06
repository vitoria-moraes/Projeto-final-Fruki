import { formatMoney } from '../../data/catalogBuilder.js';

export default function CartModal({
  isOpen,
  onClose,
  cartItems,
  onUpdateQuantity,
  onConfirmOrder,
  recommendations = [],
  submitting = false,
  onClearCart,
  onContinueShopping,
}) {
  if (!isOpen) return null;
  const cart = Object.values(cartItems);
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + (item.price || 0) * (item.packSize || 6) * item.quantity, 0);
  const hasReference = cart.some((item) => item.priceSource === 'referencia' || item.demonstracao);
  const crossSell = recommendations.filter((product) => product.orderable && !cartItems[product.codItem]).slice(0, 3);

  return (
    <div
      className="fixed inset-0 z-[60] flex justify-end bg-slate-950/50 backdrop-blur-sm"
      role="presentation"
      onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}
    >
      <section className="flex h-full w-full max-w-xl flex-col bg-[#f8faf8] shadow-2xl" role="dialog" aria-modal="true" aria-label="Seu pedido">
        <header className="flex items-center justify-between border-b border-slate-100 bg-white p-5 sm:p-6">
          <div>
            <p className="fruki-kicker text-emerald-700">Pede Fruki · Pedido</p>
            <h2 className="mt-1 text-xl font-black text-[#064b2b]">Seu pedido · {totalItems} fardo{totalItems === 1 ? '' : 's'}</h2>
          </div>
          <button type="button" onClick={onClose} aria-label="Fechar carrinho" className="rounded-full p-2 text-2xl text-slate-400 transition hover:bg-slate-100 hover:text-slate-800">×</button>
        </header>

        <div className="flex-1 space-y-3 overflow-y-auto p-5 sm:p-6">
          {!cart.length && (
            <div className="grid place-items-center py-16 text-center">
              <div className="grid h-16 w-16 place-items-center rounded-full bg-emerald-50 text-3xl">🛒</div>
              <h3 className="mt-4 text-lg font-black text-[#064b2b]">Seu carrinho está vazio</h3>
              <p className="mt-1 max-w-xs text-sm text-slate-500">Explore o catálogo e adicione os fardos que combinam com a sua loja.</p>
              <button type="button" onClick={onContinueShopping || onClose} className="fruki-btn-primary mt-5">Ver catálogo</button>
            </div>
          )}

          {cart.map((item) => {
            const lineTotal = (item.price || 0) * (item.packSize || 6) * item.quantity;
            return (
              <article key={item.codItem} className="flex gap-3 rounded-2xl border border-slate-100 bg-white p-3 shadow-sm">
                <img src={item.image} alt="" className="h-20 w-20 shrink-0 rounded-xl bg-white object-contain" />
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="line-clamp-2 text-sm font-extrabold text-slate-800">{item.name}</h3>
                    <strong className="shrink-0 text-sm font-black text-emerald-800">{formatMoney(lineTotal)}</strong>
                  </div>
                  <p className="mt-1 text-xs font-medium text-slate-500">
                    {formatMoney(item.price)} / unidade · fardo c/ {item.packSize || 6} unidades
                  </p>
                  <p className="text-xs font-bold text-slate-700">Fardo: {formatMoney((item.price || 0) * (item.packSize || 6))}</p>
                  <div className="mt-2 flex items-center justify-between gap-2">
                    <span className={`rounded-full px-2 py-1 text-[10px] font-bold ${item.priceSource === 'referencia' ? 'bg-amber-100 text-amber-900' : 'bg-emerald-50 text-emerald-800'}`}>
                      {item.priceSource === 'referencia' ? 'Preço de referência' : 'Preço da tabela'}
                    </span>
                    <div className="flex h-8 items-center overflow-hidden rounded-lg border border-emerald-100 bg-emerald-50">
                      <button type="button" onClick={() => onUpdateQuantity(item, item.quantity - 1)} aria-label={`Remover um fardo de ${item.name}`} className="h-full w-8 font-bold text-emerald-800 hover:bg-emerald-100">−</button>
                      <span className="w-8 text-center text-xs font-black text-emerald-900">{item.quantity}</span>
                      <button type="button" onClick={() => onUpdateQuantity(item, item.quantity + 1)} aria-label={`Adicionar um fardo de ${item.name}`} className="h-full w-8 font-bold text-emerald-800 hover:bg-emerald-100">+</button>
                    </div>
                  </div>
                </div>
              </article>
            );
          })}

          {cart.length > 0 && crossSell.length > 0 && (
            <section className="mt-5 rounded-2xl border border-amber-200 bg-amber-50/70 p-4">
              <h3 className="text-xs font-black uppercase tracking-wide text-amber-950">Outras sugestões para sua loja</h3>
              <p className="mt-1 text-xs text-amber-900/70">Itens recomendados que ainda não estão no pedido.</p>
              <div className="mt-3 space-y-2">
                {crossSell.map((product) => (
                  <div key={product.codItem} className="flex items-center gap-3 rounded-xl bg-white p-2">
                    <img src={product.image} alt="" className="h-12 w-12 object-contain" />
                    <div className="min-w-0 flex-1">
                      <p className="line-clamp-1 text-xs font-bold">{product.name}</p>
                      <p className="text-xs font-extrabold text-emerald-800">Fardo c/ {product.packSize}: {formatMoney((product.price || 0) * (product.packSize || 6))}</p>
                    </div>
                    <button type="button" onClick={() => onUpdateQuantity(product, 1)} className="rounded-lg bg-amber-100 px-3 py-2 text-xs font-bold text-amber-950 transition hover:bg-amber-200">Adicionar</button>
                  </div>
                ))}
              </div>
            </section>
          )}
        </div>

        {cart.length > 0 && (
          <footer className="border-t border-slate-100 bg-white p-5 sm:p-6">
            {hasReference && (
              <p className="mb-3 rounded-xl bg-amber-50 p-3 text-xs leading-5 text-amber-900">
                Este pedido inclui itens com preço de referência. Eles ficam salvos como pedido demonstrativo até a tabela da sua região ser confirmada pelo atendimento.
              </p>
            )}
            <div className="mb-1 flex justify-between text-sm text-slate-500"><span>Fardos</span><span>{totalItems}</span></div>
            <div className="mb-1 flex justify-between text-sm text-slate-500"><span>Subtotal estimado</span><span>{formatMoney(total)}</span></div>
            <div className="mb-4 flex items-end justify-between gap-3">
              <span className="text-lg font-black text-[#064b2b]">Total estimado</span>
              <span className="text-2xl font-black text-emerald-800">{formatMoney(total)}</span>
            </div>
            <div className="flex flex-col gap-2 sm:flex-row">
              <button type="button" onClick={onClearCart} className="rounded-xl border border-slate-200 px-4 py-3 text-sm font-bold text-slate-600 transition hover:bg-slate-50">Limpar</button>
              <button type="button" disabled={submitting} onClick={onConfirmOrder} className="flex-1 rounded-xl bg-[#006633] px-5 py-3.5 text-sm font-extrabold text-white shadow transition hover:bg-[#004d26] disabled:cursor-wait disabled:opacity-60">
                {submitting ? 'Enviando pedido…' : 'Finalizar pedido'}
              </button>
            </div>
            <p className="mt-3 text-center text-[11px] leading-5 text-slate-400">O total oficial é confirmado pelo atendimento com a tabela de preços da sua região.</p>
          </footer>
        )}
      </section>
    </div>
  );
}
