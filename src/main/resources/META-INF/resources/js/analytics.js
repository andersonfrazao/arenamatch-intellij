(function () {
    'use strict';

    if (window.ArenaMatchAnalytics) return;
    var source = document.currentScript;
    var measurementId = source && source.getAttribute('data-measurement-id');
    var privacyUrl = source && source.getAttribute('data-privacy-url');
    var hosts = ['www.arenamatch.app.br', 'arenamatch.app.br'];
    if (!/^G-[A-Z0-9]+$/.test(measurementId || '') ||
            window.location.protocol !== 'https:' || hosts.indexOf(window.location.hostname) === -1) return;

    var storageKey = 'arenamatch.analytics-consent.v1';
    var configured = false;
    var tagRequested = false;
    var disableKey = 'ga-disable-' + measurementId;
    window[disableKey] = true;
    window.dataLayer = window.dataLayer || [];
    window.gtag = window.gtag || function () { window.dataLayer.push(arguments); };
    window.gtag('consent', 'default', {
        analytics_storage: 'denied', ad_storage: 'denied',
        ad_user_data: 'denied', ad_personalization: 'denied'
    });

    function readChoice() {
        try {
            var value = window.localStorage.getItem(storageKey);
            return value === 'granted' || value === 'denied' ? value : null;
        } catch (ignored) { return null; }
    }

    function saveChoice(choice) {
        try { window.localStorage.setItem(storageKey, choice); } catch (ignored) { /* A escolha vale nesta pagina. */ }
    }

    function cleanUrl(value) {
        try {
            var url = new URL(value);
            return url.protocol === 'https:' || url.protocol === 'http:' ? url.origin + url.pathname : '';
        } catch (ignored) { return ''; }
    }

    function startAnalytics() {
        window[disableKey] = false;
        window.gtag('consent', 'update', {
            analytics_storage: 'granted', ad_storage: 'denied',
            ad_user_data: 'denied', ad_personalization: 'denied'
        });
        if (!configured) {
            window.gtag('js', new Date());
            window.gtag('config', measurementId, {
                page_location: cleanUrl(window.location.href),
                page_referrer: cleanUrl(document.referrer),
                allow_google_signals: false,
                allow_ad_personalization_signals: false,
                cookie_flags: 'SameSite=Lax;Secure'
            });
            configured = true;
        }
        if (!tagRequested) {
            var tag = document.createElement('script');
            tag.id = 'am-google-analytics-tag';
            tag.async = true;
            tag.src = 'https://www.googletagmanager.com/gtag/js?id=' + measurementId;
            tag.onerror = function () {
                tagRequested = false;
                tag.remove();
            };
            tagRequested = true;
            document.head.appendChild(tag);
        }
    }

    function stopAnalytics() {
        // O opt-out do destino bloqueia novas medicoes sem recarregar formularios.
        window[disableKey] = true;
        var hostParts = window.location.hostname.split('.');
        var domains = [''];
        while (hostParts.length > 1) {
            domains.push('; domain=' + hostParts.join('.'));
            domains.push('; domain=.' + hostParts.join('.'));
            hostParts.shift();
        }
        document.cookie.split(';').forEach(function (cookie) {
            var name = cookie.trim().split('=')[0];
            if (name !== '_ga' && name.indexOf('_ga_') !== 0) return;
            domains.forEach(function (domain) {
                document.cookie = name + '=; Max-Age=0; path=/' + domain + '; SameSite=Lax; Secure';
            });
        });
    }

    function initialize() {
        var originalPadding = document.body.style.paddingBottom;
        var basePadding = window.getComputedStyle(document.body).paddingBottom;
        var banner = document.createElement('section');
        banner.id = 'am-analytics-consent';
        banner.className = 'am-analytics-banner';
        banner.setAttribute('aria-labelledby', 'am-analytics-title');

        var title = document.createElement('h2');
        title.id = 'am-analytics-title';
        title.textContent = 'Cookies de estatísticas';
        var text = document.createElement('p');
        text.textContent = 'Podemos usar o Google Analytics para entender as visitas e melhorar o Arena Match? Você pode aceitar ou recusar e mudar sua escolha a qualquer momento.';
        var actions = document.createElement('div');
        actions.className = 'am-analytics-actions';

        function button(id, label) {
            var element = document.createElement('button');
            element.id = id;
            element.type = 'button';
            element.textContent = label;
            actions.appendChild(element);
            return element;
        }
        var accept = button('am-analytics-accept', 'Aceitar estatísticas');
        var reject = button('am-analytics-reject', 'Recusar estatísticas');
        var policy = document.createElement('a');
        policy.href = privacyUrl;
        policy.textContent = 'Política de privacidade';
        actions.appendChild(policy);
        banner.appendChild(title);
        banner.appendChild(text);
        banner.appendChild(actions);

        var preferences = document.createElement('button');
        preferences.id = 'am-analytics-preferences';
        preferences.type = 'button';
        preferences.className = 'am-analytics-preferences';
        preferences.textContent = 'Cookies de estatísticas';
        preferences.setAttribute('aria-controls', banner.id);

        function fitSpace() {
            document.body.style.paddingBottom = banner.hidden ? originalPadding :
                'calc(' + basePadding + ' + ' + (banner.getBoundingClientRect().height + 24) + 'px)';
        }

        function showBanner(show, focus) {
            banner.hidden = !show;
            preferences.hidden = show;
            preferences.setAttribute('aria-expanded', String(show));
            fitSpace();
            if (show && focus) accept.focus();
        }

        function applyChoice(choice) {
            if (choice === 'granted') startAnalytics();
            else stopAnalytics();
            showBanner(choice === null, false);
        }

        function choose(choice) {
            saveChoice(choice);
            applyChoice(choice);
        }

        accept.addEventListener('click', function () { choose('granted'); });
        reject.addEventListener('click', function () { choose('denied'); });
        preferences.addEventListener('click', function () { showBanner(true, true); });
        document.body.appendChild(banner);
        document.body.appendChild(preferences);
        window.addEventListener('resize', fitSpace);
        if (window.ResizeObserver) new window.ResizeObserver(fitSpace).observe(banner);
        window.addEventListener('storage', function (event) {
            if (event.key === storageKey || event.key === null) applyChoice(readChoice());
        });
        window.ArenaMatchAnalytics = { openPreferences: function () { showBanner(true, true); } };
        applyChoice(readChoice());
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize, { once: true });
    else initialize();
}());
