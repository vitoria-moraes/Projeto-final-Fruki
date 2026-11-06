export default function SearchBar({ value = '', onChange, onClear }) {
  function handleSubmit(event) {
    event.preventDefault();
  }

  return (
    <form onSubmit={handleSubmit} className="w-full" role="search">
      <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-1.5 transition focus-within:border-emerald-600 focus-within:ring-2 focus-within:ring-emerald-100">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="shrink-0 text-slate-400"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.35-4.35" />
        </svg>

        <input
          type="search"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="Buscar por produto, sabor, marca ou categoria…"
          aria-label="Buscar no catálogo"
          className="w-full bg-transparent py-2 text-base text-slate-900 outline-none placeholder:text-slate-400"
        />

        {value && (
          <button
            type="button"
            onClick={onClear}
            aria-label="Limpar busca"
            className="rounded-full p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>
    </form>
  );
}
