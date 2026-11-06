const plain = (value = '') => String(value)
  .normalize('NFD')
  .replace(/[\u0300-\u036f]/g, '')
  .toLowerCase()
  .replace(/[^a-z0-9]+/g, ' ')
  .trim();

const CATEGORY_ALIASES = new Map([
  ['agua', 'Águas'], ['aguas', 'Águas'], ['água', 'Águas'], ['águas', 'Águas'],
  ['refri', 'Refrigerantes'], ['refrigerante', 'Refrigerantes'], ['refrigerantes', 'Refrigerantes'],
  ['energetico', 'Energéticos'], ['energeticos', 'Energéticos'], ['energético', 'Energéticos'], ['energéticos', 'Energéticos'],
  ['cerveja', 'Cervejas'], ['cervejas', 'Cervejas'],
  ['tonica', 'Tônicas'], ['tonicas', 'Tônicas'], ['tônica', 'Tônicas'], ['tônicas', 'Tônicas'],
  ['refresco', 'Refrescos'], ['refrescos', 'Refrescos'], ['suco', 'Sucos'], ['sucos', 'Sucos'],
]);

export function canonicalCategory(value = '') {
  const key = plain(value);
  const name = CATEGORY_ALIASES.get(key)
    || (/^aguas?(?:\s|$)/.test(key) ? 'Águas' : null)
    || (/^refri(?:gerantes?)?(?:\s|$)/.test(key) ? 'Refrigerantes' : null)
    || (key ? String(value).trim() : 'Outros');
  return { id: plain(name), name };
}

function formatSize(text) {
  const encodedDecimal = String(text).match(/(\d+)[_-](\d+)\s*l\b/i);
  if (encodedDecimal) return `${encodedDecimal[1]},${encodedDecimal[2]} L`;
  const match = text.match(/(?:^|\s)(\d+(?:[., ]\d+)?)\s*(ml|l)\b/i);
  if (!match) return '';
  const amount = match[1].replace(/\s/g, ',').replace('.', ',');
  return `${amount} ${match[2].toUpperCase()}`;
}

export function formatProductName(value = '', family = '') {
  const source = String(value);
  const raw = String(value).replace(/[_-]+/g, ' ').replace(/\.[^.]+$/, '').trim();
  const key = plain(`${family} ${raw}`);
  const size = formatSize(source) || formatSize(raw) || formatSize(key);
  const suffix = size ? ` ${size}` : '';

  if (/agua/.test(key)) {
    if (/da pedra/.test(key)) return `Água da Pedra${/sem gas/.test(key) ? ' sem gás' : /gas/.test(key) ? ' com gás' : ''}${suffix}`;
    const gas = /sem gas|s gas|zero gas|regular|natural/.test(key) ? ' sem gás' : /com gas|c gas|gaseificada/.test(key) ? ' com gás' : '';
    return `Água Mineral Fruki${gas}${suffix}`;
  }
  if (/bellavista/.test(key)) {
    const style = /ipa/.test(key) ? 'IPA' : /pilsen/.test(key) ? 'Pilsen' : /lager/.test(key) ? 'Lager' : '';
    return `Cerveja Bellavista${style ? ` ${style}` : ''}${suffix}`;
  }
  if (/elev|energet|energy/.test(key)) { const flavor = /coco|acai/.test(key) ? 'Coco com A\u00e7ai' : /frutas tropicais/.test(key) ? 'Frutas Tropicais' : /guarana.*maracuja|maracuja.*guarana/.test(key) ? 'Guaran\u00e1 com Maracuj\u00e1' : /maca verde/.test(key) ? 'Ma\u00e7\u00e3 Verde' : /morango.*melancia|melancia.*morango/.test(key) ? 'Morango com Melancia' : /pink limonade|pink lemonade/.test(key) ? 'Pink Lemonade' : /zero/.test(key) ? 'Zero' : /original/.test(key) ? 'Original' : ''; return 'Energ\u00e9tico Elev' + (flavor ? ' ' + flavor : '') + suffix; }
  if (/guarana/.test(key)) return 'Refrigerante Fruki Guaran\u00e1' + (/zero/.test(key) ? ' Zero' : '') + suffix;
  if (/cola/.test(key)) return `Refrigerante Fruki Cola${suffix}`;
  if (/frukito.*uva|uva.*frukito/.test(key)) return 'Refresco Frukito Uva' + suffix;
  if (/com tem/.test(key)) return 'Suco COM/TEM Laranja' + suffix;
  if (/citrus|frukito/.test(key)) return `Refresco Fruki Citrus${suffix}`;
  if (/tonica/.test(key)) return `Água Tônica Fruki${/zero/.test(key) ? ' Zero' : ''}${suffix}`;
  if (/suco/.test(key)) return `Suco Fruki${suffix}`;

  return raw.replace(/\bfruki\b/gi, 'Fruki').replace(/\bpet\b/gi, '').replace(/\bregular\b/gi, '').replace(/\bfrontal\b/gi, '').replace(/\bgotas\b/gi, '').replace(/\s+/g, ' ').trim()
    .replace(/\bagua\b/gi, 'Água').replace(/\bgas\b/gi, 'Gás').replace(/\bguarana\b/gi, 'Guaraná');
}

export { plain as normalizeText };
