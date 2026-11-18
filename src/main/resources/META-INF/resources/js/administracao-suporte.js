(function () {
    "use strict";
    let focoAnterior;
    function atualizarRolagem() {
        document.querySelectorAll("[data-rolagem]").forEach(function (regiao) {
            const dica = document.getElementById(regiao.dataset.rolagem);
            dica.hidden = regiao.scrollWidth <= regiao.clientWidth + 1;
            if (dica.hidden) regiao.removeAttribute("aria-describedby");
            else regiao.setAttribute("aria-describedby", dica.id);
        });
    }
    window.Gestao = {
        finalizarInteracao: function (id, destino) {
            const formulario = document.getElementById(id);
            formulario.setAttribute("aria-busy", "false");
            if (formulario.querySelector(".gestao-erro:not([hidden])")) return;
            const alvo = destino && document.getElementById(id + ":" + destino);
            if (alvo) { alvo.focus({ preventScroll: true }); alvo.scrollIntoView({ block: "nearest", inline: "nearest" }); }
        },
        iniciar: function (id) {
            focoAnterior = document.activeElement.id;
            window.iniciarEnvioAcesso(id);
            document.getElementById(id).querySelector(".gestao-erro").hidden = true;
        },
        concluir: function (id, destino) {
            window.concluirEnvioAcesso(id, "input.ui-state-error, textarea.ui-state-error, .ui-selectonemenu.ui-state-error .ui-selectonemenu-label");
            atualizarRolagem();
            if (document.getElementById(id).querySelector(".ui-messages-error, .ui-messages-warn, .ui-state-error, .gestao-erro:not([hidden])")) return;
            const sucesso = document.getElementById(id).querySelector(".ui-messages-info");
            const alvo = sucesso ? sucesso.closest(".ui-messages") : document.getElementById(destino ? id + ":" + destino : focoAnterior);
            if (alvo) { alvo.focus({ preventScroll: true }); alvo.scrollIntoView({ block: "nearest", inline: "nearest" }); }
        },
        falhou: function (id) {
            window.concluirEnvioAcesso(id);
            const aviso = document.getElementById(id).querySelector(".gestao-erro");
            aviso.hidden = false;
            aviso.focus({ preventScroll: true });
            aviso.scrollIntoView({ block: "nearest", inline: "nearest" });
        }
    };
    window.addEventListener("resize", atualizarRolagem);
    atualizarRolagem();
}());
