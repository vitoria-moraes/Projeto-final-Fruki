export default function Footer() {
  return (
    <footer className="fruki-footer mt-16">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-9 md:grid-cols-2 lg:grid-cols-4">
          <div>
            <img src="/images/brand/logo-fruki.svg" alt="Pede Fruki" className="h-12 w-auto rounded-lg bg-white p-2" />
            <p className="mt-4 max-w-xs text-sm leading-6 text-white/70">
              O portal B2B da Fruki Bebidas para abastecer sua loja com o mix certo, na medida certa.
            </p>
            <p className="mt-4 text-xs font-semibold uppercase tracking-widest text-lime-200">Fruki Bebidas S.A.</p>
            <p className="mt-1 text-xs text-white/55">CNPJ 87.315.099/0001-07</p>
          </div>

          <div>
            <h3 className="text-sm font-extrabold uppercase tracking-widest text-white">Portfólio</h3>
            <ul className="mt-4 space-y-2 text-sm text-white/70">
              <li>Refrigerantes Fruki</li>
              <li>Água da Pedra</li>
              <li>Energético Elev</li>
              <li>Cervejaria Bellavista</li>
              <li>Refrescos Frukito</li>
              <li>Sucos COM/TEM</li>
              <li>Tônicas e Citrus</li>
            </ul>
          </div>

          <div>
            <h3 className="text-sm font-extrabold uppercase tracking-widest text-white">Atendimento</h3>
            <ul className="mt-4 space-y-3 text-sm text-white/70">
              <li><span className="mr-2">📞</span>0800 703 9910</li>
              <li><span className="mr-2">✉️</span>contato@fruki.com.br</li>
              <li><span className="mr-2">🕘</span>Segunda a sexta, 9h às 12h e 13h às 17h</li>
              <li><span className="mr-2">📍</span>Lajeado · Rio Grande do Sul</li>
            </ul>
          </div>

          <div>
            <h3 className="text-sm font-extrabold uppercase tracking-widest text-white">Compra B2B</h3>
            <ul className="mt-4 space-y-2 text-sm text-white/70">
              <li>Preço por unidade e por fardo</li>
              <li>Sugestões pelo perfil da loja</li>
              <li>Pedido enviado ao atendimento</li>
              <li>Histórico de compras no perfil</li>
            </ul>
            <div className="mt-5 rounded-2xl border border-white/15 bg-white/5 p-4 text-xs leading-5 text-white/65">
              Os preços de referência são uma prévia do catálogo. A tabela da sua região é confirmada pelo atendimento no fechamento do pedido.
            </div>
          </div>
        </div>

        <div className="mt-10 flex flex-col gap-3 border-t border-white/15 pt-6 text-xs text-white/55 sm:flex-row sm:items-center sm:justify-between">
          <p>© 2026 Fruki Bebidas S.A. Todos os direitos reservados.</p>
          <p>Ecossistema B2B Pede Fruki · Catálogo demonstrativo integrado à API.</p>
        </div>
      </div>
    </footer>
  );
}
