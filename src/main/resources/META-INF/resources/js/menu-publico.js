(function () {
    'use strict';
    var toggle = document.getElementById('siteNavToggle');
    var nav = document.getElementById('siteNav');
    if (!toggle || !nav) return;
    var header = toggle.closest('.site-header');

    function setOpen(open, restoreFocus) {
        toggle.setAttribute('aria-expanded', String(open));
        toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
        nav.classList.toggle('is-open', open);
        if (restoreFocus) toggle.focus();
    }

    toggle.addEventListener('click', function () {
        setOpen(toggle.getAttribute('aria-expanded') !== 'true', false);
    });
    nav.addEventListener('click', function (event) {
        var link = event.target.closest('a');
        if (!link) return;
        setOpen(false, false);
        if (link.pathname === window.location.pathname && link.hash) {
            var destino = document.getElementById(decodeURIComponent(link.hash.slice(1)));
            if (destino) {
                destino.setAttribute('tabindex', '-1');
                destino.focus({ preventScroll: true });
            }
        }
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
            setOpen(false, true);
        }
    });
    document.addEventListener('click', function (event) {
        if (!header.contains(event.target)) setOpen(false, false);
    });
    window.matchMedia('(min-width: 75rem)').addEventListener('change', function () {
        setOpen(false, false);
    });
    function atualizarAltura() {
        document.documentElement.style.setProperty('--public-header-height', header.getBoundingClientRect().height + 'px');
    }
    window.addEventListener('resize', atualizarAltura);
    if (window.ResizeObserver) new window.ResizeObserver(atualizarAltura).observe(header);
    header.classList.add('has-menu-toggle');
    document.documentElement.classList.add('has-public-menu');
    atualizarAltura();
}());
