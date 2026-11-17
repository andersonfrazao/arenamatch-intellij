(function () {
    "use strict";

    jQuery(function () {
        const botaoCalendario = document.querySelector(".am-busca .ui-datepicker-trigger");
        if (botaoCalendario) {
            botaoCalendario.setAttribute("aria-label", "Abrir calendário da data do jogo");
        }
    });

    window.atualizarRaioBusca = function (controle) {
        document.getElementById("formBusca:displayRaio").textContent = controle.value + " km";
    };

    window.concluirBusca = function (destino, idTime) {
        window.concluirEnvioAcesso("formBusca", "input.ui-state-error, textarea.ui-state-error");
        const formulario = document.getElementById("formBusca");
        if (formulario.querySelector(".ui-messages-error, .ui-messages-warn, .ui-state-error")) return;
        if (destino === "filtros") return;

        let alvo = formulario.querySelector(".busca-resultados-titulo");
        if (idTime != null) {
            const cartao = formulario.querySelector('[data-time-id="' + idTime + '"]');
            if (cartao) {
                alvo = cartao.querySelector(destino === "confirmacao" ? ".busca-confirmacao-titulo" : ".busca-card-titulo");
            }
        }
        if (alvo) {
            alvo.focus({ preventScroll: true });
            alvo.scrollIntoView({ block: "start", inline: "nearest" });
        }
    };
}());
