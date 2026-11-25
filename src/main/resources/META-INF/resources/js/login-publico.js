/*
 * Menu do cabeçalho público das páginas de acesso (ARENA-90).
 * Abaixo de 992px o menu fica recolhido: abre pelo botão, fecha ao escolher
 * um link, com Esc (devolvendo o foco ao botão), ao tocar fora ou ao sair
 * do menu pelo teclado.
 */
(() => {
    const toggle = document.getElementById('loginPublicToggle');
    const nav = document.getElementById('loginPublicNav');
    if (!toggle || !nav) return;

    const desktop = window.matchMedia('(min-width: 992px)');
    const isOpen = () => toggle.getAttribute('aria-expanded') === 'true';

    const setOpen = (open) => {
        nav.classList.toggle('is-open', open);
        toggle.setAttribute('aria-expanded', String(open));
        toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
    };

    toggle.addEventListener('click', () => {
        const open = !isOpen();
        setOpen(open);
        if (open) {
            const first = nav.querySelector('a');
            if (first) first.focus();
        }
    });

    nav.addEventListener('click', (event) => {
        if (event.target.closest('a')) setOpen(false);
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && isOpen()) {
            setOpen(false);
            toggle.focus();
        }
    });

    document.addEventListener('click', (event) => {
        if (isOpen() && !event.target.closest('.login-public-header')) setOpen(false);
    });

    // Tab para fora do menu aberto fecha o menu.
    nav.addEventListener('focusout', (event) => {
        if (desktop.matches || !isOpen()) return;
        const next = event.relatedTarget;
        if (next && !nav.contains(next) && next !== toggle) setOpen(false);
    });

    desktop.addEventListener('change', () => setOpen(false));
})();
