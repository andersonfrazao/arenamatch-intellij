/* Administração (ARENA-98): admin.xhtml
 *
 * Foco depois das ações AJAX. No celular a lista e a edição ficam uma abaixo da
 * outra, então o foco (e a rolagem) acompanha o que mudou:
 * - "Buscar" com resultado leva ao título do resultado;
 * - "Editar" leva ao título do formulário de edição;
 * - "Fechar" volta ao título do resultado.
 */
(function (window, document) {
    "use strict";

    window.admFocar = function (id) {
        var alvo = document.getElementById(id);
        if (alvo) {
            alvo.focus({ preventScroll: true });
            alvo.scrollIntoView({ block: "start", behavior: "smooth" });
        }
    };

    window.admAposBuscar = function () {
        if (document.querySelector(".adm-lista")) {
            window.admFocar("admResultadoTitulo");
        }
    };
}(window, document));
