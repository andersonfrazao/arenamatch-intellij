const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const script = fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/administracao-suporte.js'), 'utf8');

function pagina(erroTransporte = false) {
    const estados = {};
    const focos = [];
    let validacoes = 0;
    const formulario = {
        setAttribute: (chave, valor) => { estados[chave] = valor; },
        querySelector: seletor => seletor.includes('gestao-erro') ? (erroTransporte ? {} : null) : { className: 'ui-state-error' }
    };
    const campo = { focus: () => focos.push('campo'), scrollIntoView() {} };
    const document = {
        getElementById: id => id === 'formContato' ? formulario : campo,
        querySelectorAll: () => []
    };
    const window = { addEventListener() {}, concluirEnvioAcesso() { validacoes++; } };
    vm.runInNewContext(script, { document, window });
    return { gestao: window.Gestao, estados, focos, validacoes: () => validacoes };
}

test('trocar assunto preserva foco mesmo com erro anterior no campo de mensagem', () => {
    const paginaTeste = pagina();
    paginaTeste.gestao.finalizarInteracao('formContato', '');
    assert.equal(paginaTeste.estados['aria-busy'], 'false');
    assert.equal(paginaTeste.validacoes(), 0);
    assert.deepEqual(paginaTeste.focos, []);
    paginaTeste.gestao.finalizarInteracao('formContato', 'mensagem');
    assert.deepEqual(paginaTeste.focos, ['campo']);
});

test('falha de transporte mantém foco no aviso ao concluir interação parcial', () => {
    const paginaTeste = pagina(true);
    paginaTeste.gestao.finalizarInteracao('formContato', 'mensagem');
    assert.deepEqual(paginaTeste.focos, []);
    assert.equal(paginaTeste.estados['aria-busy'], 'false');
});
