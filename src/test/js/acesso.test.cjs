const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function executar(nome, contexto) {
    const arquivo = path.join(__dirname, '../../main/resources/META-INF/resources/js', nome);
    vm.runInNewContext(fs.readFileSync(arquivo, 'utf8'), contexto);
}

function ambienteFormulario() {
    const seletores = {};
    const formulario = { atributos: {}, setAttribute(nome, valor) { this.atributos[nome] = valor; }, querySelector: seletor => seletores[seletor] };
    const documento = { getElementById: () => formulario, activeElement: null };
    const janela = {
        innerHeight: 780, requestAnimationFrame: callback => callback(),
        visualViewport: { height: 780, offsetTop: 0, scale: 1, addEventListener(nome, listener) { this.listener = listener; } }
    };
    executar('acesso.js', { document: documento, window: janela });
    return { formulario, seletores, documento, janela };
}

function campo() {
    return {
        focus() { this.focado = true; },
        getBoundingClientRect() { return { top: 600, bottom: 644 }; },
        scrollIntoView() { this.rolado = true; },
        matches: seletor => seletor === 'input, textarea'
    };
}

test('sinaliza envio e devolve foco ao primeiro campo inválido', () => {
    const app = ambienteFormulario();
    const invalido = campo();
    app.seletores['input.ui-state-error'] = invalido;
    app.janela.iniciarEnvioAcesso('formLogin');
    assert.equal(app.formulario.atributos['aria-busy'], 'true');
    app.janela.concluirEnvioAcesso('formLogin');
    assert.equal(app.formulario.atributos['aria-busy'], 'false');
    assert.ok(invalido.focado);
});

test('erro de autenticação focaliza o contêiner acessível das mensagens', () => {
    const app = ambienteFormulario();
    const mensagens = campo();
    app.seletores['.ui-messages-error, .ui-messages-warn'] = { closest: () => mensagens };
    app.janela.concluirEnvioAcesso('formLogin');
    assert.ok(mensagens.focado);
});

test('avança o foco para o código após envio bem sucedido', () => {
    const app = ambienteFormulario();
    const codigo = campo();
    app.seletores["input[autocomplete='one-time-code']"] = codigo;
    app.janela.concluirEnvioAcesso('formRecuperacao');
    assert.ok(codigo.focado);
});

test('consulta parcial focaliza sua mensagem sem voltar a um campo inválido de outra ação', () => {
    const app = ambienteFormulario();
    const invalidoAnterior = campo();
    const mensagens = campo();
    app.seletores['input.ui-state-error'] = invalidoAnterior;
    app.seletores['.ui-messages-error, .ui-messages-warn'] = { closest: () => mensagens };
    app.janela.concluirEnvioAcesso('formCadastro', '[id$=inputCep].ui-state-error');
    assert.ok(mensagens.focado);
    assert.ok(!invalidoAnterior.focado);
});

test('redução do viewport revela o campo em edição', () => {
    const app = ambienteFormulario();
    const senha = campo();
    app.documento.activeElement = senha;
    app.janela.visualViewport.height = 390;
    app.janela.visualViewport.listener();
    assert.ok(senha.rolado);
});

test('ampliação mantém a posição escolhida pelo usuário', () => {
    const app = ambienteFormulario();
    const senha = campo();
    app.documento.activeElement = senha;
    app.janela.visualViewport.scale = 2;
    app.janela.visualViewport.height = 390;
    app.janela.visualViewport.listener();
    assert.ok(!senha.rolado);
});

function ambienteMenu() {
    const eventos = {};
    const classes = new Set();
    const documento = { activeElement: null, addEventListener(nome, listener) { eventos[nome] = listener; } };
    const primeiroLink = { focus() { documento.activeElement = this; }, closest: () => primeiroLink };
    const cabecalho = {
        contains: elemento => [primeiroLink, botao, menu].includes(elemento),
        addEventListener(nome, listener) { eventos[nome] = listener; }
    };
    const botao = {
        atributos: { 'aria-expanded': 'false' },
        closest: () => cabecalho,
        getAttribute(nome) { return this.atributos[nome]; },
        setAttribute(nome, valor) { this.atributos[nome] = valor; },
        addEventListener(nome, listener) { eventos['botao-' + nome] = listener; },
        focus() { documento.activeElement = this; }
    };
    const menu = {
        classList: { toggle(classe, ativo) { ativo ? classes.add(classe) : classes.delete(classe); } },
        querySelector: () => primeiroLink,
        addEventListener(nome, listener) { eventos['menu-' + nome] = listener; }
    };
    documento.getElementById = id => id === 'loginPublicToggle' ? botao : menu;
    const janela = {
        requestAnimationFrame: callback => callback(),
        matchMedia: () => ({ addEventListener(nome, listener) { eventos['media-' + nome] = listener; } })
    };
    executar('login-publico.js', { document: documento, window: janela });
    return { documento, botao, primeiroLink, classes, eventos };
}

test('menu aberto dirige o foco ao primeiro destino e Escape retorna ao botão', () => {
    const app = ambienteMenu();
    app.eventos['botao-click']();
    assert.equal(app.botao.atributos['aria-expanded'], 'true');
    assert.equal(app.botao.atributos['aria-label'], 'Fechar menu');
    assert.equal(app.documento.activeElement, app.primeiroLink);
    app.eventos.keydown({ key: 'Escape' });
    assert.equal(app.botao.atributos['aria-expanded'], 'false');
    assert.equal(app.documento.activeElement, app.botao);
});

test('menu fecha ao sair pelo teclado, clicar fora ou mudar a composição', () => {
    const app = ambienteMenu();
    app.eventos['botao-click']();
    app.documento.activeElement = {};
    app.eventos.focusout();
    assert.ok(!app.classes.has('is-open'));
    app.eventos['botao-click']();
    app.eventos.click({ target: {} });
    assert.ok(!app.classes.has('is-open'));
    app.eventos['botao-click']();
    app.eventos['media-change']();
    assert.equal(app.botao.atributos['aria-expanded'], 'false');
});
