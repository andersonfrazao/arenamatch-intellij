/*
 * Arena Match — comportamento de apresentação do layout autenticado (ARENA-89).
 *
 * - Painel de notificações: abre/fecha pelo sino, fecha com Esc ou pelo botão
 *   fechar, mantém o estado aberto quando o painel é recriado por Ajax
 *   (WebSocket ou ações das notificações) e sincroniza aria-expanded.
 * - Teclado virtual: marca <html class="am-editando"> enquanto um campo de
 *   texto está focado, para a barra inferior sair da frente do campo.
 */
(function () {
    "use strict";

    if (window.amLayoutIniciado) {
        return;
    }
    window.amLayoutIniciado = true;

    var painelAberto = false;
    var movimentoReduzido = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    function painel() {
        return document.getElementById("amNotifPanel");
    }

    function sino() {
        return document.getElementById("amBellBtn");
    }

    function aplicarEstado() {
        var p = painel();
        var b = sino();
        if (p) {
            p.classList.toggle("open", painelAberto);
        }
        if (b) {
            b.setAttribute("aria-expanded", painelAberto ? "true" : "false");
        }
    }

    // Chamada pelo p:remoteCommand atualizarSininhoAoAbrirAjax (oncomplete).
    window.abrirPainelNotificacoes = function () {
        painelAberto = true;
        aplicarEstado();
        var p = painel();
        if (!p) {
            return;
        }
        p.scrollIntoView({ behavior: movimentoReduzido ? "auto" : "smooth", block: "start" });
        try {
            p.focus({ preventScroll: true });
        } catch (e) {
            p.focus();
        }
    };

    window.fecharPainelNotificacoes = function (devolverFoco) {
        painelAberto = false;
        aplicarEstado();
        var b = sino();
        if (devolverFoco && b) {
            b.focus();
        }
    };

    document.addEventListener("click", function (event) {
        if (event.target.closest("[data-am-notif-close]")) {
            window.fecharPainelNotificacoes(true);
            return;
        }

        if (!event.target.closest("#amBellBtn")) {
            return;
        }

        if (painelAberto) {
            window.fecharPainelNotificacoes(false);
            return;
        }

        if (typeof window.atualizarSininhoAoAbrirAjax === "function") {
            window.atualizarSininhoAoAbrirAjax();
        } else {
            window.abrirPainelNotificacoes();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape" || !painelAberto) {
            return;
        }
        // Diálogos do PrimeFaces tratam o próprio Esc.
        if (document.querySelector(".ui-dialog[aria-hidden='false'], .ui-dialog.ui-overlay-visible")) {
            return;
        }
        window.fecharPainelNotificacoes(true);
    });

    // O painel fica dentro de painelNotificacoesGlobais, que o Ajax recria:
    // reaplica o estado depois de cada atualização parcial.
    if (window.jQuery) {
        window.jQuery(document).on("pfAjaxComplete", aplicarEstado);
    }

    var tiposTexto = /^(text|search|email|tel|url|password|number|date|datetime-local|time|month|week)$/;

    function campoEditavel(el) {
        if (!el || el.disabled || el.readOnly) {
            return false;
        }
        if (el.isContentEditable || el.tagName === "TEXTAREA") {
            return true;
        }
        return el.tagName === "INPUT" && tiposTexto.test(el.type || "text");
    }

    document.addEventListener("focusin", function (event) {
        document.documentElement.classList.toggle("am-editando", campoEditavel(event.target));
    });

    document.addEventListener("focusout", function () {
        window.setTimeout(function () {
            if (!campoEditavel(document.activeElement)) {
                document.documentElement.classList.remove("am-editando");
            }
        }, 0);
    });
})();
