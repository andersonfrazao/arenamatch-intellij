const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function alvo() {
    return {
        focus() { this.focado = true; },
        scrollIntoView() { this.rolado = true; }
    };
}

function ambiente() {
    const titulo = alvo();
    const confirmacao = alvo();
    const cartao = alvo();
    const formulario = {
        querySelector(seletor) {
            if (seletor === '.busca-resultados-titulo') return titulo;
            if (seletor === '[data-time-id="8"]') {
                return { querySelector: s => s === '.busca-confirmacao-titulo' ? confirmacao : cartao };
            }
            return null;
        }
    };
    const raio = {};
    const janela = { concluirEnvioAcesso(id) { this.formularioConcluido = id; } };
    const calendario = { setAttribute(nome, valor) { this[nome] = valor; } };
    const documento = {
        getElementById: id => id === 'formBusca' ? formulario : raio,
        querySelector: () => calendario
    };
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/busca.js'), 'utf8'),
        { window: janela, document: documento, jQuery: callback => callback() });
    return { titulo, confirmacao, cartao, formulario, raio, janela, calendario };
}

test('calendário apresenta nome acessível para o botão de abertura', () => {
    assert.equal(ambiente().calendario['aria-label'], 'Abrir calendário da data do jogo');
});

test('pesquisa concluída focaliza os resultados e revela o título', () => {
    const app = ambiente();
    app.janela.concluirBusca('resultados');
    assert.equal(app.janela.formularioConcluido, 'formBusca');
    assert.ok(app.titulo.focado && app.titulo.rolado);
});

test('revisão e retorno direcionam o foco ao cartão selecionado', () => {
    const app = ambiente();
    app.janela.concluirBusca('confirmacao', 8);
    assert.ok(app.confirmacao.focado);
    app.janela.concluirBusca('cartao', 8);
    assert.ok(app.cartao.focado);
});

test('troca de filtro e mensagens de erro evitam deslocar foco aos resultados', () => {
    const app = ambiente();
    app.janela.concluirBusca('filtros');
    assert.ok(!app.titulo.focado);
    app.formulario.querySelector = () => ({});
    app.janela.concluirBusca('resultados');
    assert.ok(!app.titulo.focado);
});

test('controle de distância atualiza a legenda durante o ajuste', () => {
    const app = ambiente();
    app.janela.atualizarRaioBusca({ value: '45' });
    assert.equal(app.raio.textContent, '45 km');
});
