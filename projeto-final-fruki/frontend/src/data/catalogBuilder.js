import { CATALOG, CATALOG_BY_CODE, CATEGORY_META, CATEGORY_ORDER } from './catalog.js';
import { canonicalCategory, formatProductName, normalizeText } from '../utils/productFormatting.js';

export const PLACEHOLDER_IMAGE = '/images/brand/produto-placeholder.svg';

const STOP_WORDS = new Set([
  'fruki', 'bebida', 'bebidas', 'produto', 'garrafa', 'pet', 'lata', 'fardo',
  'pack', 'com', 'sem', 'de', 'da', 'do', 'e', 'energetico', 'energetica',
  'refrigerante', 'refresco', 'suco', 'agua', 'cerveja', 'tonica', 'citrus',
]);

const clean = (value) => normalizeText(value || '');

export function formatMoney(value) {
  if (value == null || Number.isNaN(Number(value))) return 'Sob consulta';
  return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export function volumeLabel(volumeMl) {
  if (!volumeMl) return '';
  if (volumeMl >= 1000) return `${String(volumeMl / 1000).replace('.', ',')} L`;
  return `${volumeMl} ml`;
}

export function packageLabel(item) {
  const name = clean(item?.name);
  const image = clean(item?.image);
  const category = item?.categoryName || '';
  if (/vidro/.test(name) || /vidro/.test(image)) return 'Garrafa de vidro';
  if (/tetrapack|tetra pack|caixinha/.test(name)) return 'Embalagem tetra pack';
  if (/lata/.test(name) || /lata/.test(image)) return 'Lata';
  if (['Refrigerantes', 'Águas', 'Tônicas'].includes(category) && item?.volumeMl <= 350) return 'Lata';
  if (category === 'Energéticos' && item?.volumeMl <= 500) return 'Lata';
  if (category === 'Cervejas' && item?.volumeMl < 355) return 'Lata';
  if (category === 'Cervejas' && item?.volumeMl === 473) return 'Lata';
  if (item?.volumeMl >= 1000 || /pet/.test(image)) return 'Garrafa PET';
  if (category === 'Cervejas') return 'Garrafa';
  return 'Garrafa';
}

export function descriptionFor(item) {
  const brand = item?.category === 'Águas' ? 'Água da Pedra' : item?.category === 'Cervejas' ? 'Bellavista' : 'Fruki';
  return `${packageLabel(item)} de ${volumeLabel(item?.volumeMl)}. Linha ${brand}, fardo com ${item?.packSize || 6} unidades.`;
}

export function volumeInMl(value = '') {
  const text = String(value).toLowerCase().replace(',', '.');
  const encodedDecimal = text.match(/(\d+)[_-](\d+)\s*l\b/);
  if (encodedDecimal) return Math.round(Number(`${encodedDecimal[1]}.${encodedDecimal[2]}`) * 1000);
  const match = text.match(/(\d+(?:[.,]\d+)?)\s*(ml|l)\b/) || text.match(/(?:pet|_)(\d{3,4})(?:\b|_)/);
  if (!match) return null;
  if (!match[2]) return Number(match[1]);
  const amount = Number(match[1].replace(',', '.'));
  return Math.round(match[2] === 'l' ? amount * 1000 : amount);
}

export function unitsPerPack(name = '') {
  const explicit = String(name).match(/(?:c\/?\s*|com\s+)(\d{1,2})\s*(?:un|unid|unidades)?\b/i);
  if (explicit) return Number(explicit[1]);
  const volume = volumeInMl(name);
  if (!volume) return 6;
  if (volume <= 269) return 12;
  if (volume <= 355) return 12;
  if (volume <= 473) return 6;
  if (volume <= 600) return 12;
  if (volume <= 1000) return 6;
  if (volume <= 2000) return 6;
  if (volume <= 3000) return 6;
  return 4;
}

function tokens(value) {
  return clean(value).split(/[^a-z0-9]+/).filter((word) => word.length > 1 && !STOP_WORDS.has(word));
}

function apiScore(apiItem, candidate) {
  const apiTokens = new Set(tokens(apiItem.nome));
  const candidateTokens = new Set(tokens(`${candidate.name} ${(candidate.aliases || []).join(' ')}`));
  let common = 0;
  for (const token of apiTokens) if (candidateTokens.has(token)) common += 1;
  const apiVolume = volumeInMl(apiItem.nome);
  const volumeMatch = apiVolume && candidate.volumeMl && Math.abs(apiVolume - candidate.volumeMl) < 1;
  const categoryMatch = clean(canonicalCategory(apiItem.categoria).name) === clean(candidate.category);
  return common * 4 + (volumeMatch ? 7 : 0) + (categoryMatch ? 2 : 0) - Math.abs(apiTokens.size - candidateTokens.size) * 0.05;
}

function findCatalogMatch(apiItem) {
  const rawCode = String(apiItem.codItem || '');
  const codeCandidates = [rawCode, rawCode.replace(/^0+/, ''), rawCode.padStart(5, '0')];
  for (const code of codeCandidates) {
    if (CATALOG_BY_CODE.has(code)) return CATALOG_BY_CODE.get(code);
  }
  const apiName = clean(apiItem.nome);
  const byAlias = CATALOG.find((candidate) => {
    const names = [candidate.name, ...(candidate.aliases || [])].map(clean);
    return names.includes(apiName);
  });
  if (byAlias) return byAlias;

  let best = null;
  let bestScore = 0;
  for (const candidate of CATALOG) {
    const score = apiScore(apiItem, candidate);
    if (score > bestScore) {
      best = candidate;
      bestScore = score;
    }
  }
  return bestScore >= 10 ? best : null;
}

function baseProduct(candidate) {
  return {
    id: `ref-${candidate.code}`,
    codItem: `REF-${candidate.code}`,
    code: candidate.code,
    name: candidate.name,
    category: clean(candidate.category),
    categoryName: candidate.category,
    description: descriptionFor(candidate),
    price: candidate.price,
    priceSource: 'referencia',
    packSize: candidate.packSize,
    volumeMl: candidate.volumeMl,
    image: candidate.image,
    available: true,
    orderable: true,
    demonstracao: true,
    featured: Boolean(candidate.featured),
  };
}

export function productFromApi(apiItem) {
  if (!apiItem || apiItem.codItem == null) return null;
  const matched = findCatalogMatch(apiItem);
  const apiPrice = apiItem.preco == null ? null : Number(apiItem.preco);
  const apiCode = String(apiItem.codItem);

  if (matched) {
    return {
      ...baseProduct(matched),
      id: apiCode,
      codItem: apiCode,
      code: matched.code,
      name: matched.name,
      category: clean(matched.category),
      categoryName: matched.category,
      price: apiPrice ?? matched.price,
      priceSource: apiPrice == null ? 'referencia' : 'tabela',
      orderable: Boolean(apiCode),
      demonstracao: apiPrice == null || Boolean(apiItem.demonstracao) || apiCode.startsWith('MOCK-'),
      matchedCode: matched.code,
    };
  }

  const category = canonicalCategory(apiItem.categoria);
  const name = formatProductName(apiItem.nome, apiItem.categoria) || apiItem.nome;
  const volume = volumeInMl(name);
  const image = CATALOG.find((candidate) => clean(candidate.name) === clean(name))?.image || PLACEHOLDER_IMAGE;
  return {
    id: apiCode,
    codItem: apiCode,
    code: null,
    name,
    category: category.id,
    categoryName: category.name,
    description: `${category.name} com preço cadastrado na tabela da sua loja.`,
    price: apiPrice,
    priceSource: 'tabela',
    packSize: unitsPerPack(name),
    volumeMl: volume,
    image,
    available: true,
    orderable: Boolean(apiCode) && apiPrice != null,
    demonstracao: Boolean(apiItem.demonstracao) || apiCode.startsWith('MOCK-'),
    featured: false,
    matchedCode: null,
  };
}

export function buildCatalog(apiItems = []) {
  const usedCodes = new Set();
  const usedImages = new Set();
  const products = [];

  for (const apiItem of apiItems) {
    const product = productFromApi(apiItem);
    if (!product) continue;
    if (product.matchedCode) usedCodes.add(product.matchedCode);
    usedImages.add(product.image);
    products.push(product);
  }

  for (const candidate of CATALOG) {
    if (usedCodes.has(candidate.code) || usedImages.has(candidate.image)) continue;
    products.push(baseProduct(candidate));
  }

  return products.sort((a, b) => {
    if (a.featured !== b.featured) return a.featured ? -1 : 1;
    const categoryDiff = CATEGORY_ORDER.indexOf(a.categoryName) - CATEGORY_ORDER.indexOf(b.categoryName);
    if (categoryDiff !== 0) return categoryDiff;
    return a.name.localeCompare(b.name, 'pt-BR');
  });
}

export function sortProducts(products, sort = 'relevancia') {
  const list = [...products];
  if (sort === 'nome') return list.sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
  if (sort === 'menor-preco') return list.sort((a, b) => (a.price ?? Infinity) - (b.price ?? Infinity));
  if (sort === 'maior-preco') return list.sort((a, b) => (b.price ?? -Infinity) - (a.price ?? -Infinity));
  if (sort === 'categoria') return list.sort((a, b) => (CATEGORY_ORDER.indexOf(a.categoryName) - CATEGORY_ORDER.indexOf(b.categoryName)) || a.name.localeCompare(b.name, 'pt-BR'));
  return list.sort((a, b) => {
    if (a.featured !== b.featured) return a.featured ? -1 : 1;
    return 0;
  });
}

export function categoryOptions(products) {
  const counts = new Map();
  for (const product of products) counts.set(product.categoryName, (counts.get(product.categoryName) || 0) + 1);
  return [
    { id: 'todos', name: 'Todos', count: products.length, accent: '#006633', soft: '#e8f6ed', icon: '✨' },
    ...CATEGORY_ORDER.filter((name) => counts.has(name)).map((name) => ({
      id: clean(name),
      name,
      count: counts.get(name),
      ...CATEGORY_META[name],
    })),
    ...Array.from(counts.keys()).filter((name) => !CATEGORY_ORDER.includes(name)).map((name) => ({
      id: clean(name), name, count: counts.get(name), accent: '#475569', soft: '#f1f5f9', icon: '🥤',
    })),
  ];
}

export function uniqueCatalogSize() {
  return new Set(CATALOG.map((item) => item.code)).size;
}

export { CATEGORY_META, CATEGORY_ORDER };
