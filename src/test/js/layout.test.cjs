const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

const codigo = fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/layout.js'), 'utf8');

function elemento(id) {
    const classes = new Set();
    return {
        id, classes, atributos: {}, estilos: {}, filhos: new Set(), focado: false,
        classList: {
            contains: classe => classes.has(classe),
            toggle(classe, ativo) { ativo ? classes.add(classe) : classes.delete(classe); }
        },
        style: { setProperty(nome, valor) { this.dono.estilos[nome] = valor; } },
        setAttribute(nome, valor) { this.atributos[nome] = valor; },
        contains(filho) { return this === filho || this.filhos.has(filho); },
        closest(seletor) { return seletor === '#' + this.id ? this : null; },
        matches() { return false; },
        getBoundingClientRect() { return this.limites || { top: 0, bottom: 44, height: 72 }; },
        focus() { this.focado = true; },
        scrollIntoView() { this.rolado = true; }
    };
}

function ambiente() {
    const eventos = {};
    const eventosViewport = {};
    const elementos = Object.fromEntries(['amMain', 'amBottomNav', 'amNotifPanel', 'amBellBtn', 'amNotifClose', 'header']
        .map(id => [id, elemento(id)]));
    const corpo = elemento('body');
    corpo.classes.add('am-shell');
    corpo.style.dono = corpo;
    elementos.amMain.querySelector = () => elementos.header;
    let observarMutacao;
    let observarMedida;
    let atualizacoes = 0;
    let aberturas = 0;
    const documento = {
        body: corpo, activeElement: corpo,
        getElementById: id => elementos[id],
        addEventListener(nome, listener) { eventos[nome] = listener; }
    };
    const janela = {
        innerWidth: 390, innerHeight: 844,
        visualViewport: { height: 844, scale: 1, offsetTop: 0, addEventListener(nome, listener) { eventosViewport[nome] = listener; } },
        getComputedStyle: () => ({ position: 'static' }),
        requestAnimationFrame: callback => callback(),
        addEventListener(nome, listener) { eventos[nome] = listener; },
        atualizarSininhoAjax() { atualizacoes++; },
        atualizarSininhoAoAbrirAjax() { aberturas++; }
    };
    vm.runInNewContext(codigo, {
        window: janela, document: documento,
        ResizeObserver: class { constructor(callback) { observarMedida = callback; } observe() {} unobserve() {} },
        MutationObserver: class { constructor(callback) { observarMutacao = callback; } observe() {} }
    });
    return {
        elementos, corpo, documento, janela,
        evento(nome, alvo, extras = {}) { eventos[nome]({ target: alvo, ...extras }); },
        mutacao: () => observarMutacao(), medida: () => observarMedida(),
        resize: () => eventosViewport.resize(),
        atualizacoes: () => atualizacoes, aberturas: () => aberturas
    };
}

test('abre notificações, mantém aria e fecha com Escape devolvendo o foco', () => {
    const app = ambiente();
    app.evento('click', app.elementos.amBellBtn);
    assert.equal(app.elementos.amBellBtn.atributos['aria-expanded'], 'true');
    assert.equal(app.aberturas(), 1);
    assert.ok(app.elementos.amNotifPanel.focado);
    assert.ok(app.elementos.amNotifPanel.rolado);
    app.evento('keydown', app.elementos.amNotifPanel, { key: 'Escape', preventDefault() {} });
    assert.equal(app.elementos.amBellBtn.atributos['aria-expanded'], 'false');
    assert.ok(app.elementos.amBellBtn.focado);
});

test('reposição AJAX preserva painel aberto e foco de leitura', () => {
    const app = ambiente();
    app.evento('click', app.elementos.amBellBtn);
    app.evento('focusin', app.elementos.amNotifPanel);
    app.elementos.amNotifPanel = elemento('amNotifPanel');
    app.elementos.amBellBtn = elemento('amBellBtn');
    app.mutacao();
    assert.ok(app.elementos.amNotifPanel.classes.has('open'));
    assert.ok(app.elementos.amNotifPanel.focado);
    assert.equal(app.elementos.amBellBtn.atributos['aria-expanded'], 'true');
});

test('retorno de requisição de abertura respeita fechamento ocorrido durante AJAX', () => {
    const app = ambiente();
    app.evento('click', app.elementos.amBellBtn);
    app.evento('click', app.elementos.amNotifClose);
    app.elementos.amNotifPanel = elemento('amNotifPanel');
    app.janela.restaurarPainelNotificacoes();
    assert.ok(!app.elementos.amNotifPanel.classes.has('open'));
});

test('eventos WebSocket são acumulados enquanto há um placar em edição', () => {
    const app = ambiente();
    const campo = elemento('placar');
    app.elementos.amNotifPanel.filhos.add(campo);
    app.evento('click', app.elementos.amBellBtn);
    app.evento('input', campo);
    app.janela.atualizarNotificacoesEmTempoReal();
    app.janela.atualizarNotificacoesEmTempoReal();
    assert.equal(app.atualizacoes(), 0);
    app.evento('click', app.elementos.amNotifClose);
    assert.equal(app.atualizacoes(), 1);
});

test('refresh em tempo real não muda o foco fora das notificações', () => {
    const app = ambiente();
    app.evento('click', app.elementos.amBellBtn);
    app.evento('focusin', elemento('formularioPrincipal'));
    app.janela.atualizarNotificacoesEmTempoReal();
    app.elementos.amNotifPanel = elemento('amNotifPanel');
    app.mutacao();
    assert.equal(app.atualizacoes(), 1);
    assert.ok(!app.elementos.amNotifPanel.focado);
});

test('reserva de rodapé acompanha altura real da navegação', () => {
    const app = ambiente();
    app.elementos.amBottomNav.limites = { height: 128 };
    app.medida();
    assert.equal(app.corpo.estilos['--am-nav-height'], '128px');
});

test('viewport reduzido durante digitação libera o rodapé e reposiciona o campo', () => {
    const app = ambiente();
    const campo = elemento('campo');
    campo.matches = seletor => seletor.startsWith('input');
    campo.limites = { top: 600, bottom: 644, height: 44 };
    app.documento.activeElement = campo;
    app.evento('focusin', campo);
    app.janela.visualViewport.height = 430;
    app.resize();
    assert.ok(app.corpo.classes.has('am-keyboard-open'));
    assert.ok(campo.rolado);
    app.documento.activeElement = app.corpo;
    app.evento('focusout', campo);
    assert.ok(!app.corpo.classes.has('am-keyboard-open'));
});

test('zoom e mudança de orientação não são tratados como teclado virtual', () => {
    const app = ambiente();
    const campo = elemento('campo');
    campo.matches = () => true;
    app.documento.activeElement = campo;
    app.janela.visualViewport.height = 400;
    app.janela.visualViewport.scale = 2;
    app.resize();
    assert.ok(!app.corpo.classes.has('am-keyboard-open'));
    app.janela.visualViewport.scale = 1;
    app.janela.innerWidth = 844;
    app.resize();
    assert.ok(!app.corpo.classes.has('am-keyboard-open'));
});
