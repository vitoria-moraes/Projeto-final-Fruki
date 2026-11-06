// Front can be opened from IntelliJ (63342) or Vite (5173), while Spring runs on 8080.
const API_ORIGIN = (window.PEDEFRUKI_API_ORIGIN || 'http://localhost:8080').replace(/\/$/, '');
const API = `${API_ORIGIN}/api/v1`;

async function requestJson(path, options = {}) {
  const { headers = {}, ...fetchOptions } = options;
  const response = await fetch(`${API}${path}`, {
    ...fetchOptions,
    headers: { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...headers },
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(body.detail || `Falha na API (HTTP ${response.status})`);
    error.status = response.status;
    error.fields = body.erros || [];
    throw error;
  }
  return body;
}

export const getProducts = () => requestJson('/produtos?limite=200');
export const getPurchaseHistory = (cnpj) => requestJson(`/clientes/${encodeURIComponent(cnpj)}/compras?limite=10`);
export const getRecommendations = (clientId) => requestJson(`/recomendacoes/${encodeURIComponent(clientId)}?limite=8`);
export const getContext = (city, uf) => requestJson(`/contexto?cidade=${encodeURIComponent(city || 'Porto Alegre')}&uf=${encodeURIComponent(uf || 'RS')}`);
export const getDemo = async () => {
  const response = await fetch(`${API_ORIGIN}/mock/demo`, { headers: { Accept: 'application/json' } });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.detail || `Falha na API (HTTP ${response.status})`);
  return body;
};
export const createOrder = (cnpjCliente, items) => requestJson('/pedidos', {
  method: 'POST',
  body: JSON.stringify({ cnpjCliente, itens: items.map(({ codItem, quantidade }) => ({ codigoProduto: codItem, quantidade })) }),
});
