export default function CatalogHeader() {
  return (
    <section className="w-full py-8">
      <div className="flex flex-col gap-3">

        <span className="text-xs font-bold tracking-widest text-lime-200 uppercase">
          Pede Fruki · Catálogo
        </span>

        <h2 className="text-3xl md:text-4xl font-extrabold text-white">
          Nossos produtos
        </h2>

        <p className="text-base md:text-lg font-medium text-white max-w-2xl">
          Explore nossa variedade de bebidas e encontre seus
          produtos favoritos.
        </p>

        <div className="w-16 h-1.5 bg-[#A9DF38] rounded-full mt-2" />

      </div>
    </section>
  );
}