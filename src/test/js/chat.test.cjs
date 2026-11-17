const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function elemento(id) {
    return {
        id, hidden: false, disabled: false, value: '', selectionStart: 0, selectionEnd: 0,
        dataset: {}, style: { setProperty() {} }, classList: { add() {} },
        setAttribute(nome, valor) { this[nome] = valor; },
        focus() { this.focado = true; },
        click() { this.cliques = (this.cliques || 0) + 1; },
        setSelectionRange(inicio, fim) { this.selectionStart = inicio; this.selectionEnd = fim; },
        getBoundingClientRect: () => ({ top: 0, bottom: 0 }),
        querySelector: () => null, querySelectorAll: () => []
    };
}

function ambiente() {
    const ids = Object.fromEntries(['formChat', 'amBottomNav', 'chatContainer', 'formChat:painelChat',
        'painelMensagensScroll', 'formChat:novaMensagem', 'formChat:enviar', 'chatNovasMensagens',
        'chatLeituraStatus', 'chatEstadoConexao', 'chatReconectar', 'chatConexao', 'chatAcaoStatus',
        'formChat:msgs', 'chatInboxLista', 'chatConversaTitulo', 'chatInboxTitulo'].map(id => [id, elemento(id)]));
    ids.formChat.dataset = { time: '1', websocket: '/ws-arenamatch' };
    ids.formChat.parentElement = {};
    ids['formChat:painelChat'].dataset = { tipo: 'JOGO', conversa: '6' };
    ids.amBottomNav.getBoundingClientRect = () => ({ top: 710, bottom: 780 });
    const area = ids.painelMensagensScroll;
    area.scrollHeight = 1000;
    area.clientHeight = 400;
    area._topo = 0;
    Object.defineProperty(area, 'scrollTop', {
        get() { return this._topo; },
        set(valor) { this._topo = Math.max(0, Math.min(valor, this.scrollHeight - this.clientHeight)); }
    });
    area.getBoundingClientRect = () => ({ top: 150, bottom: 150 + area.clientHeight });
    const mensagens = [100, 600].map((posicao, indice) => {
        const item = elemento('chatMensagem-' + (indice + 1));
        item.getBoundingClientRect = () => ({ top: 150 + posicao - area.scrollTop, bottom: 230 + posicao - area.scrollTop });
        ids[item.id] = item;
        return item;
    });
    area.querySelectorAll = () => mensagens;
    area.querySelector = () => mensagens.at(-1);
    const eventos = {};
    const eventosJanela = {};
    const timers = new Map();
    const quadros = [];
    let numeroTimer = 0;
    let chamadas = 0;
    const clientes = [];
    const inscricoes = [];
    const documento = {
        hidden: false,
        body: { classList: { add() {} } },
        getElementById: id => ids[id],
        addEventListener: (tipo, acao) => { eventos[tipo] = acao; }
    };
    const viewport = { height: 780, offsetTop: 0, addEventListener(tipo, acao) { this[tipo] = acao; } };
    const desktop = { matches: false, addEventListener() {} };
    const janela = {
        visualViewport: viewport, innerHeight: 780,
        matchMedia: () => desktop, getComputedStyle: () => ({ paddingBottom: '0' }),
        requestAnimationFrame: acao => { quadros.push(acao); return quadros.length; },
        addEventListener: (tipo, acao) => { eventosJanela[tipo] = acao; },
        setTimeout: (acao, atraso) => { const id = ++numeroTimer; timers.set(id, { acao, atraso }); return id; },
        clearTimeout: id => timers.delete(id),
        rcAtualizarChat() { chamadas++; this.ArenaChat.prepararAtualizacao(); },
        SockJS: class {},
        Stomp: { over() {
            const cliente = {
                connect(cabecalhos, sucesso, erro) { this.sucesso = sucesso; this.erro = erro; },
                disconnect() { this.desconectado = true; },
                subscribe(rota, acao) {
                    const inscricao = { rota, acao, unsubscribe() { this.encerrada = true; } };
                    inscricoes.push(inscricao);
                    return inscricao;
                }
            };
            clientes.push(cliente);
            return cliente;
        } }
    };
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/chat.js'), 'utf8'), {
        window: janela, document: documento, ResizeObserver: class { observe() {} }
    });
    return {
        api: janela.ArenaChat, ids, area, mensagens, eventos, eventosJanela, documento, viewport, desktop,
        clientes, inscricoes, chamadas: () => chamadas,
        dispararTimers(atraso) {
            for (const [id, timer] of [...timers]) if (timer.atraso === atraso) { timers.delete(id); timer.acao(); }
        },
        quadros() { for (const acao of quadros.splice(0)) acao(); }
    };
}

function conectar(app) {
    app.clientes.at(-1).sucesso();
    app.dispararTimers(100);
    app.api.concluirAtualizacao({ chatAtualizado: true });
}

test('conecta equipe e sala e agrupa eventos em uma atualizacao', () => {
    const app = ambiente();
    conectar(app);
    assert.deepEqual(app.inscricoes.map(item => item.rota), ['/topic/notificacoes/1', '/topic/chat/6']);
    const antes = app.chamadas();
    app.inscricoes[0].acao({ body: 'CHEGOU_CHAT' });
    app.inscricoes[1].acao({});
    app.dispararTimers(100);
    assert.equal(app.chamadas(), antes + 1);
    app.inscricoes[1].acao({});
    app.dispararTimers(100);
    assert.equal(app.chamadas(), antes + 1);
    app.api.concluirAtualizacao({ chatAtualizado: true });
    assert.equal(app.chamadas(), antes + 2);
});

test('mensagem recebida preserva texto e cursor em edicao', () => {
    const app = ambiente();
    const campo = app.ids['formChat:novaMensagem'];
    campo.value = 'Rascunho de mensagem';
    campo.selectionStart = campo.selectionEnd = 10;
    app.api.prepararAtualizacao();
    campo.selectionStart = campo.selectionEnd = 4;
    app.api.concluirAtualizacao({ chatAtualizado: true });
    assert.equal(campo.value, 'Rascunho de mensagem');
    assert.equal(campo.selectionStart, 4);
});

test('ler mensagens anteriores conserva ancora e oferece acesso as novas', () => {
    const app = ambiente();
    app.area.scrollTop = 100;
    app.eventos.scroll({ target: app.area });
    app.api.prepararAtualizacao();
    app.mensagens.push(elemento('chatMensagem-3'));
    app.area.scrollHeight = 1200;
    app.api.concluirAtualizacao({ chatAtualizado: true });
    assert.equal(app.area.scrollTop, 100);
    assert.equal(app.ids.chatNovasMensagens.hidden, false);
    app.eventos.click({ target: { closest: seletor => seletor === '#chatNovasMensagens' } });
    assert.equal(app.area.scrollTop, 800);
    assert.equal(app.ids.chatNovasMensagens.hidden, true);
});

test('troca de conversa durante conexao inscreve a sala atual', () => {
    const app = ambiente();
    app.api.prepararSelecao();
    app.ids['formChat:painelChat'].dataset.conversa = '9';
    app.api.concluirSelecao();
    app.clientes[0].sucesso();
    assert.deepEqual(app.inscricoes.map(item => item.rota), ['/topic/notificacoes/1', '/topic/chat/9']);
});

test('voltar cancela inscricao da sala e conserva rascunho ao reabrir', () => {
    const app = ambiente();
    conectar(app);
    app.ids['formChat:novaMensagem'].value = 'Rascunho da partida';
    app.api.prepararSelecao();
    app.ids['formChat:painelChat'].dataset = {};
    app.api.concluirSelecao();
    assert.equal(app.inscricoes[1].encerrada, true);
    app.ids['formChat:painelChat'].dataset = { tipo: 'JOGO', conversa: '6' };
    app.ids['formChat:novaMensagem'] = elemento('formChat:novaMensagem');
    app.api.concluirSelecao();
    assert.equal(app.ids['formChat:novaMensagem'].value, 'Rascunho da partida');
});

test('falha no envio recupera texto e envio confirmado limpa o rascunho', () => {
    const app = ambiente();
    app.ids['formChat:novaMensagem'].value = 'Mensagem para enviar';
    assert.equal(app.api.prepararEnvio(), true);
    assert.equal(app.api.prepararEnvio(), false);
    app.ids['formChat:novaMensagem'] = elemento('formChat:novaMensagem');
    app.api.concluirEnvio({ mensagemEnviada: false });
    assert.equal(app.ids['formChat:novaMensagem'].value, 'Mensagem para enviar');
    app.api.prepararEnvio();
    app.ids['formChat:novaMensagem'] = elemento('formChat:novaMensagem');
    app.api.concluirEnvio({ mensagemEnviada: true });
    assert.equal(app.ids['formChat:novaMensagem'].value, '');
    assert.equal(app.area.scrollTop, 600);
});

test('Enter envia e Shift Enter e composicao preservam digitacao', () => {
    const app = ambiente();
    let impedidos = 0;
    const evento = { target: app.ids['formChat:novaMensagem'], key: 'Enter', preventDefault() { impedidos++; } };
    app.eventos.keydown({ ...evento, shiftKey: true });
    app.eventos.keydown({ ...evento, isComposing: true });
    assert.equal(impedidos, 0);
    app.eventos.keydown(evento);
    assert.equal(impedidos, 1);
    assert.equal(app.ids['formChat:enviar'].cliques, 1);
});

test('reconexao reabre apenas a sala vigente e ignora callback antigo', () => {
    const app = ambiente();
    conectar(app);
    app.clientes[0].erro();
    assert.equal(app.ids.chatReconectar.hidden, false);
    app.dispararTimers(5000);
    app.ids['formChat:painelChat'].dataset.conversa = '9';
    const antes = app.inscricoes.length;
    app.clientes[0].sucesso();
    assert.equal(app.inscricoes.length, antes);
    app.clientes[1].sucesso();
    assert.equal(app.inscricoes.at(-1).rota, '/topic/chat/9');
});

test('pagina em segundo plano adia leitura ate ficar visivel', () => {
    const app = ambiente();
    conectar(app);
    const antes = app.chamadas();
    app.documento.hidden = true;
    app.inscricoes[0].acao({ body: 'CHEGOU_CHAT' });
    app.dispararTimers(100);
    assert.equal(app.chamadas(), antes);
    app.documento.hidden = false;
    app.eventos.visibilitychange();
    app.dispararTimers(100);
    assert.equal(app.chamadas(), antes + 1);
});

test('atualizacao preserva foco de teclado na conversa da inbox', () => {
    const app = ambiente();
    const link = elemento('conversa');
    link.dataset.conversa = 'JOGO:6';
    app.documento.activeElement = { closest: () => link };
    app.api.prepararAtualizacao();
    app.documento.activeElement = app.documento.body;
    app.ids.formChat.querySelectorAll = () => [link];
    app.api.concluirAtualizacao({ chatAtualizado: true });
    assert.equal(link.focado, true);
});

test('erro de transporte continua visivel apos callback de conclusao', () => {
    const app = ambiente();
    conectar(app);
    app.api.prepararAtualizacao();
    app.api.falhouAtualizacao();
    app.api.concluirAtualizacao();
    assert.equal(app.ids.chatConexao.hidden, false);
    assert.equal(app.ids.chatReconectar.hidden, false);
});

test('reordenacao da inbox restaura foco pela partida quando o JSF reutiliza o id da linha', () => {
    const app = ambiente();
    const anterior = elemento('linha-1');
    anterior.dataset.conversa = 'JOGO:5';
    app.documento.activeElement = { closest: () => anterior };
    app.api.prepararAtualizacao();
    const linhaReutilizada = elemento('linha-1');
    linhaReutilizada.dataset.conversa = 'JOGO:6';
    const conversaDesejada = elemento('linha-0');
    conversaDesejada.dataset.conversa = 'JOGO:5';
    app.documento.activeElement = linhaReutilizada;
    app.ids.formChat.querySelectorAll = () => [conversaDesejada, linhaReutilizada];
    app.api.concluirAtualizacao({ chatAtualizado: true });
    assert.equal(conversaDesejada.focado, true);
});

test('viewport reduzido conserva leitura no fim e mede a area ocupada pela navegacao', () => {
    const app = ambiente();
    app.area.clientHeight = 200;
    app.viewport.height = 430;
    app.viewport.resize();
    app.quadros();
    assert.equal(app.area.scrollTop, 800);
});
