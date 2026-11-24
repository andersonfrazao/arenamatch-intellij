/* Minha agenda (ARENA-93): painel "Jogos marcados" e foco após atualizações AJAX. */
(function (window, document) {
    "use strict";

    function painelTodos() {
        return document.getElementById("agendaTodosPainel");
    }

    function botaoTodos() {
        return document.getElementById("agendaTodosBtn");
    }

    function definirPainelTodos(aberto) {
        const painel = painelTodos();
        const botao = botaoTodos();
        if (!painel || !botao) {
            return;
        }
        painel.hidden = !aberto;
        botao.setAttribute("aria-expanded", String(aberto));
    }

    // Delegado no document: o botão e o painel são recriados quando o formulário é atualizado.
    document.addEventListener("click", function (event) {
        if (!event.target.closest("#agendaTodosBtn")) {
            return;
        }
        const painel = painelTodos();
        definirPainelTodos(Boolean(painel && painel.hidden));
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }
        const painel = painelTodos();
        if (painel && !painel.hidden && painel.contains(document.activeElement)) {
            definirPainelTodos(false);
            botaoTodos().focus();
        }
    });

    function focarTituloDetalhes(rolar) {
        const titulo = document.getElementById("agendaDetalhesTitulo");
        if (!titulo) {
            return;
        }
        titulo.focus({ preventScroll: true });
        if (rolar) {
            const reduzir = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
            titulo.scrollIntoView({ behavior: reduzir ? "auto" : "smooth", block: "start" });
        }
    }

    // Após escolher um jogo da lista: fecha a lista e leva ao dia escolhido.
    window.agendaAposEscolherJogo = function () {
        definirPainelTodos(false);
        focarTituloDetalhes(true);
    };

    // Ao abrir o pedido de cancelamento, o campo de motivo recebe o foco.
    window.agendaFocarCancelamento = function () {
        const campo = document.querySelector(".agenda-cancelamento textarea");
        if (campo) {
            campo.focus({ preventScroll: true });
            campo.scrollIntoView({ block: "center" });
        }
    };

    // Uma atualização AJAX recria o botão ou o dia que tinha o foco, e o foco cairia no
    // início da página. Ele volta ao mesmo elemento (mesmo id) ou, se o elemento sumiu
    // (ex.: "Hoje", "Aceitar"), ao dia selecionado ou ao título dos detalhes.
    const jq = window.jQuery;
    if (!jq || window.agendaFocoRegistrado) {
        return;
    }
    window.agendaFocoRegistrado = true;

    let foco = null;

    jq(document).on("pfAjaxSend", function () {
        const ativo = document.activeElement;
        if (!ativo || !ativo.id || !ativo.closest(".agenda")) {
            foco = null;
            return;
        }
        foco = {
            id: ativo.id,
            noCalendario: Boolean(ativo.closest(".agenda-calendario"))
        };
    });

    jq(document).on("pfAjaxComplete", function () {
        if (!foco) {
            return;
        }
        const anterior = foco;
        foco = null;

        const ativo = document.activeElement;
        if (ativo && ativo !== document.body && document.body.contains(ativo)) {
            return;
        }

        const mesmo = document.getElementById(anterior.id);
        if (mesmo) {
            mesmo.focus({ preventScroll: true });
            return;
        }
        const diaSelecionado = document.querySelector(".agenda-dia.is-selecionado");
        if (anterior.noCalendario && diaSelecionado) {
            diaSelecionado.focus({ preventScroll: true });
            return;
        }
        focarTituloDetalhes(false);
    });
})(window, document);
