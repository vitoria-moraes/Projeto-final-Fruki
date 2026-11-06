const money = (value) => value == null || Number.isNaN(Number(value))
  ? 'Preço sob consulta'
  : Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function ProductCard({ product, quantity = 0, onUpdateQuantity, recommended = false }) {
  const { name, categoryName, description, price, image, available } = product;
  return (
    <article className="group flex h-full flex-col overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-lg">
      <div className="relative flex h-52 items-center justify-center bg-white p-5">
          {recommended && <span className="absolute left-3 top-3 rounded-full bg-amber-100 px-3 py-1 text-xs font-bold text-amber-900">#{product.posicao} · score {Number(product.score).toFixed(1)}{product.demonstracao ? ' · demo' : ''}</span>}
        <span className={`absolute right-3 top-3 rounded-full px-3 py-1 text-xs font-semibold ${available ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-500'}`}>
          {product.demonstracao ? 'Exemplo demonstrativo' : available ? 'Disponível' : 'Indisponível'}
        </span>
        <img src={image} alt={name} className="h-full w-full object-contain" loading="lazy" onError={(event) => { event.currentTarget.onerror = null; event.currentTarget.src = '/images/brand/produto-placeholder.svg'; }} />
      </div>
      <div className="flex flex-grow flex-col gap-3 p-4">
        <span className="w-fit rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-bold uppercase tracking-wide text-emerald-800">{categoryName}</span>
        <div className="flex-grow">
          <h3 className="line-clamp-2 text-lg font-extrabold leading-snug text-[#064b2b]">{name}</h3>
          <p className="mt-2 text-sm leading-relaxed text-slate-700">{description}</p>
          {recommended && product.motivos?.length > 0 && <ul className="mt-3 space-y-1 text-sm text-slate-700">{product.motivos.slice(0, 3).map((reason) => <li key={reason}>• {reason}</li>)}</ul>}
        </div>
        <div className="flex items-center justify-between border-t border-slate-100 pt-3">
          <div><p className="text-xs font-bold uppercase tracking-wide text-slate-600">Preço unitário</p><p className="text-lg font-black text-emerald-900">{money(price)}</p></div>
          <span className="text-xs font-medium text-slate-600">{price == null ? 'Pre\u00e7o sob consulta' : 'Pre\u00e7o cadastrado'}</span>
        </div>
        {quantity === 0 ? <button type="button" onClick={() => onUpdateQuantity(product, 1)} className="mt-auto flex w-full items-center justify-center gap-2 rounded-xl bg-[#006633] px-4 py-3 text-base font-extrabold text-white transition hover:bg-[#004d26] ">+ Adicionar ao carrinho</button>
          : <div className="mt-auto flex h-11 items-center justify-between overflow-hidden rounded-xl border border-emerald-200 bg-emerald-50"><button type="button" aria-label={`Remover ${name}`} onClick={() => onUpdateQuantity(product, quantity - 1)} className="h-full w-1/3 font-bold text-emerald-800 hover:bg-emerald-100">−</button><span className="w-1/3 text-center text-sm font-extrabold">{quantity}</span><button type="button" aria-label={`Adicionar ${name}`} onClick={() => onUpdateQuantity(product, quantity + 1)} className="h-full w-1/3 font-bold text-emerald-800 hover:bg-emerald-100">＋</button></div>}
      </div>
    </article>
  );
}
