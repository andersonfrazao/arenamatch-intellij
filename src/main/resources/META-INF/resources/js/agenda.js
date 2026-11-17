(function () {
    "use strict";

    let aberto = false;
    let botaoAnterior = null;
    const principal = document.getElementById("amMain");

    function aplicarEstado() {
        const botao = document.getElementById("todosMeusJogosBtn");
        const painel = document.getElementById("todosMeusJogosPanel");
        if (!botao || !painel) return;
        painel.hidden = !aberto;
        painel.classList.toggle("open", aberto);
        botao.setAttribute("aria-expanded", String(aberto));
        botaoAnterior = botao;
    }

    window.restaurarAgenda = function () {
        if (botaoAnterior !== document.getElementById("todosMeusJogosBtn")) aplicarEstado();
    };

    window.concluirAgenda = function (destino) {
        window.concluirEnvioAcesso("formAgenda", "input.ui-state-error, textarea.ui-state-error");
        window.restaurarAgenda();
        const formulario = document.getElementById("formAgenda");
        if (formulario.querySelector(".ui-messages-error, .ui-messages-warn, .ui-state-error")) return;
        if (destino === "jogo") {
            aberto = false;
            aplicarEstado();
        }
        let alvo = document.getElementById(destino === "periodo" ? "formAgenda:headerNav" : "formAgenda:painelDetalhes");
        if (destino === "cancelamento") alvo = formulario.querySelector("[id$='motivoCancelamento']");
        if (alvo) {
            alvo.focus({ preventScroll: true });
            alvo.scrollIntoView({ block: "start", inline: "nearest" });
        }
    };

    document.addEventListener("click", function (evento) {
        if (evento.target.closest("#todosMeusJogosBtn")) {
            aberto = !aberto;
            aplicarEstado();
        }
    });
    document.addEventListener("keydown", function (evento) {
        const painel = document.getElementById("todosMeusJogosPanel");
        if (evento.key === "Escape" && aberto && painel && painel.contains(evento.target)) {
            aberto = false;
            aplicarEstado();
            document.getElementById("todosMeusJogosBtn").focus();
        }
    });
    if (principal) new MutationObserver(window.restaurarAgenda).observe(principal, { childList: true, subtree: true });
    aplicarEstado();
}());
