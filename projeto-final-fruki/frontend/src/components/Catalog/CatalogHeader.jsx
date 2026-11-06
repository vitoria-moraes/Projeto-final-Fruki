export default function CatalogHeader({ total = 0, categories = 0 }) {
  return (
    <section className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <span className="fruki-kicker text-lime-200">Pede Fruki · Catálogo completo</span>
        <h2 className="mt-2 text-3xl font-black leading-tight text-white sm:text-4xl">Nossos produtos</h2>
        <p className="mt-2 max-w-2xl text-sm font-medium leading-6 text-white/80 sm:text-base">
          Explore o portfólio por categoria, marca ou sabor. Confira o preço por unidade, o conteúdo do fardo e monte o pedido da sua loja.
        </p>
      </div>
      <div className="flex shrink-0 flex-wrap gap-2">
        <span className="rounded-full border border-white/25 bg-white/10 px-4 py-2 text-xs font-bold text-white backdrop-blur">{total} produtos</span>
        <span className="rounded-full border border-white/25 bg-white/10 px-4 py-2 text-xs font-bold text-white backdrop-blur">{categories} categorias</span>
      </div>
    </section>
  );
}
