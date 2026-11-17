const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function elemento() {
    return {
        atributos: {}, hidden: true, classList: { toggle() {} },
        setAttribute(nome, valor) { this.atributos[nome] = valor; },
        focus() { this.focado = true; },
        scrollIntoView() { this.rolado = true; },
        contains(alvo) { return alvo === this; }
    };
}

function ambiente() {
    const ids = Object.fromEntries(['amMain', 'todosMeusJogosBtn', 'todosMeusJogosPanel',
        'formAgenda', 'formAgenda:headerNav', 'formAgenda:painelDetalhes', 'motivo'].map(id => [id, elemento()]));
    ids.formAgenda.querySelector = seletor => seletor.includes('motivoCancelamento') ? ids.motivo : null;
    const eventos = {};
    const janela = { concluirEnvioAcesso(id) { this.concluido = id; } };
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/agenda.js'), 'utf8'), {
        window: janela,
        document: { getElementById: id => ids[id], addEventListener: (nome, acao) => { eventos[nome] = acao; } },
        MutationObserver: class { observe() {} }
    });
    return { ids, eventos, janela };
}

test('todos os jogos alterna o painel e Escape devolve o foco', () => {
    const app = ambiente();
    app.eventos.click({ target: { closest: () => app.ids.todosMeusJogosBtn } });
    assert.equal(app.ids.todosMeusJogosPanel.hidden, false);
    assert.equal(app.ids.todosMeusJogosBtn.atributos['aria-expanded'], 'true');
    app.eventos.keydown({ key: 'Escape', target: app.ids.todosMeusJogosPanel });
    assert.equal(app.ids.todosMeusJogosPanel.hidden, true);
    assert.ok(app.ids.todosMeusJogosBtn.focado);
});

test('reposicao AJAX preserva o painel aberto', () => {
    const app = ambiente();
    app.eventos.click({ target: { closest: () => app.ids.todosMeusJogosBtn } });
    app.ids.todosMeusJogosBtn = elemento();
    app.ids.todosMeusJogosPanel = elemento();
    app.janela.restaurarAgenda();
    assert.equal(app.ids.todosMeusJogosPanel.hidden, false);
    assert.equal(app.ids.todosMeusJogosBtn.atributos['aria-expanded'], 'true');
});

test('selecionar jogo fecha a lista e focaliza detalhes', () => {
    const app = ambiente();
    app.eventos.click({ target: { closest: () => app.ids.todosMeusJogosBtn } });
    app.janela.concluirAgenda('jogo');
    assert.equal(app.ids.todosMeusJogosPanel.hidden, true);
    assert.ok(app.ids['formAgenda:painelDetalhes'].focado);
    assert.ok(app.ids['formAgenda:painelDetalhes'].rolado);
});

test('erro preserva foco de validacao e sucesso permite orientar periodo e motivo', () => {
    const app = ambiente();
    app.ids.formAgenda.querySelector = () => ({});
    app.janela.concluirAgenda('detalhes');
    assert.ok(!app.ids['formAgenda:painelDetalhes'].focado);
    app.ids.formAgenda.querySelector = seletor => seletor.includes('motivoCancelamento') ? app.ids.motivo : null;
    app.janela.concluirAgenda('periodo');
    app.janela.concluirAgenda('cancelamento');
    assert.ok(app.ids['formAgenda:headerNav'].focado);
    assert.ok(app.ids.motivo.focado);
});
