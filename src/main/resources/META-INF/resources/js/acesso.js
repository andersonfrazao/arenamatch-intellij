(function () {
    "use strict";

    function revelarCampo(elemento) {
        if (!elemento) return;
        const viewport = window.visualViewport;
        const topo = viewport ? viewport.offsetTop : 0;
        const altura = viewport ? viewport.height : window.innerHeight;
        const limites = elemento.getBoundingClientRect();
        if (limites.top < topo + 16 || limites.bottom > topo + altura - 16) {
            elemento.scrollIntoView({ block: "nearest", inline: "nearest" });
        }
    }

    window.iniciarEnvioAcesso = function (idFormulario) {
        document.getElementById(idFormulario).setAttribute("aria-busy", "true");
    };

    window.concluirEnvioAcesso = function (idFormulario) {
        const formulario = document.getElementById(idFormulario);
        if (!formulario) return;
        formulario.setAttribute("aria-busy", "false");
        const aviso = formulario.querySelector(".ui-messages-error, .ui-messages-warn");
        const destino = formulario.querySelector("input.ui-state-error") ||
            (aviso && aviso.closest(".ui-messages")) ||
            formulario.querySelector("input[autocomplete='one-time-code']");
        if (destino) {
            destino.focus({ preventScroll: true });
            revelarCampo(destino);
        }
    };

    if (window.visualViewport) {
        let alturaAnterior = window.visualViewport.height;
        window.visualViewport.addEventListener("resize", function () {
            const alturaAtual = window.visualViewport.height;
            if (alturaAtual < alturaAnterior && window.visualViewport.scale === 1 &&
                document.activeElement.matches("input, textarea")) {
                window.requestAnimationFrame(function () { revelarCampo(document.activeElement); });
            }
            alturaAnterior = alturaAtual;
        });
    }
}());
