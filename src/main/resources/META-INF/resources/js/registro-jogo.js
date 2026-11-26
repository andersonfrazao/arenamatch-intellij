/*
 * Registro de jogo combinado fora do Arena Match (ARENA-70).
 * As mensagens flutuam no topo (p:growl). Depois de "Salvar na agenda", se a
 * validação marcou um campo fora da tela, ele é trazido para o meio da tela,
 * sem focar (o teclado não abre por cima do aviso). Mesmo padrão do cadastro.
 */
(function () {
    'use strict';

    window.registroJogoMostrarErro = function () {
        var campo = document.querySelector('.registro-jogo-cartao .ui-state-error');
        if (!campo) {
            return;
        }
        var area = campo.getBoundingClientRect();
        var alturaTela = window.innerHeight || document.documentElement.clientHeight;
        if (area.top < 0 || area.bottom > alturaTela) {
            var reduzir = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            campo.scrollIntoView({ behavior: reduzir ? 'auto' : 'smooth', block: 'center' });
        }
    };
})();
