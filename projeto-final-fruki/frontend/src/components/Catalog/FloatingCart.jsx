import { formatMoney } from '../../data/catalogBuilder.js';

export default function FloatingCart({ totalItems, totalPrice, onCheckout }) {
  if (!totalItems) return null;

  return (
    <div className="fixed bottom-0 left-0 z-50 w-full p-3 pb-5 pointer-events-none sm:p-5">
      <div className="pointer-events-auto mx-auto flex max-w-4xl items-center justify-between gap-3 rounded-2xl border border-white/20 bg-[#075b35] p-3 pl-4 text-white shadow-2xl sm:p-4 sm:pl-5">
        <div className="flex min-w-0 items-center gap-3">
          <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-white/10 text-xl" aria-hidden="true">🛒</span>
          <div className="min-w-0">
            <p className="text-[11px] font-bold uppercase tracking-widest text-lime-200">Seu pedido</p>
            <div className="flex flex-wrap items-baseline gap-2">
              <span className="text-xl font-black sm:text-2xl">{formatMoney(totalPrice)}</span>
              <span className="rounded-full bg-white/15 px-2 py-0.5 text-[11px] font-bold">{totalItems} fardo{totalItems > 1 ? 's' : ''}</span>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={onCheckout}
          className="flex shrink-0 items-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-extrabold text-[#075b35] shadow-sm transition hover:bg-lime-50 sm:px-6"
        >
          Revisar pedido
          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  );
}
