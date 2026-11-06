const produtoExemplo = {
  id: "fruki-lata-350",
  name: "Guaraná Fruki",
  subtitle: "Lata 350ml",
  price: 4.50,
  imageUrl: "https://superrissul.vtexassets.com/arquivos/ids/1109240/Refrigerante-Fruki-Guaran-Lata-350ml.png?v=639259425301400000"
};

export default function Card({ product = produtoExemplo }) {
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  };

  return (
    <div className="w-64 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden flex flex-col font-sans">
      
      <div className="h-48 bg-gray-50 flex items-center justify-center p-4 relative">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="max-h-full object-contain drop-shadow-md"
        />
      </div>

      <div className="p-5 flex flex-col flex-grow">
        
        <div className="mb-4">
          <h3 className="text-lg font-bold text-gray-800 leading-tight">
            {product.name}
          </h3>
          <span className="text-sm text-gray-500">
            {product.subtitle}
          </span>
        </div>

        <div className="mt-auto flex flex-col gap-3">
          <span className="text-2xl font-extrabold text-[#006400]">
            {formatCurrency(product.price)}
          </span>

          <button
            className="w-full bg-[#005c1a] hover:bg-[#004212] text-white font-bold py-2.5 px-4 rounded-md transition-colors duration-200 flex items-center justify-center gap-2"
            onClick={() => console.log(`Produto ${product.id} adicionado!`)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            Adicionar
          </button>
        </div>
        
      </div>
    </div>
  );
}