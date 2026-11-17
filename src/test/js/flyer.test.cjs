const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function ambiente(opcoes = {}) {
    const ids = Object.fromEntries(['flyerFeedback', 'flyerPreviewImage', 'flyerDownloadButton', 'flyerShareButton']
        .map(id => [id, { hidden: true, disabled: true, setAttribute(nome, valor) { this[nome] = valor; },
            removeAttribute(nome) { delete this[nome]; }, focus() { this.focado = true; } }]));
    const imagens = [];
    const revogadas = [];
    const textosDesenhados = [];
    let urls = 0;
    const contexto = new Proxy({
        fillText: (texto, x, y) => textosDesenhados.push({ texto, x, y }),
        measureText: texto => ({ width: texto.length * 10 }),
        createLinearGradient: () => ({ addColorStop() {} }),
        createRadialGradient: () => ({ addColorStop() {} })
    }, { get: (objeto, nome) => nome in objeto ? objeto[nome] : () => {} });
    const canvas = { getContext: () => contexto, toBlob: callback => callback({ type: 'image/png' }) };
    const texto = {
        '.flyer-home-name': 'Mandante de Testes', '.flyer-away-name': 'Visitante de Testes',
        '.flyer-date': '28/11/2026', '.flyer-time': '12:00', '.flyer-weekday': 'Sábado',
        '.flyer-field-name': 'Campo de Testes', '.flyer-address': 'Endereço fictício'
    };
    if (opcoes.nomeVisitante) texto['.flyer-away-name'] = opcoes.nomeVisitante;
    const dados = { querySelector: seletor => ({ textContent: texto[seletor] || '' }) };
    const partida = { querySelector: seletor => seletor === '.flyer-match-data' ? dados : null };
    const botao = { isConnected: true, disabled: false, dataset: { flyerBackground: 'fundo', flyerLogo: 'logo' },
        closest: () => partida, classList: { add() {}, remove() {} }, focus() { this.focado = true; } };
    const navegador = opcoes.navegador || {};
    const janela = { PF: () => ({ show() {} }), setTimeout: callback => callback() };
    vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/flyer.js'), 'utf8'), {
        window: janela,
        document: { getElementById: id => ids[id], createElement: nome => nome === 'canvas' ? canvas : {} },
        navigator: navegador,
        URL: { createObjectURL: () => 'blob:' + (++urls), revokeObjectURL: url => revogadas.push(url) },
        File: class { constructor(partes, nome, tipo) { this.name = nome; this.type = tipo.type; } },
        Image: class {
            constructor() { this.width = 1920; this.height = 1080; }
            set src(valor) {
                const completar = () => opcoes.falharFundo && valor === 'fundo' ? this.onerror() : this.onload();
                if (opcoes.adiar) imagens.push(completar);
                else queueMicrotask(completar);
            }
        }
    });
    return { api: janela.ArenaMatchFlyer, ids, botao, canvas, imagens, revogadas, textosDesenhados };
}

test('gera PNG com previa e orientacao de download para navegador sem compartilhamento', async () => {
    const app = ambiente();
    await app.api.gerar(app.botao);
    assert.equal(app.canvas.width, 1080);
    assert.equal(app.canvas.height, 1350);
    assert.equal(app.ids.flyerPreviewImage.hidden, false);
    assert.equal(app.ids.flyerDownloadButton.disabled, false);
    assert.equal(app.ids.flyerShareButton.hidden, true);
    assert.match(app.ids.flyerFeedback.textContent, /Baixe para compartilhar/);
    app.api.fechar();
    assert.deepEqual(app.revogadas, ['blob:1']);
    assert.equal(app.botao.focado, true);
});

test('nome extenso usa ate quatro linhas no espaco reservado aos times', async () => {
    const nomeVisitante = 'Associação Esportiva de Futebol Amador com Nome Extenso para Validação Mobile First';
    const app = ambiente({ nomeVisitante });
    await app.api.gerar(app.botao);
    const linhas = app.textosDesenhados.filter(item => item.x === 805 && item.y >= 590 && item.y < 700);
    assert.equal(linhas.map(item => item.texto).join(' '), nomeVisitante.toUpperCase());
    assert.ok(linhas.length <= 4);
});

test('fechar durante geracao ignora resposta tardia', async () => {
    const app = ambiente({ adiar: true });
    const pendente = app.api.gerar(app.botao);
    app.api.fechar();
    app.imagens.forEach(concluir => concluir());
    await pendente;
    assert.equal(app.ids.flyerPreviewImage.hidden, true);
    assert.equal(app.ids.flyerDownloadButton.disabled, true);
    assert.equal(app.botao.disabled, false);
});

test('resposta antiga preserva bloqueio do botao durante nova geracao', async () => {
    const app = ambiente({ adiar: true });
    const anterior = app.api.gerar(app.botao);
    const imagensAnteriores = app.imagens.splice(0);
    app.api.fechar();
    const atual = app.api.gerar(app.botao);
    imagensAnteriores.forEach(concluir => concluir());
    await anterior;
    assert.equal(app.botao.disabled, true);
    assert.equal(app.ids.flyerPreviewImage.hidden, true);
    app.imagens.forEach(concluir => concluir());
    await atual;
    assert.equal(app.botao.disabled, false);
    assert.equal(app.ids.flyerPreviewImage.hidden, false);
});

test('falha no fundo apresenta erro no dialogo e permite repetir', async () => {
    const opcoes = { falharFundo: true };
    const app = ambiente(opcoes);
    await app.api.gerar(app.botao);
    assert.equal(app.ids.flyerFeedback.role, 'alert');
    assert.match(app.ids.flyerFeedback.textContent, /fundo/);
    assert.equal(app.botao.disabled, false);
    opcoes.falharFundo = false;
    await app.api.gerar(app.botao);
    assert.equal(app.ids.flyerPreviewImage.hidden, false);
});

test('compartilha arquivo quando suportado e preserva cancelamento voluntario', async () => {
    let enviado;
    const navegador = { canShare: () => true, share: async dados => { enviado = dados; } };
    const app = ambiente({ navegador });
    await app.api.gerar(app.botao);
    assert.equal(app.ids.flyerShareButton.hidden, false);
    await app.api.compartilhar();
    assert.equal(enviado.files[0].type, 'image/png');
    assert.match(enviado.files[0].name, /mandante-de-testes/);
    navegador.share = async () => { throw { name: 'AbortError' }; };
    await app.api.compartilhar();
    assert.equal(app.ids.flyerFeedback.role, 'status');
});
