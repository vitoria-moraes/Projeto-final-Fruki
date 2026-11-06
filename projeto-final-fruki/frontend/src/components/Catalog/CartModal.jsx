const money = (value) => value == null ? 'Preço sob consulta' : Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function CartModal({ isOpen, onClose, cartItems, onUpdateQuantity, onConfirmOrder, recommendations = [], submitting = false }) {
  if (!isOpen) return null;
  const cart = Object.values(cartItems);
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + (item.price || 0) * (item.packSize || 6) * item.quantity, 0);
  const crossSell = recommendations.filter((product) => product.orderable && !cartItems[product.codItem]).slice(0, 3);

  return (
    <div className="fixed inset-0 z-[60] flex justify-end bg-slate-950/50 backdrop-blur-sm" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <section className="flex h-full w-full max-w-lg flex-col bg-[#f8faf8] shadow-2xl" role="dialog" aria-modal="true" aria-label="Seu pedido">
        <header className="flex items-center justify-between border-b border-slate-100 bg-white p-5">
          <div><p className="text-xs font-bold uppercase tracking-widest text-emerald-700">Pede Fruki</p><h2 className="text-xl font-extrabold text-[#064b2b]">Seu pedido · {totalItems} itens</h2></div>
          <button type="button" onClick={onClose} aria-label="Fechar carrinho" className="rounded-full p-2 text-2xl text-slate-400 hover:bg-slate-100 hover:text-slate-800">×</button>
        </header>
        <div className="flex-1 space-y-3 overflow-y-auto p-5">
          {!cart.length && <p className="py-10 text-center text-sm text-slate-500">Seu carrinho está vazio.</p>}
          {cart.map((item) => <article key={item.codItem} className="flex items-center gap-3 rounded-2xl border border-slate-100 bg-white p-3 shadow-sm">
            <img src={item.image} alt="" className="h-16 w-16 rounded-xl bg-white object-contain" />
            <div className="min-w-0 flex-1"><h3 className="line-clamp-2 text-sm font-bold text-slate-800">{item.name}</h3><p className="mt-1 text-sm font-extrabold text-emerald-800">{money(item.price)} por unidade · fardo c/ {item.packSize || 6}: {money((item.price || 0) * (item.packSize || 6))}</p></div>
            <div className="flex h-9 items-center rounded-lg border border-emerald-100 bg-emerald-50"><button type="button" onClick={() => onUpdateQuantity(item, item.quantity - 1)} className="h-full w-8 font-bold text-emerald-800">−</button><span className="w-7 text-center text-xs font-bold">{item.quantity}</span><button type="button" onClick={() => onUpdateQuantity(item, item.quantity + 1)} className="h-full w-8 font-bold text-emerald-800">＋</button></div>
          </article>)}
          {cart.length > 0 && crossSell.length > 0 && <section className="mt-6 rounded-2xl border border-amber-200 bg-amber-50/70 p-4"><h3 className="text-xs font-extrabold uppercase tracking-wide text-amber-950">Outras recomendações para sua loja</h3><p className="mt-1 text-xs text-amber-900/70">Sugestões do motor com os preços cadastrados.</p><div className="mt-3 space-y-2">{crossSell.map((product) => <div key={product.codItem} className="flex items-center gap-3 rounded-xl bg-white p-2"><img src={product.image} alt="" className="h-12 w-12 object-contain" /><div className="min-w-0 flex-1"><p className="line-clamp-1 text-xs font-bold">{product.name}</p><p className="text-xs font-extrabold text-emerald-800">Fardo c/ {product.packSize || 6}: {money((product.price || 0) * (product.packSize || 6))}</p></div><button type="button" onClick={() => onUpdateQuantity(product, 1)} className="rounded-lg bg-amber-100 px-3 py-2 text-xs font-bold text-amber-950 hover:bg-amber-200">Adicionar</button></div>)}</div></section>}
        </div>
        {cart.length > 0 && <footer className="border-t border-slate-100 bg-white p-5"><div className="mb-1 flex justify-between text-sm text-slate-500"><span>Subtotal estimado</span><span>{money(total)}</span></div><div className="mb-4 flex justify-between text-lg font-black text-[#064b2b]"><span>Total estimado</span><span>{money(total)}</span></div><button type="button" disabled={submitting} onClick={onConfirmOrder} className="w-full rounded-xl bg-[#006633] px-5 py-4 font-extrabold text-white shadow transition hover:bg-[#004d26] disabled:cursor-wait disabled:opacity-60">{submitting ? 'Enviando pedido…' : 'Finalizar pedido'}</button></footer>}
      </section>
    </div>
  );
}
