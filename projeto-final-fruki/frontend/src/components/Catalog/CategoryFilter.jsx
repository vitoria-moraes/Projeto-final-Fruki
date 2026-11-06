
export default function CategoryFilter({
  categories = [],
  selectedCategory,
  onCategoryChange,
}) {
  return (
    <section className="w-full py-5">

      <div className="flex flex-col gap-3">

        <h3 className="text-base font-extrabold text-white">
          Categorias
        </h3>

        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-hide">

          {categories.map((category) => {
            const isSelected =
              selectedCategory === category.id;

            return (
              <button
                key={category.id}
                type="button"
                onClick={() => onCategoryChange(category.id)}
                className={`
                  shrink-0 rounded-full px-5 py-2.5
                  text-base font-semibold transition-all duration-200
                  ${
                    isSelected
                      ? "bg-green-700 text-white shadow-sm"
                      : "bg-white text-gray-600 border border-gray-200 hover:border-green-600 hover:text-green-700"
                  }
                `}
              >
                {category.name}
              </button>
            );
          })}

        </div>

      </div>

    </section>
  );
}
