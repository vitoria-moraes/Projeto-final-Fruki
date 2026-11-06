export default function CategoryFilter({ categories = [], selectedCategory, onCategoryChange }) {
  return (
    <section className="w-full py-5" aria-label="Filtrar por categoria">
      <div className="flex flex-wrap items-end justify-between gap-2">
        <div>
          <h3 className="text-base font-extrabold text-white">Categorias</h3>
          <p className="mt-1 text-xs font-medium text-white/65">Toque em uma categoria para filtrar o portfólio.</p>
        </div>
        <span className="rounded-full border border-white/25 bg-white/10 px-3 py-1 text-[11px] font-bold text-white/85">
          {Math.max(categories.length - 1, 0)} categorias
        </span>
      </div>

      <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-2 hide-scrollbar">
        {categories.map((category) => {
          const isSelected = selectedCategory === category.id;
          const accent = category.accent || '#006633';
          const soft = category.soft || '#e8f6ed';
          return (
            <button
              key={category.id}
              type="button"
              onClick={() => onCategoryChange(category.id)}
              aria-pressed={isSelected}
              style={isSelected ? { backgroundColor: accent, borderColor: accent } : { backgroundColor: 'rgba(255,255,255,.96)', borderColor: soft, color: '#334155' }}
              className="group flex shrink-0 items-center gap-2 rounded-full border px-4 py-2.5 text-sm font-bold shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md"
            >
              <span aria-hidden="true" className="text-base">{category.icon || '🥤'}</span>
              <span>{category.name}</span>
              <span
                className="grid h-5 min-w-5 place-items-center rounded-full px-1 text-[10px] font-black"
                style={isSelected ? { backgroundColor: 'rgba(255,255,255,.22)', color: '#fff' } : { backgroundColor: soft, color: accent }}
              >
                {category.count}
              </span>
            </button>
          );
        })}
      </div>
    </section>
  );
}
