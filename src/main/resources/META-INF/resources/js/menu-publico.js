/* Menu das páginas públicas (ARENA-97)
 *
 * - Abaixo de 992px o menu fica recolhido atrás do botão (disclosure com
 *   aria-expanded). Fecha com Esc, ao escolher um link, ao tocar fora, ao sair
 *   do menu com Tab e ao passar para o layout de desktop.
 * - Publica a altura real do cabeçalho em --site-header-h; o CSS usa esse valor
 *   no scroll-padding, então as âncoras (#planos, #conta...) não ficam
 *   escondidas atrás do cabeçalho fixo, qualquer que seja a altura dele.
 */
(function () {
    'use strict';
    var toggle = document.getElementById('siteNavToggle');
    var nav = document.getElementById('siteNav');
    var header = document.querySelector('.site-header');
    var root = document.documentElement;

    function medirCabecalho() {
        if (header) {
            root.style.setProperty('--site-header-h', Math.round(header.getBoundingClientRect().height) + 'px');
        }
    }

    medirCabecalho();
    if (header && window.ResizeObserver) {
        new window.ResizeObserver(medirCabecalho).observe(header);
    } else {
        window.addEventListener('resize', medirCabecalho);
    }

    if (!toggle || !nav || !header) return;

    var desktop = window.matchMedia('(min-width: 992px)');

    function aberto() {
        return toggle.getAttribute('aria-expanded') === 'true';
    }

    function setOpen(open, restoreFocus) {
        toggle.setAttribute('aria-expanded', String(open));
        toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
        nav.classList.toggle('is-open', open);
        if (restoreFocus) toggle.focus();
    }

    toggle.addEventListener('click', function () {
        setOpen(!aberto(), false);
    });
    nav.addEventListener('click', function (event) {
        if (event.target.closest('a')) setOpen(false, false);
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && aberto()) {
            setOpen(false, true);
        }
    });
    document.addEventListener('click', function (event) {
        if (aberto() && !header.contains(event.target)) setOpen(false, false);
    });
    // Tab depois do último link: o menu fecha em vez de ficar aberto sobre a página.
    header.addEventListener('focusout', function (event) {
        if (aberto() && event.relatedTarget && !header.contains(event.relatedTarget)) {
            setOpen(false, false);
        }
    });
    var aoMudar = function () { setOpen(false, false); };
    if (desktop.addEventListener) {
        desktop.addEventListener('change', aoMudar);
    } else if (desktop.addListener) {
        desktop.addListener(aoMudar);
    }
    header.classList.add('has-menu-toggle');
    root.classList.add('has-public-menu');
    medirCabecalho();
}());
