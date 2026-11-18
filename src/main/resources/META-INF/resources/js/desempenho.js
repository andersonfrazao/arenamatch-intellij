(function () {
    "use strict";
    let jogosAnteriores = 0;

    function atualizarRolagem() {
        document.querySelectorAll("[data-tabela]").forEach(function (tabela) {
            const dica = document.getElementById(tabela.dataset.tabela);
            const precisaRolar = tabela.scrollWidth > tabela.clientWidth + 1;
            dica.hidden = !precisaRolar;
            if (precisaRolar) tabela.setAttribute("aria-describedby", dica.id);
            else tabela.removeAttribute("aria-describedby");
        });
    }

    window.Desempenho = {
        iniciar: function (idFormulario) {
            window.iniciarEnvioAcesso(idFormulario);
            document.querySelector("#" + idFormulario + " .desempenho-erro").hidden = true;
        },
        concluir: function (idFormulario, destino) {
            window.concluirEnvioAcesso(idFormulario, "input.ui-state-error, textarea.ui-state-error");
            atualizarRolagem();
            if (document.getElementById(idFormulario).querySelector(".ui-messages-error, .ui-messages-warn, .ui-state-error, .desempenho-erro:not([hidden])")) return;
            const alvo = document.getElementById(idFormulario + ":" + destino);
            if (alvo) {
                alvo.focus({ preventScroll: true });
                alvo.scrollIntoView({ block: "nearest", inline: "nearest" });
            }
        },
        falhou: function (idFormulario) {
            window.concluirEnvioAcesso(idFormulario);
            const aviso = document.querySelector("#" + idFormulario + " .desempenho-erro");
            aviso.hidden = false;
            aviso.focus({ preventScroll: true });
            aviso.scrollIntoView({ block: "nearest", inline: "nearest" });
        },
        prepararHistorico: function () {
            jogosAnteriores = document.querySelectorAll("[data-jogo]").length;
            this.iniciar("formDesempenho");
        },
        concluirHistorico: function () {
            this.concluir("formDesempenho", "painelJogosScout");
            if (document.getElementById("formDesempenho").querySelector(".ui-messages-error, .ui-messages-warn, .desempenho-erro:not([hidden])")) return;
            const novoJogo = document.querySelectorAll("[data-jogo]")[jogosAnteriores];
            if (novoJogo) {
                novoJogo.focus({ preventScroll: true });
                novoJogo.scrollIntoView({ block: "start", inline: "nearest" });
            }
        }
    };

    document.addEventListener("keydown", function (evento) {
        const aba = evento.target.closest("[role='tab']");
        if (!aba || !["ArrowLeft", "ArrowRight", "Home", "End"].includes(evento.key)) return;
        const abas = Array.from(aba.closest("[role='tablist']").querySelectorAll("[role='tab']"));
        let indice = abas.indexOf(aba);
        if (evento.key === "Home") indice = 0;
        else if (evento.key === "End") indice = abas.length - 1;
        else indice = (indice + (evento.key === "ArrowRight" ? 1 : abas.length - 1)) % abas.length;
        evento.preventDefault();
        abas[indice].click();
    });
    window.addEventListener("resize", atualizarRolagem);
    atualizarRolagem();
}());
