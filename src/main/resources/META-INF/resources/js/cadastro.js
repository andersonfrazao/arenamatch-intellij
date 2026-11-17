/*
 * Cadastro e edição de dados do time (ARENA-91).
 * As funções ficam globais porque são chamadas por onclick/oncomplete do
 * XHTML e por PrimeFaces.current().executeScript() no CadastroBean.
 */
(function () {
    'use strict';

    var ID_CABECALHO = 'formCadastro:wizardHeader';
    var ID_MENSAGENS = 'formCadastro:msgs';

    function rolagem() {
        var reduzir = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        return reduzir ? 'auto' : 'smooth';
    }

    function etapaAtual() {
        var cabecalho = document.getElementById(ID_CABECALHO);
        return cabecalho ? cabecalho.getAttribute('data-etapa') : null;
    }

    function temMensagem(elemento) {
        return !!(elemento && elemento.querySelector('.ui-messages-error, .ui-messages-warn, .ui-messages-info, .ui-messages-fatal'));
    }

    var etapaAnterior = null;
    document.addEventListener('DOMContentLoaded', function () {
        etapaAnterior = etapaAtual();
    });

    /* Mostra as mensagens gerais (abaixo do conteúdo da etapa) quando houver. */
    window.cadastroMostrarMensagens = function () {
        var mensagens = document.getElementById(ID_MENSAGENS);
        if (!temMensagem(mensagens)) {
            return;
        }
        mensagens.scrollIntoView({ behavior: rolagem(), block: 'nearest' });
        mensagens.focus({ preventScroll: true });
    };

    /* Depois de Próximo/Voltar: etapa nova começa do topo, com o foco no
       título; se a etapa não mudou (validação), mostra as mensagens. */
    window.cadastroAposNavegar = function () {
        var etapa = etapaAtual();
        if (etapa && etapa !== etapaAnterior) {
            etapaAnterior = etapa;
            var cartao = document.getElementById('cadCartao');
            if (cartao) {
                cartao.scrollIntoView({ behavior: rolagem(), block: 'start' });
            }
            var titulo = document.querySelector('#cadCartao .cad-etapa-titulo');
            if (titulo) {
                titulo.focus({ preventScroll: true });
            }
            return;
        }
        window.cadastroMostrarMensagens();
    };

    /* Chamado pelo CadastroBean quando a etapa Time não passa na validação do
       servidor (endereço incompleto). Leva o usuário à luneta e repete ali a
       mensagem, já que as mensagens gerais ficam no fim da etapa. */
    window.focarLunetaCep = function () {
        var botao = document.getElementById('formCadastro:botaoBuscarCep');
        if (!botao) {
            return;
        }

        var aviso = document.getElementById('cadCepAviso');
        if (aviso) {
            var mensagens = document.getElementById(ID_MENSAGENS);
            var detalhe = mensagens && mensagens.querySelector('.ui-messages-error-detail, .ui-messages-error-summary');
            aviso.textContent = (detalhe && detalhe.textContent.trim())
                || 'Busque o endereço pelo CEP (luneta) ou use a localização atual do smartphone.';
        }

        botao.scrollIntoView({ behavior: rolagem(), block: 'center' });
        botao.focus({ preventScroll: true });
        botao.classList.add('cad-cep-destaque');
        setTimeout(function () {
            botao.classList.remove('cad-cep-destaque');
        }, 5000);
    };

    window.obterLocalizacaoAtual = function () {
        if (!navigator.geolocation) {
            alert('Seu navegador não permite usar a localização atual.');
            return;
        }

        navigator.geolocation.getCurrentPosition(
            function (posicao) {
                document.getElementById('formCadastro:geoLat').value = posicao.coords.latitude;
                document.getElementById('formCadastro:geoLon').value = posicao.coords.longitude;
                usarLocalizacaoAtual();
            },
            function () {
                alert('Não foi possível obter sua localização. Verifique a permissão do navegador.');
            },
            {
                enableHighAccuracy: true,
                timeout: 12000,
                maximumAge: 60000
            }
        );
    };

    window.limparLocalizacaoAtual = function () {
        document.getElementById('formCadastro:geoLat').value = '';
        document.getElementById('formCadastro:geoLon').value = '';
    };

    window.abrirTermos = function () {
        document.getElementById('boxTextosTermo').scrollTop = 0;
        PF('btnAceitar').disable();
        PF('dialogTermos').show();
    };

    window.verificarScrollTermos = function () {
        var caixa = document.getElementById('boxTextosTermo');
        if (caixa.scrollTop + caixa.clientHeight >= caixa.scrollHeight - 10) {
            PF('btnAceitar').enable();
        }
    };

    window.confirmarAceite = function () {
        PF('chkTermos').check();
        PF('dialogTermos').hide();
    };
})();
