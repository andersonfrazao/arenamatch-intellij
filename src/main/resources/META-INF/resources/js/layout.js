(function () {
    "use strict";

    const corpo = document.body;
    const principal = document.getElementById("amMain");
    if (!principal || !corpo.classList.contains("am-shell")) return;

    let painel = null;
    let cabecalho = null;
    let aberto = false;
    let editandoPlacar = false;
    let atualizacaoPendente = false;
    let ultimoFoco = null;
    let alturaDisponivel = window.innerHeight;
    let larguraAnterior = window.innerWidth;
    const viewport = window.visualViewport;
    const navegacao = document.getElementById("amBottomNav");

    function atualizarMedidas() {
        corpo.style.setProperty("--am-nav-height", navegacao.getBoundingClientRect().height + "px");
        const fixo = cabecalho && window.getComputedStyle(cabecalho).position === "sticky";
        corpo.style.setProperty("--am-header-height", (fixo ? cabecalho.getBoundingClientRect().height : 0) + "px");
    }

    const medidas = new ResizeObserver(atualizarMedidas);
    medidas.observe(navegacao);

    function aplicarEstado() {
        if (!painel) return;
        painel.classList.toggle("open", aberto);
        document.getElementById("amBellBtn").setAttribute("aria-expanded", String(aberto));
    }

    function atualizarSePendente() {
        if (atualizacaoPendente && !editandoPlacar) {
            atualizacaoPendente = false;
            window.atualizarSininhoAjax();
        }
    }

    function restaurarPainel() {
        const novoPainel = document.getElementById("amNotifPanel");
        const novoCabecalho = principal.querySelector(".am-topbar");
        if (novoPainel !== painel) {
            const recuperarFoco = aberto && painel && painel.contains(ultimoFoco);
            painel = novoPainel;
            editandoPlacar = false;
            aplicarEstado();
            if (recuperarFoco) painel.focus({ preventScroll: true });
            atualizarSePendente();
        }
        if (novoCabecalho !== cabecalho) {
            if (cabecalho) medidas.unobserve(cabecalho);
            cabecalho = novoCabecalho;
            if (cabecalho) medidas.observe(cabecalho);
        }
        atualizarMedidas();
    }

    window.restaurarPainelNotificacoes = restaurarPainel;
    window.atualizarNotificacoesEmTempoReal = function () {
        // Adia o refresh enquanto o placar ainda está sendo preenchido.
        atualizacaoPendente = true;
        atualizarSePendente();
    };

    function fecharPainel() {
        aberto = false;
        editandoPlacar = false;
        aplicarEstado();
        document.getElementById("amBellBtn").focus({ preventScroll: true });
        atualizarSePendente();
    }

    document.addEventListener("click", function (evento) {
        if (evento.target.closest("#amBellBtn")) {
            if (aberto) {
                fecharPainel();
                return;
            }
            aberto = true;
            aplicarEstado();
            painel.focus({ preventScroll: true });
            painel.scrollIntoView({ block: "start" });
            window.atualizarSininhoAoAbrirAjax();
        }
        if (evento.target.closest("#amNotifClose")) fecharPainel();
    });
    document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape" && aberto && painel.contains(evento.target)) {
            evento.preventDefault();
            fecharPainel();
        }
    });
    document.addEventListener("input", function (evento) {
        if (painel.contains(evento.target)) editandoPlacar = true;
    });

    function campoDeTexto(elemento) {
        return elemento && (elemento.matches("textarea, [contenteditable='true']") ||
            elemento.matches("input:not([type='hidden']):not([type='checkbox']):not([type='radio']):not([type='button']):not([type='submit']):not([type='range'])"));
    }

    function atualizarViewport() {
        const alturaAtual = viewport ? viewport.height : window.innerHeight;
        const digitando = campoDeTexto(document.activeElement);
        const mudouLargura = larguraAnterior !== window.innerWidth;
        if (!digitando || mudouLargura) alturaDisponivel = alturaAtual;
        larguraAnterior = window.innerWidth;
        const teclado = digitando && (!viewport || viewport.scale === 1) &&
            alturaDisponivel - alturaAtual > Math.max(120, alturaDisponivel * .2);
        const abriuTeclado = teclado && !corpo.classList.contains("am-keyboard-open");
        corpo.classList.toggle("am-keyboard-open", Boolean(teclado));
        atualizarMedidas();
        if (abriuTeclado) {
            window.requestAnimationFrame(function () {
                const campo = document.activeElement;
                const limites = campo.getBoundingClientRect();
                const topo = viewport ? viewport.offsetTop : 0;
                if (limites.bottom > topo + alturaAtual || limites.top < topo) {
                    campo.scrollIntoView({ block: "nearest", inline: "nearest" });
                }
            });
        }
    }

    document.addEventListener("focusin", function (evento) {
        ultimoFoco = evento.target;
        atualizarViewport();
    });
    document.addEventListener("focusout", function () {
        window.requestAnimationFrame(atualizarViewport);
    });
    window.addEventListener("resize", atualizarViewport);
    if (viewport) viewport.addEventListener("resize", atualizarViewport);
    new MutationObserver(restaurarPainel).observe(principal, { childList: true, subtree: true });
    restaurarPainel();
}());
