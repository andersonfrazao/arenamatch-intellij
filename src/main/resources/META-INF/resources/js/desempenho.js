/* Desempenho (ARENA-95): ranking.xhtml e adversario-detalhes.xhtml
 *
 * - Tabela do ranking: mostra a dica "Deslize a tabela" só quando há colunas
 *   escondidas e marca a sombra das colunas fixas depois de rolar.
 * - Troca de aba/modalidade: sinaliza carregamento (aria-busy) e devolve o foco
 *   ao controle recriado pela atualização AJAX.
 * - "Carregar mais jogos": leva o foco ao primeiro jogo novo e anuncia a quantidade.
 */
(function (window, document) {
    "use strict";

    if (window.dspIniciado) {
        return;
    }
    window.dspIniciado = true;

    function porId(id) { return document.getElementById(id); }

    window.dspFocar = function (id) {
        const alvo = porId(id);
        if (alvo) {
            alvo.focus();
        }
    };

    /* --- Tabela com rolagem própria --- */
    function avaliarTabelas() {
        document.querySelectorAll(".dsp-tabela-scroll").forEach(function (area) {
            const temMais = area.scrollWidth - area.clientWidth > 2;
            const dica = area.dataset.dica ? porId(area.dataset.dica) : null;
            if (dica) {
                dica.hidden = !temMais;
            }
            area.classList.toggle("is-rolada", area.scrollLeft > 0);
            if (!area.dataset.dspOuvindo) {
                area.dataset.dspOuvindo = "1";
                area.addEventListener("scroll", function () {
                    area.classList.toggle("is-rolada", area.scrollLeft > 0);
                }, { passive: true });
            }
        });
    }

    let timerResize = null;
    window.addEventListener("resize", function () {
        window.clearTimeout(timerResize);
        timerResize = window.setTimeout(avaliarTabelas, 120);
    });

    /* --- "Carregar mais jogos" --- */
    let jogosAntes = 0;

    window.dspAntesCarregarMais = function () {
        jogosAntes = document.querySelectorAll(".dsp-jogo").length;
    };

    window.dspAposCarregarMais = function (status) {
        if (status !== "success") {
            return;
        }
        const jogos = document.querySelectorAll(".dsp-jogo");
        const novos = jogos.length - jogosAntes;
        const aviso = porId("dspStatusJogos");
        if (novos > 0) {
            jogos[jogosAntes].focus();
            if (aviso) {
                aviso.textContent = novos === 1 ? "1 jogo carregado." : novos + " jogos carregados.";
            }
        } else {
            const botao = porId("formDesempenho:carregarMais");
            if (botao) {
                botao.focus();
            }
        }
    };

    /* --- Ciclo AJAX: carregamento e foco --- */
    let focoAntes = null;

    function painel() { return porId("formDesempenho:painelDesempenho"); }

    if (window.jQuery) {
        window.jQuery(document).on("pfAjaxSend", function () {
            const ativo = document.activeElement;
            focoAntes = ativo && ativo.id ? ativo.id : null;
            if (ativo && ativo.closest && ativo.closest(".dsp-controles, .dsp-erro") && painel()) {
                painel().setAttribute("aria-busy", "true");
            }
        });

        window.jQuery(document).on("pfAjaxComplete", function () {
            if (painel()) {
                painel().removeAttribute("aria-busy");
            }
            avaliarTabelas();
            const ativo = document.activeElement;
            if (focoAntes && (!ativo || ativo === document.body)) {
                const alvo = porId(focoAntes);
                if (alvo) {
                    alvo.focus();
                }
            }
            focoAntes = null;
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", avaliarTabelas);
    } else {
        avaliarTabelas();
    }
})(window, document);
