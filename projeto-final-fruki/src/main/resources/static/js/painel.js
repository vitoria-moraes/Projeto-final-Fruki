/* =====================================================================
   Pede Fruki — painel de testes (JavaScript puro, sem build)
   ===================================================================== */
(function () {
    'use strict';

    const $ = (id) => document.getElementById(id);
    const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));
    const esc = (v) => String(v == null ? '' : v).replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
    const brl = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const hoje = () => {
        const d = new Date();
        const mes = String(d.getMonth() + 1).padStart(2, '0');
        const dia = String(d.getDate()).padStart(2, '0');
        return `${d.getFullYear()}-${mes}-${dia}`;
    };
    const load = (k, d) => { try { const r = localStorage.getItem(k); return r ? JSON.parse(r) : d; } catch (e) { return d; } };
    const save = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)); } catch (e) { /* modo privado */ } };

    const state = {
        cart: load('pedeFruki.painel.cart', {}) || {},
        produtos: [],
        lastRec: null,
        clienteId: load('pedeFruki.painel.cliente', 41) || 41,
        indLoaded: false,
        ctxLoaded: false
    };

    function toast(msg) {
        const t = $('toast');
        t.textContent = msg;
        t.classList.add('show');
        clearTimeout(toast._t);
        toast._t = setTimeout(() => t.classList.remove('show'), 2600);
    }

    function marcarStatus(ok, texto) {
        const el = $('apiStatus');
        el.textContent = texto;
        el.classList.toggle('ok', !!ok);
        el.classList.toggle('err', !ok);
    }

    async function api(path, options) {
        const opts = Object.assign({}, options || {});
        const headers = Object.assign({ 'Accept': 'application/json' }, opts.headers || {});
        if (opts.body && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }
        opts.headers = headers;

        let res;
        try {
            res = await fetch(path, opts);
        } catch (e) {
            marcarStatus(false, 'API offline');
            const err = new Error('Não foi possível falar com a API. Ela está rodando?');
            err.status = 0;
            err.data = { detail: err.message };
            throw err;
        }

        const texto = await res.text();
        let data = texto;
        try { data = texto ? JSON.parse(texto) : null; } catch (e) { /* HTML/texto */ }

        if (!res.ok) {
            marcarStatus(res.status < 500, 'API ' + res.status);
            const msg = (data && (data.detail || data.title || data.message)) || ('HTTP ' + res.status);
            const err = new Error(msg);
            err.status = res.status;
            err.data = data;
            throw err;
        }

        marcarStatus(true, 'API online');
        return data;
    }

    function mostrarErro(container, err) {
        const corpo = typeof err.data === 'string'
            ? err.data
            : JSON.stringify(err.data == null ? {} : err.data, null, 2);
        container.innerHTML = `
            <div class="error-box">
                <b>HTTP ${esc(err.status || '—')} · ${esc(err.message)}</b>
                <div>Veja o corpo do erro abaixo. Em 400/404/409/503 ele deve ser um ProblemDetail.</div>
            </div>
            <pre class="json">${esc(corpo)}</pre>`;
    }

    function mostrarJson(container, data) {
        container.innerHTML = `<pre class="json">${esc(JSON.stringify(data, null, 2))}</pre>`;
    }

    async function comBotao(btn, rotulo, fn) {
        const original = btn.textContent;
        btn.disabled = true;
        btn.textContent = rotulo;
        try { await fn(); } finally { btn.disabled = false; btn.textContent = original; }
    }

    /* ---------------------------------------------------------------- abas */
    function go(nome) {
        $$('.tab').forEach((t) => t.classList.toggle('active', t.dataset.tab === nome));
        $$('.panel').forEach((p) => p.classList.toggle('active', p.id === 'tab-' + nome));
        if (nome === 'catalogo' && !state.produtos.length) carregarProdutos();
        if (nome === 'indicadores' && !state.indLoaded) carregarIndicadores();
        if (nome === 'contexto' && !state.ctxLoaded) carregarContexto();
        const painel = $('tab-' + nome);
        if (painel) painel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    /* ---------------------------------------------------------------- recomendação */
    async function carregarRecomendacao() {
        const id = Number($('recId').value);
        const limite = Math.max(1, Math.min(50, Number($('recLimite').value) || 10));
        if (!id || id <= 0) { toast('Informe um idCliente válido.'); return; }
        state.clienteId = id;
        $('clienteAtual').value = id;
        save('pedeFruki.painel.cliente', id);

        await comBotao($('btnRec'), 'Consultando...', async () => {
            try {
                const data = await api(`/api/v1/recomendacoes/${id}?limite=${limite}`);
                state.lastRec = data;
                renderRecomendacao(data);
            } catch (e) {
                state.lastRec = null;
                mostrarErro($('recResult'), e);
            }
        });
    }

    function renderRecomendacao(data) {
        const itens = data.itens || [];
        const ctx = data.contexto || {};
        const chips = [
            `<span class="pill">${esc(data.perfil || 'perfil')}</span>`,
            data.cidade ? `<span class="pill">${esc(data.cidade)}/${esc(data.uf)}</span>` : '',
            `<span class="pill">${itens.length} itens</span>`,
            ctx.feriadoProximo ? '<span class="pill warm">feriado próximo</span>' : ''
        ].join('');

        const cards = itens.length ? itens.map((it, idx) => {
            const score = Number(it.score || 0);
            const detalhe = Object.entries(it.detalhe || {})
                .map(([k, v]) => `<span>${esc(k)} ${esc(v)}</span>`).join('');
            const motivos = (it.motivos || []).map((m) => `<li>${esc(m)}</li>`).join('');
            return `
                <article class="item">
                    <div class="item-top">
                        <span class="pos">#${esc(it.posicao)} · ${esc(it.codItem)}</span>
                        <span class="score ${score < 60 ? 'low' : ''}">${score} / 100</span>
                    </div>
                    <h4 class="item-name">${esc(it.nome)}</h4>
                    <span class="item-cat">${esc(it.categoria)}</span>
                    <span class="item-price">${it.preco == null ? '—' : brl(it.preco)}</span>
                    <div class="bar"><span style="width:${Math.max(0, Math.min(100, score))}%"></span></div>
                    <ul class="motives">${motivos || '<li>sem motivos registrados</li>'}</ul>
                    <div class="detail">${detalhe}</div>
                    <div class="item-actions">
                        <button class="btn" data-add-index="${idx}">Adicionar ao pedido</button>
                    </div>
                </article>`;
        }).join('') : '<div class="empty">Nenhum item retornado para este cliente.</div>';

        $('recResult').innerHTML = `
            <div class="rec-head">
                <div>
                    <h3 class="rec-title">${esc(data.nomeCliente || ('Cliente #' + data.idCliente))}</h3>
                    <div class="rec-meta">${chips}</div>
                </div>
                <button class="btn ghost" data-copy-rec>Copiar JSON</button>
            </div>
            <div class="ctx-strip">
                <b>${esc(ctx.estacaoDescricao || '')}</b> · ${esc(ctx.temperatura)} °C (${esc(ctx.fonteClima || '')})
                ${ctx.feriadoProximo ? ' · <b>feriado/evento próximo</b>' : ''}
                <br>${esc(ctx.descricao || '')}
            </div>
            <div class="rec-grid">${cards}</div>`;
    }

    /* ---------------------------------------------------------------- contexto */
    function dadosContexto() {
        const cidade = ($('ctxCidade').value || 'Porto Alegre').trim();
        const uf = ($('ctxUf').value || 'RS').trim().toUpperCase();
        const data = $('ctxData').value || hoje();
        return { cidade, uf, data };
    }

    async function carregarContexto() {
        const { cidade, uf } = dadosContexto();
        await comBotao($('btnCtx'), 'Consultando...', async () => {
            try {
                const data = await api(`/api/v1/contexto?cidade=${encodeURIComponent(cidade)}&uf=${encodeURIComponent(uf)}`);
                state.ctxLoaded = true;
                renderContexto(data);
            } catch (e) { mostrarErro($('ctxResult'), e); }
        });
    }

    function renderContexto(ctx) {
        const feriados = (ctx.feriados || []).slice(0, 8)
            .map((f) => `<li>${esc(f.date)} — ${esc(f.name)} <small>(${esc(f.type)})</small></li>`).join('');
        $('ctxResult').innerHTML = `
            <div class="kpi-grid">
                <div class="kpi"><span>cidade</span><strong style="font-size:16px">${esc(ctx.cidade)}</strong><small>${esc(ctx.uf)}</small></div>
                <div class="kpi"><span>estação</span><strong style="font-size:17px">${esc(ctx.estacaoDescricao)}</strong><small>${esc(ctx.data)}</small></div>
                <div class="kpi"><span>temperatura</span><strong>${esc(ctx.temperatura)}°C</strong><small>${esc(ctx.fonteClima)}</small></div>
                <div class="kpi"><span>feriado próximo</span><strong style="font-size:17px">${ctx.feriadoProximo ? 'sim' : 'não'}</strong><small>janela de 10 dias</small></div>
            </div>
            <div class="card"><h3>Descrição</h3><p style="margin:0;color:#3c5348;font-size:12px;line-height:1.55">${esc(ctx.descricao)}</p></div>
            <div class="card"><h3>Feriados/eventos</h3><ul class="steps">${feriados || '<li>nenhum no período</li>'}</ul></div>
            <pre class="json">${esc(JSON.stringify(ctx, null, 2))}</pre>`;
    }

    async function carregarEstacao() {
        const { data } = dadosContexto();
        await comBotao($('btnEstacao'), 'Consultando...', async () => {
            try {
                const d = await api('/api/v1/contexto/estacao?data=' + encodeURIComponent(data));
                $('ctxResult').innerHTML = `
                    <div class="kpi-grid">
                        <div class="kpi"><span>data</span><strong style="font-size:16px">${esc(d.data)}</strong></div>
                        <div class="kpi"><span>estação</span><strong style="font-size:18px">${esc(d.estacao)}</strong><small>${esc(d.descricao)}</small></div>
                    </div>
                    <pre class="json">${esc(JSON.stringify(d, null, 2))}</pre>`;
            } catch (e) { mostrarErro($('ctxResult'), e); }
        });
    }

    async function carregarFeriados() {
        const { cidade, data } = dadosContexto();
        const ano = (data || hoje()).slice(0, 4);
        await comBotao($('btnFeriados'), 'Consultando...', async () => {
            try {
                const lista = await api(`/api/v1/contexto/feriados?ano=${ano}&cidade=${encodeURIComponent(cidade)}`);
                const linhas = (lista || []).map((f) => `<li>${esc(f.date)} — ${esc(f.name)} <small>(${esc(f.type)} / ${esc(f.cidade)})</small></li>`).join('');
                $('ctxResult').innerHTML = `
                    <div class="card"><h3>Feriados e eventos — ${esc(cidade)} / ${ano}</h3><ul class="steps">${linhas || '<li>nenhum</li>'}</ul></div>
                    <pre class="json">${esc(JSON.stringify(lista, null, 2))}</pre>`;
            } catch (e) { mostrarErro($('ctxResult'), e); }
        });
    }

    async function carregarMensagem() {
        const { cidade } = dadosContexto();
        await comBotao($('btnMensagem'), 'Gerando...', async () => {
            try {
                const texto = await api('/api/v1/contexto/mensagem?cidade=' + encodeURIComponent(cidade));
                $('ctxResult').innerHTML = `<div class="card"><h3>Mensagem para o bot</h3><p style="white-space:pre-wrap;font-size:12px;line-height:1.6">${esc(texto)}</p></div>`;
            } catch (e) { mostrarErro($('ctxResult'), e); }
        });
    }

    /* ---------------------------------------------------------------- indicadores */
    async function carregarIndicadores() {
        await comBotao($('btnInd'), 'Carregando...', async () => {
            try {
                const d = await api('/api/v1/indicadores');
                state.indLoaded = true;
                $('indResult').innerHTML = `
                    <div class="kpi-grid">
                        <div class="kpi"><span>cobertura</span><strong>${esc(d.coberturaPercent)}%</strong><small>${esc(d.clientesComRecomendacao)} de ${esc(d.clientesTotal)} clientes</small></div>
                        <div class="kpi"><span>variedade</span><strong>${esc(d.itensDistintos)}</strong><small>itens distintos</small></div>
                        <div class="kpi"><span>taxa de recomendação</span><strong>${esc(d.taxaRecomendacaoPercent)}%</strong><small>clientes com 3+ itens</small></div>
                        <div class="kpi"><span>média por cliente</span><strong>${esc(d.mediaItensPorCliente)}</strong><small>recomendações</small></div>
                    </div>
                    <pre class="json">${esc(JSON.stringify(d, null, 2))}</pre>`;
            } catch (e) {
                state.indLoaded = false;
                mostrarErro($('indResult'), e);
            }
        });
    }

    /* ---------------------------------------------------------------- catálogo */
    async function carregarProdutos() {
        await comBotao($('btnProd'), 'Carregando...', async () => {
            try {
                const lista = await api('/api/v1/produtos?limite=100');
                state.produtos = lista || [];
                preencherCategorias();
                renderProdutos();
            } catch (e) {
                state.produtos = [];
                $('prodGrid').innerHTML = '';
                mostrarErro($('prodGrid'), e);
            }
        });
    }

    function preencherCategorias() {
        const select = $('prodCategoria');
        const atual = select.value;
        const cats = Array.from(new Set(state.produtos.map((p) => p.categoria).filter(Boolean))).sort();
        select.innerHTML = '<option value="">Todas</option>' + cats.map((c) => `<option value="${esc(c)}">${esc(c)}</option>`).join('');
        if (cats.includes(atual)) select.value = atual;
    }

    function renderProdutos() {
        const busca = ($('prodBusca').value || '').toLowerCase().trim();
        const cat = $('prodCategoria').value;
        const lista = state.produtos.filter((p) => {
            const okCat = !cat || p.categoria === cat;
            const okBusca = !busca || String(p.nome || '').toLowerCase().includes(busca);
            return okCat && okBusca;
        });
        $('prodGrid').innerHTML = lista.length ? lista.map((p) => `
            <article class="prod-card">
                <span class="cod">${esc(p.codItem)}</span>
                <h4>${esc(p.nome)}</h4>
                <span class="item-cat">${esc(p.categoria || 'Outros')}</span>
                <span class="price">${p.preco == null ? '—' : brl(p.preco)}</span>
                <button class="btn" data-cod="${esc(p.codItem)}" data-nome="${esc(p.nome)}" data-preco="${p.preco == null ? '' : p.preco}">Adicionar</button>
            </article>`).join('') : '<div class="empty">Nenhum produto encontrado.</div>';
    }

    /* ---------------------------------------------------------------- carrinho */
    function salvarCarrinho() { save('pedeFruki.painel.cart', state.cart); }

    function adicionarAoCarrinho(cod, nome, preco) {
        if (!cod) return;
        const atual = state.cart[cod] || { nome: nome || cod, preco: Number(preco) || 0, qtd: 0 };
        atual.qtd += 1;
        atual.nome = nome || atual.nome;
        if (preco !== '' && preco != null) atual.preco = Number(preco) || 0;
        state.cart[cod] = atual;
        salvarCarrinho();
        renderCarrinho();
        toast('Adicionado: ' + atual.nome);
    }

    function mudarQtd(cod, delta) {
        const item = state.cart[cod];
        if (!item) return;
        item.qtd += delta;
        if (item.qtd <= 0) delete state.cart[cod];
        salvarCarrinho();
        renderCarrinho();
    }

    function renderCarrinho() {
        const entradas = Object.entries(state.cart);
        const total = entradas.reduce((s, [, i]) => s + i.preco * i.qtd, 0);
        $('cartTotal').textContent = brl(total);
        if (!entradas.length) {
            $('cartBody').innerHTML = '<p class="fine">Carrinho vazio. Adicione itens na aba Catálogo ou na Recomendação.</p>';
            return;
        }
        $('cartBody').innerHTML = entradas.map(([cod, i]) => `
            <div class="cart-row">
                <span class="name" title="${esc(i.nome)}">${esc(i.nome)}</span>
                <span class="qty">
                    <button data-dec="${esc(cod)}" aria-label="menos">−</button>
                    <b>${i.qtd}</b>
                    <button data-inc="${esc(cod)}" aria-label="mais">+</button>
                </span>
                <span class="sub">${brl(i.preco * i.qtd)}</span>
                <button class="btn ghost" style="padding:4px 8px" data-del="${esc(cod)}">×</button>
            </div>`).join('');
    }

    /* ---------------------------------------------------------------- clientes */
    async function cadastrarCliente() {
        const body = {
            codCliente: Number($('cliCod').value),
            cnpj: $('cliCnpj').value,
            nomeFantasia: $('cliNome').value,
            cidade: $('cliCidade').value,
            uf: $('cliUf').value
        };
        if ($('cliHierarquia').value) body.codHierarquia = Number($('cliHierarquia').value);
        await comBotao($('btnCli'), 'Enviando...', async () => {
            try {
                const r = await api('/api/v1/clientes', { method: 'POST', body: JSON.stringify(body) });
                $('cliResult').innerHTML = `<div class="card"><h3>Cliente criado</h3><pre class="json">${esc(JSON.stringify(r, null, 2))}</pre></div>`;
                state.clienteId = r.codCliente;
                $('clienteAtual').value = r.codCliente;
                save('pedeFruki.painel.cliente', r.codCliente);
                toast('Cliente ' + r.codCliente + ' criado.');
            } catch (e) { mostrarErro($('cliResult'), e); }
        });
    }

    async function buscarCliente() {
        const cnpj = ($('cliBusca').value || '').replace(/\D/g, '');
        if (!cnpj) { toast('Informe o CNPJ.'); return; }
        await comBotao($('btnCliBusca'), 'Buscando...', async () => {
            try {
                const r = await api('/api/v1/clientes/' + encodeURIComponent(cnpj));
                mostrarJson($('cliResult'), r);
            } catch (e) { mostrarErro($('cliResult'), e); }
        });
    }

    /* ---------------------------------------------------------------- compras */
    async function registrarCompra() {
        const body = {
            cnpj: $('compCnpj').value,
            codItem: $('compItem').value,
            quantidade: Number($('compQtd').value),
            dataCompra: $('compData').value || hoje()
        };
        if (!body.cnpj || !body.codItem || !body.quantidade) {
            toast('Preencha CNPJ, item e quantidade.');
            return;
        }
        if ($('compValor').value) body.valorUnitario = Number($('compValor').value);
        await comBotao($('btnCompra'), 'Registrando...', async () => {
            try {
                const r = await api('/api/v1/compras', { method: 'POST', body: JSON.stringify(body) });
                mostrarJson($('compraResult'), r);
                toast('Compra registrada.');
            } catch (e) { mostrarErro($('compraResult'), e); }
        });
    }

    /* ---------------------------------------------------------------- pedido */
    async function finalizarPedido() {
        const entradas = Object.entries(state.cart);
        if (!entradas.length) { toast('Carrinho vazio.'); return; }
        const cnpj = ($('pedCnpj').value || '').replace(/\D/g, '');
        if (!cnpj) { toast('Informe o CNPJ.'); return; }
        const body = {
            cnpjCliente: cnpj,
            itens: entradas.map(([cod, i]) => ({ codigoProduto: cod, quantidade: i.qtd }))
        };
        await comBotao($('btnPedido'), 'Enviando...', async () => {
            try {
                const r = await api('/api/v1/pedidos', { method: 'POST', body: JSON.stringify(body) });
                mostrarJson($('pedidoResult'), r);
                state.cart = {};
                salvarCarrinho();
                renderCarrinho();
                toast('Pedido criado: ' + r.codigoPedido);
            } catch (e) { mostrarErro($('pedidoResult'), e); }
        });
    }

    /* ---------------------------------------------------------------- whatsapp */
    function bolha(texto, quem) {
        const div = document.createElement('div');
        div.className = 'bubble ' + quem;
        div.textContent = texto;
        $('chatLog').appendChild(div);
        $('chatLog').scrollTop = $('chatLog').scrollHeight;
    }

    async function enviarChat() {
        const telefone = ($('chatPhone').value || '').trim();
        const mensagem = ($('chatText').value || '').trim();
        if (!mensagem) return;
        bolha(mensagem, 'user');
        $('chatText').value = '';
        try {
            const r = await api('/mock/whatsapp/enviar-mensagem', {
                method: 'POST',
                body: JSON.stringify({ telefone: telefone, mensagem: mensagem })
            });
            bolha(r.resposta || JSON.stringify(r), 'bot');
        } catch (e) {
            bolha('Erro: ' + e.message, 'bot');
        }
    }

    /* ---------------------------------------------------------------- erros */
    async function dispararErro(tipo) {
        const alvo = $('erroResult');
        try {
            if (tipo === '404') {
                await api('/api/v1/recomendacoes/999999');
            } else if (tipo === '400') {
                await api('/api/v1/recomendacoes/abc');
            } else if (tipo === '409') {
                await api('/api/v1/clientes', {
                    method: 'POST',
                    body: JSON.stringify({
                        codCliente: 1001,
                        cnpj: '10000001000190',
                        nomeFantasia: 'Teste 409',
                        cidade: 'Lajeado',
                        uf: 'RS'
                    })
                });
            } else if (tipo === '401') {
                await api('/catalogo');
            } else if (tipo === '422') {
                await api('/api/v1/clientes', { method: 'POST', body: JSON.stringify({}) });
            }
            alvo.innerHTML = '<div class="error-box"><b>Nenhum erro retornado</b><div>O cenário não produziu falha. Confira a massa/serviço.</div></div>';
        } catch (e) {
            mostrarErro(alvo, e);
        }
    }

    /* ---------------------------------------------------------------- inicialização */
    function ligarEventos() {
        $$('.tab').forEach((t) => t.addEventListener('click', () => go(t.dataset.tab)));

        $$('[data-go]').forEach((b) => b.addEventListener('click', () => {
            const tab = b.dataset.go;
            go(tab);
            if (b.dataset.id) {
                $('recId').value = b.dataset.id;
                carregarRecomendacao();
            }
            if (b.dataset.cidade) {
                $('ctxCidade').value = b.dataset.cidade;
                carregarContexto();
            }
        }));

        $('btnRec').addEventListener('click', carregarRecomendacao);
        $$('[data-rec-sample]').forEach((b) => b.addEventListener('click', () => {
            $('recId').value = b.dataset.recSample;
            carregarRecomendacao();
        }));

        $('btnCtx').addEventListener('click', carregarContexto);
        $('btnEstacao').addEventListener('click', carregarEstacao);
        $('btnFeriados').addEventListener('click', carregarFeriados);
        $('btnMensagem').addEventListener('click', carregarMensagem);

        $('btnInd').addEventListener('click', carregarIndicadores);
        $('btnProd').addEventListener('click', carregarProdutos);
        $('prodBusca').addEventListener('input', renderProdutos);
        $('prodCategoria').addEventListener('change', renderProdutos);

        $('btnCli').addEventListener('click', cadastrarCliente);
        $('btnCliBusca').addEventListener('click', buscarCliente);
        $('btnCompra').addEventListener('click', registrarCompra);
        $('btnPedido').addEventListener('click', finalizarPedido);
        $('btnLimpar').addEventListener('click', () => {
            state.cart = {};
            salvarCarrinho();
            renderCarrinho();
            toast('Carrinho limpo.');
        });

        $('btnChat').addEventListener('click', enviarChat);
        $('chatText').addEventListener('keydown', (e) => { if (e.key === 'Enter') enviarChat(); });
        $$('[data-chat]').forEach((b) => b.addEventListener('click', () => {
            $('chatText').value = b.dataset.chat;
            enviarChat();
        }));

        $$('[data-erro]').forEach((b) => b.addEventListener('click', () => dispararErro(b.dataset.erro)));

        $('recResult').addEventListener('click', (e) => {
            const add = e.target.closest('[data-add-index]');
            if (add && state.lastRec) {
                const item = (state.lastRec.itens || [])[Number(add.dataset.addIndex)];
                if (item) adicionarAoCarrinho(item.codItem, item.nome, item.preco);
                return;
            }
            if (e.target.closest('[data-copy-rec]') && state.lastRec) {
                if (!navigator.clipboard) { toast('Copie manualmente o JSON exibido.'); return; }
                navigator.clipboard.writeText(JSON.stringify(state.lastRec, null, 2))
                    .then(() => toast('JSON copiado.'))
                    .catch(() => toast('Não foi possível copiar.'));
            }
        });

        $('prodGrid').addEventListener('click', (e) => {
            const b = e.target.closest('[data-cod]');
            if (b) adicionarAoCarrinho(b.dataset.cod, b.dataset.nome, b.dataset.preco);
        });

        $('cartBody').addEventListener('click', (e) => {
            const inc = e.target.closest('[data-inc]');
            const dec = e.target.closest('[data-dec]');
            const del = e.target.closest('[data-del]');
            if (inc) mudarQtd(inc.dataset.inc, 1);
            if (dec) mudarQtd(dec.dataset.dec, -1);
            if (del) { delete state.cart[del.dataset.del]; salvarCarrinho(); renderCarrinho(); }
        });

        $('clienteAtual').addEventListener('change', () => {
            const v = Number($('clienteAtual').value);
            if (v > 0) { state.clienteId = v; save('pedeFruki.painel.cliente', v); }
        });
    }

    function init() {
        $('ctxData').value = hoje();
        $('compData').value = hoje();
        $('clienteAtual').value = state.clienteId;
        $('recId').value = state.clienteId;
        ligarEventos();
        renderCarrinho();
        // ping: prova de vida sem depender de banco
        const opts = {};
        if (typeof AbortSignal !== 'undefined' && AbortSignal.timeout) {
            opts.signal = AbortSignal.timeout(8000);
        }
        fetch('/api/v1/produtos?limite=1', Object.assign({ headers: { 'Accept': 'application/json' } }, opts))
            .then((r) => marcarStatus(r.ok, r.ok ? 'API online' : 'API ' + r.status))
            .catch(() => marcarStatus(false, 'API offline'));
    }

    document.addEventListener('DOMContentLoaded', init);
})();
