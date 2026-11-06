
import frukiLogo from "../../assets/logo/logo.webp";

export default function Header({ userName, region, onProfileClick }) {
  return (
    <header className="w-full bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-6 py-4">
        <div className="flex items-center justify-between">

          <div className="flex items-center gap-4">
            <img
              src={frukiLogo}
              alt="Pede Fruki"
              className="w-32 h-auto object-contain"
            />
          </div>

          <button type="button" onClick={onProfileClick} aria-label="Abrir painel do cliente" className="flex items-center gap-3 rounded-xl px-2 py-1 text-left transition hover:bg-emerald-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-emerald-700">
            <div className="block text-right">
              <p>
              <span className="text-base font-extrabold text-[#064b2b]">{userName || "Minha Conta"}</span>
              </p>

              <p className="text-sm font-medium text-slate-700">
                {region || 'Bem-vindo ao catálogo Fruki'}
              </p>
            </div>

            <div className="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="21"
                height="21"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#007500"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="12" cy="8" r="5" />
                <path d="M20 21a8 8 0 0 0-16 0" />
              </svg>
            </div>
          </button>

        </div>
      </div>

      <div className="h-1 bg-[#006633]" />
    </header>
  );
}
