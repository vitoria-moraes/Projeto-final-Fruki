import frukiLogo from '../../assets/logo/logo.webp';

export default function Header({ userName, region, onProfileClick, cartCount = 0, onCartClick }) {
  return (
    <header className="sticky top-0 z-40 w-full border-b border-emerald-100 bg-white/95 shadow-sm backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-6">
          <a href="#topo" className="flex shrink-0 items-center" aria-label="Pede Fruki — início">
            <img src={frukiLogo} alt="Pede Fruki" className="h-11 w-auto object-contain sm:h-12" />
          </a>
          <nav className="hidden items-center gap-1 lg:flex" aria-label="Navegação do catálogo">
            <a href="#catalogo" className="rounded-lg px-3 py-2 text-sm font-bold text-slate-600 transition hover:bg-emerald-50 hover:text-[#006633]">Catálogo</a>
            <a href="#recomendacoes" className="rounded-lg px-3 py-2 text-sm font-bold text-slate-600 transition hover:bg-emerald-50 hover:text-[#006633]">Sugestões</a>
            <a href="#combo" className="rounded-lg px-3 py-2 text-sm font-bold text-slate-600 transition hover:bg-emerald-50 hover:text-[#006633]">Combo</a>
          </nav>
        </div>

        <div className="flex min-w-0 items-center gap-2 sm:gap-3">
          <span className="hidden max-w-52 items-center gap-2 truncate rounded-full border border-emerald-100 bg-emerald-50/70 px-3 py-2 text-xs font-bold text-emerald-900 xl:flex">
            <span aria-hidden="true">📍</span>
            <span className="truncate">{region || 'Sua região'}</span>
          </span>

          <button
            type="button"
            onClick={onCartClick}
            aria-label={`Abrir pedido com ${cartCount} fardo${cartCount === 1 ? '' : 's'}`}
            className="relative flex h-10 items-center gap-2 rounded-xl border border-emerald-200 bg-white px-3 text-sm font-extrabold text-[#006633] transition hover:border-emerald-400 hover:bg-emerald-50"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <circle cx="9" cy="21" r="1" />
              <circle cx="20" cy="21" r="1" />
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
            </svg>
            <span className="hidden sm:inline">Meu pedido</span>
            {cartCount > 0 && (
              <span className="absolute -right-2 -top-2 grid h-6 min-w-6 place-items-center rounded-full bg-[#e31b23] px-1 text-[11px] font-black text-white shadow">
                {cartCount > 99 ? '99+' : cartCount}
              </span>
            )}
          </button>

          <button
            type="button"
            onClick={onProfileClick}
            aria-label="Abrir painel da loja"
            className="flex max-w-64 items-center gap-3 rounded-xl border border-transparent px-2 py-1 text-left transition hover:border-emerald-100 hover:bg-emerald-50"
          >
            <div className="hidden min-w-0 text-right sm:block">
              <p className="truncate text-[17px] font-bold leading-6 text-[#064b2b]">{userName || 'Minha conta'}</p>
              <p className="truncate text-[15px] font-medium leading-5 text-slate-500">{region || 'Cliente Pede Fruki'}</p>
            </div>
            <span className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-emerald-100 text-emerald-800">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <circle cx="12" cy="8" r="5" />
                <path d="M20 21a8 8 0 0 0-16 0" />
              </svg>
            </span>
          </button>
        </div>
      </div>
      <div className="h-1 bg-gradient-to-r from-[#006633] via-[#7acc00] to-[#006633]" />
    </header>
  );
}
