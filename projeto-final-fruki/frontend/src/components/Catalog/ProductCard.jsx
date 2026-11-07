import { CATEGORY_META } from '../../data/catalog.js';
import { formatMoney } from '../../data/catalogBuilder.js';

export default function ProductCard({ product, quantity = 0, onUpdateQuantity }) {
  const { name, categoryName, description, price, packSize = 6, image } = product;
  const meta = CATEGORY_META[categoryName] || { accent: '#006633', soft: '#e8f6ed', icon: '🥤' };

  return (
    <article className="fruki-product group flex h-full flex-col overflow-hidden rounded-[1.4rem] border border-slate-100 bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-xl">
      <div className="fruki-product-media relative flex h-52 items-center justify-center overflow-hidden bg-gradient-to-b from-white to-slate-50 p-5">
        <span
          className="absolute left-3 top-3 z-10 rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-wide shadow-sm"
          style={{ backgroundColor: meta.soft, color: meta.accent }}
        >
          {meta.icon} {categoryName}
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
        <div className="flex-grow">
          <h3 className="line-clamp-2 text-lg font-bold leading-snug text-[#064b2b]" title={name}>{name}</h3>
          <p className="mt-2 line-clamp-2 text-sm leading-relaxed text-slate-600">{description}</p>
        </div>

        <div className="rounded-xl border border-slate-100 bg-slate-50 p-3">
          <div className="flex items-end justify-between gap-3">
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500">Preço unitário</p>
              <p className="text-xl font-black leading-tight text-[#064b2b]">{formatMoney(price)}</p>
            </div>
            <div className="text-right">
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500">Fardo c/ {packSize}</p>
              <p className="text-sm font-bold text-emerald-800">{formatMoney((Number(price) || 0) * packSize)}</p>
            </div>
          </div>
        </div>

        <div className="mt-auto flex h-11 items-center justify-between overflow-hidden rounded-xl border border-emerald-200 bg-emerald-50">
          <button
            type="button"
            aria-label={`Remover um fardo de ${name}`}
            onClick={() => onUpdateQuantity(product, quantity - 1)}
            disabled={quantity === 0}
            className="h-full w-1/3 text-lg font-bold text-emerald-800 transition hover:bg-emerald-100 disabled:cursor-not-allowed disabled:text-emerald-300 disabled:hover:bg-transparent"
          >
            −
          </button>
          <span className="w-1/3 text-center text-sm font-bold text-emerald-900">
            {quantity} <span className="text-[10px] font-bold uppercase">fardo{quantity === 1 ? '' : 's'}</span>
          </span>
          <button
            type="button"
            aria-label={`Adicionar um fardo de ${name}`}
            onClick={() => onUpdateQuantity(product, quantity + 1)}
            className="h-full w-1/3 text-lg font-bold text-emerald-800 transition hover:bg-emerald-100"
          >
            +
          </button>
        </div>
      </div>
    </article>
  );
}
