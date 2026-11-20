/*
 * Cadastro e edição de dados do time (ARENA-91).
 * As funções ficam globais porque são chamadas por onclick/oncomplete do
 * XHTML e por PrimeFaces.current().executeScript() no CadastroBean.
 */
(function () {
    'use strict';

    var ID_CABECALHO = 'formCadastro:wizardHeader';
    /* O p:growl "formCadastro:msgs" desenha as mensagens neste contêiner, no fim do body. */
    var ID_AVISOS = 'formCadastro:msgs_container';

    function rolagem() {
        var reduzir = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        return reduzir ? 'auto' : 'smooth';
    }

    function etapaAtual() {
        var cabecalho = document.getElementById(ID_CABECALHO);
        return cabecalho ? cabecalho.getAttribute('data-etapa') : null;
    }

    /* As mensagens de campo obrigatório têm resumo igual ao detalhe; no aviso
       flutuante o texto apareceria duas vezes. Mantém só o título nesses casos. */
    function esconderDetalheRepetido() {
        var avisos = document.getElementById(ID_AVISOS);
        if (!avisos) {
            return;
        }
        Array.prototype.forEach.call(avisos.querySelectorAll('.ui-growl-message'), function (mensagem) {
            var titulo = mensagem.querySelector('.ui-growl-title');
            var detalhe = mensagem.querySelector('p');
            if (titulo && detalhe && titulo.textContent.trim() === detalhe.textContent.trim()) {
                detalhe.hidden = true;
            }
        });
    }

    var etapaAnterior = null;
    document.addEventListener('DOMContentLoaded', function () {
        etapaAnterior = etapaAtual();
    });
    window.addEventListener('load', esconderDetalheRepetido);
    if (window.jQuery) {
        window.jQuery(document).on('pfAjaxComplete', esconderDetalheRepetido);
    }

    /* As mensagens flutuam no topo (p:growl). Quando há campo com erro fora da
       tela, traz o primeiro para o meio da tela, sem abrir o teclado. */
    window.cadastroMostrarMensagens = function () {
        var campo = document.querySelector('#cadCartao .ui-state-error');
        if (!campo) {
            return;
        }
        var area = campo.getBoundingClientRect();
        var alturaTela = window.innerHeight || document.documentElement.clientHeight;
        if (area.top < 0 || area.bottom > alturaTela) {
            campo.scrollIntoView({ behavior: rolagem(), block: 'center' });
        }
    };

    /* Depois de Próximo/Voltar: etapa nova começa do topo, com o foco no
       título; se a etapa não mudou (validação), mostra o primeiro campo com erro. */
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
       mensagem, que continua junto do campo depois que o aviso flutuante some. */
    window.focarLunetaCep = function () {
        var botao = document.getElementById('formCadastro:botaoBuscarCep');
        if (!botao) {
            return;
        }

        var aviso = document.getElementById('cadCepAviso');
        if (aviso) {
            var avisos = document.getElementById(ID_AVISOS);
            var erro = avisos && avisos.querySelector('.ui-growl-error .ui-growl-message');
            var detalhe = erro && (erro.querySelector('p') || erro.querySelector('.ui-growl-title'));
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
