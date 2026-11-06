import React from 'react';

export default function FloatingCart({ totalItems, totalPrice, onCheckout }) {
  if (totalItems === 0) return null;

  const formattedTotal = totalPrice.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });

  return (
    <div className="fixed bottom-0 left-0 w-full z-50 p-4 pb-6 pointer-events-none">
      <div className="max-w-4xl mx-auto bg-green-800 rounded-2xl shadow-2xl p-4 flex items-center justify-between pointer-events-auto transform transition-transform duration-300">
        <div className="flex flex-col text-white">
          <span className="text-sm text-green-200 font-medium">Total do Pedido</span>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold">{formattedTotal}</span>
            <span className="text-sm bg-green-700 px-2 py-0.5 rounded-full">{totalItems} fardos</span>
          </div>
        </div>
        
        <button 
          onClick={onCheckout}
          className="bg-white text-green-800 font-bold py-3 px-6 rounded-xl hover:bg-gray-100 transition-colors flex items-center gap-2 shadow-sm"
        >
          Fechar Pedido
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  );
}