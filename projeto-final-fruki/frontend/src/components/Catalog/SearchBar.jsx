
import { useState } from "react";

export default function SearchBar({ onSearch }) {
  const [search, setSearch] = useState("");

  function handleSearch(event) {
    event.preventDefault();

    onSearch(search);
  }

  function handleChange(event) {
    const value = event.target.value;

    setSearch(value);
    onSearch(value);
  }

  return (
    <form
      onSubmit={handleSearch}
      className="w-full"
    >
      <div className="flex items-center gap-3 bg-white border border-gray-200 rounded-xl px-4 py-3 transition-all focus-within:border-green-700 focus-within:ring-2 focus-within:ring-green-100">

        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="21"
          height="21"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="text-gray-400 shrink-0"
        >
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.35-4.35" />
        </svg>

        <input
          type="text"
          value={search}
          onChange={handleChange}
          placeholder="Buscar refrigerantes, águas, sucos..."
          className="w-full bg-transparent outline-none text-base text-slate-900 placeholder:text-slate-600"
        />

        <button
          type="submit"
          className="bg-green-700 hover:bg-green-800 text-white p-2 rounded-lg transition-colors shrink-0"
          aria-label="Pesquisar produtos"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="19"
            height="19"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
        </button>

      </div>
    </form>
  );
}
