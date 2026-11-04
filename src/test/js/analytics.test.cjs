const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const script = fs.readFileSync(path.join(__dirname, '../../main/resources/META-INF/resources/js/analytics.js'), 'utf8');
const ID = 'G-VPZK0B0PSH';
const KEY = 'arenamatch.analytics-consent.v1';

function browser(options = {}) {
    const requests = [];
    const storage = new Map(options.choice ? [[KEY, options.choice]] : []);
    const cookies = new Map([['_ga', 'visitor'], ['_ga_VPZK0B0PSH', 'session'], ['JSESSIONID', 'login-session']]);
    const listeners = {};
    class Element {
        constructor(tag) {
            this.tagName = tag.toUpperCase();
            this.children = [];
            this.attributes = {};
            this.events = {};
            this.style = { paddingBottom: '' };
            this.hidden = false;
        }
        setAttribute(name, value) { this.attributes[name] = value; }
        getAttribute(name) { return this.attributes[name] || null; }
        appendChild(child) {
            this.children.push(child);
            child.parent = this;
            if (child.src) requests.push(child);
            return child;
        }
        addEventListener(name, callback) { this.events[name] = callback; }
        click() { this.events.click?.(); }
        focus() { this.focused = true; }
        getBoundingClientRect() { return { height: 180 }; }
        remove() { this.parent.children = this.parent.children.filter(child => child !== this); }
    }
    const head = new Element('head');
    const body = new Element('body');
    const currentScript = new Element('script');
    currentScript.setAttribute('data-measurement-id', options.id || ID);
    currentScript.setAttribute('data-privacy-url', '/privacidade.xhtml#estatisticas');
    function find(element, id) {
        if (element.id === id) return element;
        for (const child of element.children) {
            const result = find(child, id);
            if (result) return result;
        }
        return null;
    }
    const document = {
        head, body, currentScript,
        readyState: 'complete',
        referrer: 'https://www.google.com/search?q=futebol&email=example%40example.com#section',
        createElement: tag => new Element(tag),
        getElementById: id => find(body, id) || find(head, id)
    };
    Object.defineProperty(document, 'cookie', {
        get: () => Array.from(cookies, ([name, value]) => name + '=' + value).join('; '),
        set: value => {
            const pair = value.split(';')[0].split('=');
            if (value.includes('Max-Age=0')) cookies.delete(pair[0]);
            else cookies.set(pair[0], pair[1]);
        }
    });
    const window = {
        location: new URL(options.url || 'https://www.arenamatch.app.br/index.xhtml?token=private&email=example%40example.com#section'),
        localStorage: {
            getItem: key => {
                if (options.storageBlocked) throw new Error('Storage blocked');
                return storage.get(key) || null;
            },
            setItem: (key, value) => {
                if (options.storageBlocked) throw new Error('Storage blocked');
                storage.set(key, value);
            }
        },
        getComputedStyle: () => ({ paddingBottom: '16px' }),
        addEventListener: (name, callback) => { listeners[name] = callback; }
    };
    const context = vm.createContext({ window, document, URL, Date });
    const run = () => vm.runInContext(script, context);
    run();
    return {
        window, document, requests, storage, cookies, run,
        element: id => document.getElementById(id),
        configs: () => (window.dataLayer || []).filter(entry => entry[0] === 'config'),
        storageChanged: choice => {
            if (choice === null) storage.delete(KEY);
            else storage.set(KEY, choice);
            listeners.storage({ key: KEY });
        }
    };
}

test('sem escolha nao carrega Google nem configura coleta', () => {
    const page = browser();
    assert.equal(page.requests.length, 0);
    assert.equal(page.configs().length, 0);
    assert.equal(page.window['ga-disable-' + ID], true);
    assert.equal(page.element('am-analytics-consent').hidden, false);
    assert.equal(page.element('am-analytics-preferences').hidden, true);
});

test('aceite carrega uma tag e omite dados de query e fragmentos das URLs', () => {
    const page = browser();
    page.element('am-analytics-accept').click();
    assert.equal(page.requests.length, 1);
    assert.equal(page.requests[0].src, 'https://www.googletagmanager.com/gtag/js?id=' + ID);
    const config = page.configs()[0];
    assert.equal(config[1], ID);
    assert.equal(config[2].page_location, 'https://www.arenamatch.app.br/index.xhtml');
    assert.equal(config[2].page_referrer, 'https://www.google.com/search');
    assert.equal(config[2].allow_google_signals, false);
    assert.equal(config[2].allow_ad_personalization_signals, false);
    assert.equal(page.storage.get(KEY), 'granted');
    assert.equal(page.element('am-analytics-consent').hidden, true);
    assert.equal(page.window['ga-disable-' + ID], false);
});

test('aceitar novamente ou executar o script novamente nao duplica a tag', () => {
    const page = browser();
    page.element('am-analytics-accept').click();
    page.window.ArenaMatchAnalytics.openPreferences();
    page.element('am-analytics-accept').click();
    page.run();
    assert.equal(page.requests.length, 1);
    assert.equal(page.configs().length, 1);
});

test('aceite salvo carrega a tag sem reapresentar o aviso', () => {
    const page = browser({ choice: 'granted' });
    assert.equal(page.requests.length, 1);
    assert.equal(page.element('am-analytics-consent').hidden, true);
    assert.equal(page.element('am-analytics-preferences').hidden, false);
});

test('recusa salva continua sem Google e permite mudar a escolha', () => {
    const page = browser({ choice: 'denied' });
    assert.equal(page.requests.length, 0);
    assert.equal(page.element('am-analytics-consent').hidden, true);
    page.element('am-analytics-preferences').click();
    assert.equal(page.element('am-analytics-consent').hidden, false);
    page.element('am-analytics-accept').click();
    assert.equal(page.requests.length, 1);
});

test('recusa inicial nao configura nem carrega Google', () => {
    const page = browser();
    page.element('am-analytics-reject').click();
    assert.equal(page.storage.get(KEY), 'denied');
    assert.equal(page.requests.length, 0);
    assert.equal(page.configs().length, 0);
});

test('revogar bloqueia o destino e apaga somente cookies de Analytics', () => {
    const page = browser();
    page.element('am-analytics-accept').click();
    page.element('am-analytics-preferences').click();
    page.element('am-analytics-reject').click();
    assert.equal(page.window['ga-disable-' + ID], true);
    assert.equal(page.cookies.has('_ga'), false);
    assert.equal(page.cookies.has('_ga_VPZK0B0PSH'), false);
    assert.equal(page.cookies.get('JSESSIONID'), 'login-session');
    assert.equal(page.storage.get(KEY), 'denied');
    assert.equal(page.document.body.style.paddingBottom, '');
    page.element('am-analytics-preferences').click();
    page.element('am-analytics-accept').click();
    assert.equal(page.window['ga-disable-' + ID], false);
    assert.equal(page.requests.length, 1);
    assert.equal(page.configs().length, 1);
});

test('bloqueio de armazenamento nao impede escolha nem quebra o site', () => {
    const page = browser({ storageBlocked: true });
    assert.equal(page.requests.length, 0);
    assert.doesNotThrow(() => page.element('am-analytics-accept').click());
    assert.equal(page.requests.length, 1);
    assert.doesNotThrow(() => page.element('am-analytics-reject').click());
    assert.equal(page.window['ga-disable-' + ID], true);
});

for (const url of [
    'http://www.arenamatch.app.br/index.xhtml',
    'https://dev.arenamatch.app.br/index.xhtml',
    'https://www.arenamatch.app.br.example.com/index.xhtml',
    'https://localhost/index.xhtml'
]) {
    test('nao inicializa fora dos hosts HTTPS de producao: ' + url, () => {
        const page = browser({ url, choice: 'granted' });
        assert.equal(page.requests.length, 0);
        assert.equal(page.element('am-analytics-consent'), null);
        assert.equal(page.window.gtag, undefined);
    });
}

test('ID invalido nao inicializa a integracao', () => {
    const page = browser({ id: 'G-ABC<script>' });
    assert.equal(page.requests.length, 0);
    assert.equal(page.element('am-analytics-consent'), null);
});

test('mudanca de preferencia em outra aba bloqueia a coleta', () => {
    const page = browser({ choice: 'granted' });
    page.storageChanged('denied');
    assert.equal(page.window['ga-disable-' + ID], true);
    assert.equal(page.cookies.has('_ga'), false);
    assert.equal(page.requests.length, 1);
    page.storageChanged(null);
    assert.equal(page.element('am-analytics-consent').hidden, false);
});

test('erro ao carregar Google permite nova tentativa sem duplicar configuracao', () => {
    const page = browser();
    page.element('am-analytics-accept').click();
    assert.doesNotThrow(() => page.requests[0].onerror());
    page.element('am-analytics-preferences').click();
    page.element('am-analytics-accept').click();
    assert.equal(page.requests.length, 2);
    assert.equal(page.configs().length, 1);
});
