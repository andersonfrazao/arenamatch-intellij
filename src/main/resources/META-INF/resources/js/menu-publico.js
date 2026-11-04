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
        if (event.target.closest('a')) setOpen(false, false);
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
            setOpen(false, true);
        }
    });
    document.addEventListener('click', function (event) {
        if (!header.contains(event.target)) setOpen(false, false);
    });
    window.matchMedia('(max-width: 780px)').addEventListener('change', function () {
        setOpen(false, false);
    });
    header.classList.add('has-menu-toggle');
    document.documentElement.classList.add('has-public-menu');
}());
