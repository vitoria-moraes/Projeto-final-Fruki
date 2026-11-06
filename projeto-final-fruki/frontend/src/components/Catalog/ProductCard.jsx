import { CATEGORY_META } from '../../data/catalog.js';
import { formatMoney, packageLabel, volumeLabel } from '../../data/catalogBuilder.js';

export default function ProductCard({ product, quantity = 0, onUpdateQuantity, recommended = false }) {
  const {
    name,
    categoryName,
    description,
    price,
    packSize = 6,
    volumeMl,
    image,
    available,
    orderable,
    priceSource,
    featured,
    score,
    motivos = [],
  } = product;

  const meta = CATEGORY_META[categoryName] || { accent: '#006633', soft: '#e8f6ed', icon: '🥤' };
  const unitPrice = Number(price) || 0;
  const packPrice = unitPrice * packSize;
  const isReference = priceSource === 'referencia';

  return (
    <article className="fruki-product group flex h-full flex-col overflow-hidden rounded-[1.4rem] border border-slate-100 bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-xl">
      <div className="fruki-product-media relative flex h-52 items-center justify-center overflow-hidden bg-gradient-to-b from-white to-slate-50 p-5">
        <div className="absolute left-3 top-3 z-10 flex max-w-[calc(100%-1.5rem)] flex-wrap gap-1.5">
          <span className="rounded-full px-2.5 py-1 text-[10px] font-black uppercase tracking-wide shadow-sm" style={{ backgroundColor: meta.soft, color: meta.accent }}>
            {meta.icon} {categoryName}
          </span>
          {recommended && <span className="rounded-full bg-amber-100 px-2.5 py-1 text-[10px] font-black uppercase tracking-wide text-amber-900 shadow-sm">Sugestão</span>}
          {!recommended && featured && <span className="rounded-full bg-lime-100 px-2.5 py-1 text-[10px] font-black uppercase tracking-wide text-lime-900 shadow-sm">Destaque</span>}
        </div>

        <span className={`absolute right-3 top-3 z-10 rounded-full px-2.5 py-1 text-[10px] font-bold ${available ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-500'}`}>
          {available ? 'Disponível' : 'Indisponível'}
        </span>

        <img
          src={image}
          alt={name}
          className="h-full w-full object-contain transition duration-300 group-hover:scale-105"
          loading="lazy"
          onError={(event) => { event.currentTarget.onerror = null; event.currentTarget.src = '/images/brand/produto-placeholder.svg'; }}
        />
      </div>

      <div className="flex flex-grow flex-col gap-3 p-4">
        <div className="flex items-center justify-between gap-2 text-[11px] font-bold uppercase tracking-wide text-slate-500">
          <span>{volumeLabel(volumeMl)} · {packageLabel(product)}</span>
          <span className="shrink-0 rounded-full bg-slate-100 px-2 py-1 text-slate-600">Fardo c/ {packSize}</span>
        </div>

        <div className="flex-grow">
          <h3 className="line-clamp-2 text-lg font-extrabold leading-snug text-[#064b2b]" title={name}>{name}</h3>
          <p className="mt-2 line-clamp-2 text-sm leading-relaxed text-slate-600">{description}</p>

          {recommended && motivos.length > 0 && (
            <ul className="mt-3 space-y-1 text-xs font-medium text-slate-600">
              {motivos.slice(0, 2).map((reason) => (
                <li key={reason} className="flex items-start gap-1.5"><span className="mt-0.5 text-emerald-600">✓</span><span className="line-clamp-1">{reason}</span></li>
              ))}
            </ul>
          )}
        </div>

        <div className="flex items-end justify-between gap-2 rounded-xl bg-slate-50 p-3">
          <div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500">Preço por unidade</p>
            <p className="text-xl font-black leading-tight text-[#064b2b]">{formatMoney(price)}</p>
            <p className="mt-0.5 text-xs font-bold text-emerald-800">Fardo: {formatMoney(packPrice)}</p>
          </div>
          <div className="flex flex-col items-end gap-1">
            {recommended && score != null && <span className="rounded-full bg-emerald-700 px-2 py-1 text-[10px] font-black text-white">Score {Number(score).toFixed(1)}</span>}
            <span className={`rounded-full px-2 py-1 text-[10px] font-bold ${isReference ? 'bg-amber-100 text-amber-900' : 'bg-emerald-100 text-emerald-800'}`}>
              {isReference ? 'Preço ref.' : 'Tabela'}
            </span>
          </div>
        </div>

        {quantity === 0 ? (
          <button
            type="button"
            onClick={() => onUpdateQuantity(product, 1)}
            className="mt-auto flex w-full items-center justify-center gap-2 rounded-xl bg-[#006633] px-4 py-3 text-sm font-extrabold text-white transition hover:bg-[#004d26] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700"
          >
            <span className="text-lg leading-none">+</span>
            {orderable ? 'Adicionar ao pedido' : 'Consultar disponibilidade'}
          </button>
        ) : (
          <div className="mt-auto flex h-11 items-center justify-between overflow-hidden rounded-xl border border-emerald-200 bg-emerald-50">
            <button type="button" aria-label={`Remover um fardo de ${name}`} onClick={() => onUpdateQuantity(product, quantity - 1)} className="h-full w-1/3 text-lg font-bold text-emerald-800 hover:bg-emerald-100">−</button>
            <span className="w-1/3 text-center text-sm font-black text-emerald-900">{quantity} <span className="text-[10px] font-bold uppercase">fardo{quantity > 1 ? 's' : ''}</span></span>
            <button type="button" aria-label={`Adicionar um fardo de ${name}`} onClick={() => onUpdateQuantity(product, quantity + 1)} className="h-full w-1/3 text-lg font-bold text-emerald-800 hover:bg-emerald-100">+</button>
          </div>
        )}
      </div>
    </article>
  );
}
