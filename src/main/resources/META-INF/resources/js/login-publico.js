(function () {
    "use strict";

    const botao = document.getElementById("loginPublicToggle");
    const menu = document.getElementById("loginPublicNav");
    if (!botao || !menu) return;
    const cabecalho = botao.closest(".login-public-header");

    function definirAberto(aberto) {
        menu.classList.toggle("is-open", aberto);
        botao.setAttribute("aria-expanded", String(aberto));
        botao.setAttribute("aria-label", aberto ? "Fechar menu" : "Abrir menu");
        if (aberto) menu.querySelector("a").focus();
    }

    botao.addEventListener("click", function () {
        definirAberto(botao.getAttribute("aria-expanded") !== "true");
    });
    menu.addEventListener("click", function (evento) {
        if (evento.target.closest("a")) definirAberto(false);
    });
    document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape" && botao.getAttribute("aria-expanded") === "true") {
            definirAberto(false);
            botao.focus();
        }
    });
    document.addEventListener("click", function (evento) {
        if (!cabecalho.contains(evento.target)) definirAberto(false);
    });
    cabecalho.addEventListener("focusout", function () {
        window.requestAnimationFrame(function () {
            if (!cabecalho.contains(document.activeElement)) definirAberto(false);
        });
    });
    window.matchMedia("(min-width: 62rem)").addEventListener("change", function () { definirAberto(false); });
}());
