(function () {
    "use strict";

    window.concluirEtapaCadastro = function () {
        window.concluirEnvioAcesso("formCadastro");
        const formulario = document.getElementById("formCadastro");
        if (formulario.querySelector(".ui-state-error, .ui-messages-error, .ui-messages-warn")) return;
        const titulo = formulario.querySelector(".cadastro-titulo");
        if (titulo) {
            titulo.focus({ preventScroll: true });
            titulo.scrollIntoView({ block: "start", inline: "nearest" });
        }
    };

    window.focarLunetaCep = function () {
        const botao = document.getElementById("formCadastro:botaoBuscarCep");
        if (!botao) return;
        botao.scrollIntoView({ block: "nearest", inline: "nearest" });
        botao.focus({ preventScroll: true });
        botao.classList.add("foco-busca-cep");
        window.setTimeout(function () { botao.classList.remove("foco-busca-cep"); }, 5000);
    };

    function informarLocalizacao(mensagem) {
        const aviso = document.getElementById("mensagemLocalizacao");
        aviso.textContent = mensagem;
        aviso.hidden = false;
    }

    window.obterLocalizacaoAtual = function () {
        const aviso = document.getElementById("mensagemLocalizacao");
        aviso.hidden = true;
        if (!navigator.geolocation) {
            informarLocalizacao("Seu navegador não permite usar a localização atual.");
            return;
        }
        navigator.geolocation.getCurrentPosition(function (posicao) {
            if (!document.getElementById("formCadastro:inputCep")) return;
            document.getElementById("formCadastro:geoLat").value = posicao.coords.latitude;
            document.getElementById("formCadastro:geoLon").value = posicao.coords.longitude;
            window.usarLocalizacaoAtual();
        }, function () {
            if (document.getElementById("mensagemLocalizacao")) {
                informarLocalizacao("Não foi possível obter sua localização. Verifique a permissão do navegador.");
            }
        }, { enableHighAccuracy: true, timeout: 12000, maximumAge: 60000 });
    };

    window.limparLocalizacaoAtual = function () {
        document.getElementById("formCadastro:geoLat").value = "";
        document.getElementById("formCadastro:geoLon").value = "";
    };

    window.abrirTermos = function () {
        document.getElementById("boxTextosTermo").scrollTop = 0;
        PF("btnAceitar").disable();
        PF("dialogTermos").show();
    };

    window.focarLeituraTermos = function () {
        document.getElementById("boxTextosTermo").focus({ preventScroll: true });
        window.verificarScrollTermos();
    };

    window.verificarScrollTermos = function () {
        const caixa = document.getElementById("boxTextosTermo");
        if (caixa.scrollTop + caixa.clientHeight >= caixa.scrollHeight - 10) {
            PF("btnAceitar").enable();
        }
    };

    window.confirmarAceite = function () {
        PF("chkTermos").check();
        PF("dialogTermos").hide();
    };
}());
