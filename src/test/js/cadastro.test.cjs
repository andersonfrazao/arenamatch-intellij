const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function ambiente() {
    const elementos = {};
    for (const id of ['formCadastro', 'formCadastro:botaoBuscarCep', 'formCadastro:inputCep',
        'formCadastro:geoLat', 'formCadastro:geoLon', 'mensagemLocalizacao', 'boxTextosTermo', 'titulo']) {
        const classes = new Set();
        elementos[id] = {
            value: '', hidden: true, classes,
            classList: { add: classe => classes.add(classe), remove: classe => classes.delete(classe) },
            focus() { this.focado = true; },
            scrollIntoView() { this.rolado = true; }
        };
    }
    elementos.boxTextosTermo.scrollTop = 200;
    elementos.boxTextosTermo.clientHeight = 200;
    elementos.boxTextosTermo.scrollHeight = 1000;
    elementos.formCadastro.querySelector = seletor => seletor === '.cadastro-titulo' ? elementos.titulo : null;
    const botoes = {
        btnAceitar: { disable() { this.desabilitado = true; }, enable() { this.desabilitado = false; } },
        dialogTermos: { show() { this.aberto = true; }, hide() { this.aberto = false; } },
        chkTermos: { check() { this.aceito = true; } }
    };
    const janela = {
        concluirEnvioAcesso(id) { this.formularioConcluido = id; },
        usarLocalizacaoAtual() { this.consultouEndereco = true; },
        setTimeout(callback, espera) { this.temporizador = callback; this.espera = espera; }
    };
    const navegador = {};
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/cadastro.js'), 'utf8'), {
        window: janela, document: { getElementById: id => elementos[id] }, navigator: navegador, PF: nome => botoes[nome]
    });
    return { elementos, botoes, janela, navegador };
}

test('avançar o assistente direciona o foco ao título da nova etapa', () => {
    const app = ambiente();
    app.janela.concluirEtapaCadastro();
    assert.equal(app.janela.formularioConcluido, 'formCadastro');
    assert.ok(app.elementos.titulo.focado);
    assert.ok(app.elementos.titulo.rolado);
});

test('erro da etapa preserva a orientação do foco de validação', () => {
    const app = ambiente();
    app.elementos.formCadastro.querySelector = () => ({});
    app.janela.concluirEtapaCadastro();
    assert.ok(!app.elementos.titulo.focado);
});

test('luneta recebe foco e destaque temporário quando falta buscar endereço', () => {
    const app = ambiente();
    app.janela.focarLunetaCep();
    const botao = app.elementos['formCadastro:botaoBuscarCep'];
    assert.ok(botao.focado);
    assert.ok(botao.classes.has('foco-busca-cep'));
    assert.equal(app.janela.espera, 5000);
    app.janela.temporizador();
    assert.ok(!botao.classes.has('foco-busca-cep'));
});

test('indisponibilidade ou recusa do GPS aparecem na própria página', () => {
    const app = ambiente();
    app.janela.obterLocalizacaoAtual();
    assert.equal(app.elementos.mensagemLocalizacao.hidden, false);
    assert.match(app.elementos.mensagemLocalizacao.textContent, /não permite/);
    app.navegador.geolocation = { getCurrentPosition(sucesso, erro) { erro(); } };
    app.janela.obterLocalizacaoAtual();
    assert.match(app.elementos.mensagemLocalizacao.textContent, /Verifique a permissão/);
    assert.ok(!app.janela.consultouEndereco);
});

test('posição obtida preenche coordenadas e solicita o endereço', () => {
    const app = ambiente();
    app.navegador.geolocation = { getCurrentPosition(sucesso) { sucesso({ coords: { latitude: -23.5, longitude: -46.6 } }); } };
    app.janela.obterLocalizacaoAtual();
    assert.equal(app.elementos['formCadastro:geoLat'].value, -23.5);
    assert.equal(app.elementos['formCadastro:geoLon'].value, -46.6);
    assert.ok(app.janela.consultouEndereco);
    app.janela.limparLocalizacaoAtual();
    assert.equal(app.elementos['formCadastro:geoLat'].value, '');
    assert.equal(app.elementos['formCadastro:geoLon'].value, '');
});

test('resposta do GPS após sair da etapa não solicita atualização de endereço', () => {
    const app = ambiente();
    delete app.elementos['formCadastro:inputCep'];
    app.navegador.geolocation = { getCurrentPosition(sucesso) { sucesso({ coords: { latitude: -23.5, longitude: -46.6 } }); } };
    app.janela.obterLocalizacaoAtual();
    assert.ok(!app.janela.consultouEndereco);
});

test('termos começam no início, com leitura focada e aceite condicionado ao fim', () => {
    const app = ambiente();
    app.janela.abrirTermos();
    assert.equal(app.elementos.boxTextosTermo.scrollTop, 0);
    assert.ok(app.botoes.btnAceitar.desabilitado);
    assert.ok(app.botoes.dialogTermos.aberto);
    app.janela.focarLeituraTermos();
    assert.ok(app.elementos.boxTextosTermo.focado);
    assert.ok(app.botoes.btnAceitar.desabilitado);
    app.elementos.boxTextosTermo.scrollTop = 800;
    app.janela.verificarScrollTermos();
    assert.ok(!app.botoes.btnAceitar.desabilitado);
    app.janela.confirmarAceite();
    assert.ok(app.botoes.chkTermos.aceito);
    assert.ok(!app.botoes.dialogTermos.aberto);
});
