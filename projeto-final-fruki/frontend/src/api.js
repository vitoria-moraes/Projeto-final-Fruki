// O catálogo é servido pelo próprio Spring em produção. Em Vite/IntelliJ, a API roda em 8080.
const isDevServer = ['5173', '63342'].includes(window.location.port) || window.location.protocol === 'file:';
const API_ORIGIN = (window.PEDEFRUKI_API_ORIGIN || (isDevServer ? 'http://localhost:8080' : window.location.origin)).replace(/\/$/, '');
const API = `${API_ORIGIN}/api/v1`;

async function requestJson(path, options = {}, timeoutMs = 15000) {
  const { headers = {}, ...fetchOptions } = options;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetch(`${API}${path}`, {
      ...fetchOptions,
      signal: controller.signal,
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
  } catch (error) {
    if (error.name === 'AbortError') {
      const timeout = new Error('A API demorou demais para responder. O catálogo segue disponível.');
      timeout.status = 0;
      throw timeout;
    }
    throw error;
  } finally {
    clearTimeout(timer);
  }
}

export const getProducts = () => requestJson('/produtos?limite=200', {}, 12000);
export const getPurchaseHistory = (cnpj) => requestJson(`/clientes/${encodeURIComponent(cnpj)}/compras?limite=10`, {}, 12000);
export const getRecommendations = (clientId) => requestJson(`/recomendacoes/${encodeURIComponent(clientId)}?limite=8`, {}, 15000);
export const getContext = (city, uf) => requestJson(`/contexto?cidade=${encodeURIComponent(city || 'Porto Alegre')}&uf=${encodeURIComponent(uf || 'RS')}`, {}, 12000);
export const getDemo = async () => {
  const response = await fetch(`${API_ORIGIN}/mock/demo`, { headers: { Accept: 'application/json' }, signal: AbortSignal.timeout(12000) });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.detail || `Falha na API (HTTP ${response.status})`);
  return body;
};
export const createOrder = (cnpjCliente, items) => requestJson('/pedidos', {
  method: 'POST',
  body: JSON.stringify({ cnpjCliente, itens: items.map(({ codItem, quantidade }) => ({ codigoProduto: codItem, quantidade })) }),
});
