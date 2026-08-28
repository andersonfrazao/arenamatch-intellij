const navItems = [
    { id: 'agenda', label: 'Agenda', icon: 'fa-regular fa-calendar-days' },
    { id: 'buscar', label: 'Buscar', icon: 'fa-solid fa-magnifying-glass' },
    { id: 'ranking', label: 'Ranking', icon: 'fa-solid fa-list-ol' },
    { id: 'chat', label: 'Chat', icon: 'fa-regular fa-comments' },
    { id: 'menu', label: 'Menu', icon: 'fa-solid fa-bars' }
];

const athletes = [
    { id: 1, name: 'Lucas Almeida', number: 10, position: 'Meia', goals: 8, cards: 2, initials: 'LA' },
    { id: 2, name: 'Rafael Santos', number: 9, position: 'Atacante', goals: 12, cards: 1, initials: 'RS' },
    { id: 3, name: 'Bruno Lima', number: 5, position: 'Volante', goals: 2, cards: 4, initials: 'BL' },
    { id: 4, name: 'Diego Souza', number: 1, position: 'Goleiro', goals: 0, cards: 1, initials: 'DS' }
];

const teams = ['União da Serra', 'Estrela Azul', 'Real Primavera', 'Vila Nova', 'Atlético Norte', 'Juventude FC', 'Nacional da Vila'];
let state = { route: 'menu', pro: true, management: 'hub', teamTab: 'elenco', champTab: 'overview', wizardStep: 1, format: 'groups', created: false, selectedAthlete: null };

const page = document.getElementById('page');
const toast = document.getElementById('toast');

function navMarkup(item) {
    return `<a href="#${item.id}" class="nav-item ${state.route === item.id ? 'active' : ''}" data-route="${item.id}"><i class="${item.icon}"></i><span>${item.label}</span></a>`;
}

function renderNav() {
    document.querySelector('.desktop-nav').innerHTML = navItems.map(navMarkup).join('');
    document.querySelector('.bottom-nav').innerHTML = navItems.map(navMarkup).join('');
    document.querySelectorAll('[data-route]').forEach(link => link.addEventListener('click', event => {
        event.preventDefault();
        state.route = link.dataset.route;
        if (state.route === 'menu') state.management = 'hub';
        render();
    }));
}

function renderTemplate(name) {
    page.innerHTML = document.getElementById(`${name}-template`).innerHTML;
}

function managementHub() {
    const lock = state.pro ? '' : '<span class="menu-lock"><i class="fa-solid fa-lock"></i></span>';
    return `
        <div class="page-heading"><div><span class="eyebrow">Módulos e configurações</span><h1><i class="fa-solid fa-bars"></i> Menu</h1><p>Recursos de gestão ficam organizados aqui, sem alterar Agenda, Busca, Ranking ou Chat.</p></div></div>
        <section class="menu-section">
            <h2><i class="fa-solid fa-bolt"></i> Gestão do Time <span class="section-pro"><i class="fa-solid fa-crown"></i> PRO</span></h2>
            <div class="menu-list premium-list">
                <button class="menu-entry" data-management="team" data-open-team-tab="elenco"><span class="menu-icon"><i class="fa-solid fa-people-group"></i></span><span><strong>Meu Elenco</strong><small>Atletas, posições, camisas e situação</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-management="team" data-open-team-tab="stats"><span class="menu-icon"><i class="fa-solid fa-chart-line"></i></span><span><strong>Estatísticas dos Atletas</strong><small>Gols, cartões, partidas e histórico</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-management="team" data-open-team-tab="matches"><span class="menu-icon"><i class="fa-solid fa-clipboard-list"></i></span><span><strong>Jogos e Súmulas</strong><small>Eventos registrados nas partidas</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
            </div>
        </section>
        <section class="menu-section">
            <h2><i class="fa-solid fa-trophy"></i> Campeonatos <span class="section-pro"><i class="fa-solid fa-crown"></i> PRO</span></h2>
            <div class="menu-list premium-list">
                <button class="menu-entry" data-management="wizard"><span class="menu-icon"><i class="fa-solid fa-diagram-project"></i></span><span><strong>Criar Campeonato</strong><small>Grupos, mata-mata ou pontos corridos</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-management="championships"><span class="menu-icon"><i class="fa-solid fa-medal"></i></span><span><strong>Meus Campeonatos</strong><small>Competições criadas ou disputadas</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-management="championships"><span class="menu-icon"><i class="fa-solid fa-envelope-open-text"></i></span><span><strong>Convites Recebidos</strong><small>Convites para participar de campeonatos</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
            </div>
        </section>
        <section class="menu-section">
            <h2 class="general-title"><i class="fa-solid fa-sliders"></i> Geral</h2>
            <div class="menu-list general-list">
                <button class="menu-entry" data-demo-toast="Edição dos dados aberta no sistema real"><span class="menu-icon"><i class="fa-regular fa-id-card"></i></span><span><strong>Meus Dados</strong><small>Responsável, informações e escudo do time</small></span><i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-demo-toast="Canal de suporte aberto"><span class="menu-icon"><i class="fa-solid fa-headset"></i></span><span><strong>Suporte</strong><small>Fale com o Arena Match</small></span><i class="fa-solid fa-chevron-right"></i></button>
                <button class="menu-entry" data-demo-toast="Administração disponível conforme o perfil"><span class="menu-icon"><i class="fa-solid fa-user-shield"></i></span><span><strong>Administração</strong><small>Visível somente para administradores</small></span><i class="fa-solid fa-chevron-right"></i></button>
            </div>
        </section>`;
}

function paywall(title) {
    return `<div class="page-heading"><button class="ghost-button" data-management="hub"><i class="fa-solid fa-arrow-left"></i> Menu</button></div><div class="paywall"><span class="paywall-icon"><i class="fa-solid fa-lock"></i></span><span class="eyebrow">Exclusivo PRO pago</span><h2>${title}</h2><p>Este módulo é liberado somente quando a assinatura PRO está ativa e o pagamento confirmado.</p><button class="primary-button" id="upgradeDemo"><i class="fa-solid fa-crown"></i> Simular ativação PRO</button></div>`;
}

function teamModule() {
    if (!state.pro) return paywall('Gerenciamento do Time');
    const rows = athletes.map(a => `<div class="athlete-row"><span class="avatar">${a.initials}</span><span class="row-copy"><strong>${a.number} · ${a.name}</strong><small>${a.position} · Ativo</small></span><span class="row-stats"><span><b>${a.goals}</b> gols</span><span><b>${a.cards}</b> cartões</span><button class="row-action" data-edit-athlete="${a.id}" aria-label="Editar ${a.name}"><i class="fa-solid fa-pen"></i></button></span></div>`).join('');
    return `
        <div class="page-heading"><div><button class="ghost-button" data-management="hub"><i class="fa-solid fa-arrow-left"></i> Menu</button><span class="eyebrow"> Gestão do Time</span><h1><i class="fa-solid fa-people-group"></i> Meu Time</h1><p>O elenco é compartilhado com partidas amistosas e campeonatos.</p></div><button class="primary-button" id="addAthlete"><i class="fa-solid fa-user-plus"></i> Novo atleta</button></div>
        <div class="overview-strip"><div class="metric"><strong>${athletes.length}</strong><small>Atletas ativos</small></div><div class="metric"><strong>22</strong><small>Gols na temporada</small></div><div class="metric"><strong>8</strong><small>Cartões</small></div></div>
        <div class="subnav"><button class="${state.teamTab === 'elenco' ? 'active' : ''}" data-team-tab="elenco">Elenco</button><button class="${state.teamTab === 'stats' ? 'active' : ''}" data-team-tab="stats">Estatísticas</button><button class="${state.teamTab === 'matches' ? 'active' : ''}" data-team-tab="matches">Jogos</button></div>
        ${state.teamTab === 'elenco' ? `<section class="panel"><div class="panel-header"><div><h2>Elenco principal</h2><p>Atletas disponíveis para escalação</p></div></div><div class="athlete-list">${rows}</div></section>` : teamSecondary()}`;
}

function teamSecondary() {
    if (state.teamTab === 'form') {
        const athlete = athletes.find(a => a.id === state.selectedAthlete) || { name:'', number:'', position:'Atacante' };
        return `<section class="panel"><div class="panel-header"><div><h2>${state.selectedAthlete ? 'Editar atleta' : 'Novo atleta'}</h2><p>Dados usados nas escalações e súmulas</p></div></div><div class="form-card embedded"><label>Nome completo<input id="athleteName" value="${athlete.name}" placeholder="Nome do atleta"></label><label>Número da camisa<input id="athleteNumber" type="number" min="1" max="99" value="${athlete.number}" placeholder="10"></label><label>Posição<select id="athletePosition">${['Goleiro','Zagueiro','Lateral','Volante','Meia','Atacante'].map(p=>`<option ${p===athlete.position?'selected':''}>${p}</option>`).join('')}</select></label><div class="button-row"><button class="ghost-button" id="cancelAthlete">Cancelar</button><button class="primary-button" id="saveAthlete"><i class="fa-solid fa-check"></i> Salvar atleta</button></div></div></section>`;
    }
    if (state.teamTab === 'stats') return `<section class="panel"><div class="panel-header"><div><h2>Destaques da temporada</h2><p>Dados originados das súmulas confirmadas</p></div></div><table class="standings"><thead><tr><th>#</th><th>Atleta</th><th>Jogos</th><th>Gols</th><th>Cartões</th></tr></thead><tbody>${athletes.map((a,i)=>`<tr><td>${i+1}</td><td>${a.name}</td><td>${12-i}</td><td>${a.goals}</td><td>${a.cards}</td></tr>`).join('')}</tbody></table></section>`;
    return `<section class="panel"><div class="panel-header"><div><h2>Jogos realizados</h2><p>Abra uma partida para consultar sua súmula</p></div></div><div class="match-list"><div class="match-row"><span class="event-date"><b>3</b><small>SET</small></span><span class="row-copy"><strong>Mocidade 3 × 1 União</strong><small>Amistoso · Súmula concluída</small></span><span class="status confirmed">Finalizado</span></div><div class="match-row"><span class="event-date"><b>27</b><small>AGO</small></span><span class="row-copy"><strong>Estrela 2 × 2 Mocidade</strong><small>Partida principal · Súmula concluída</small></span><span class="status confirmed">Finalizado</span></div></div></section>`;
}

function championshipModule() {
    if (!state.pro) return paywall('Campeonatos');
    if (state.management === 'wizard') return championshipWizard();
    if (state.management === 'champ-detail') return championshipDetail();
    return `
        <div class="page-heading"><div><button class="ghost-button" data-management="hub"><i class="fa-solid fa-arrow-left"></i> Menu</button><span class="eyebrow"> Área PRO</span><h1><i class="fa-solid fa-trophy"></i> Campeonatos</h1><p>Competições organizadas sem alterar o fluxo principal de agendamento.</p></div><button class="primary-button" data-management="wizard"><i class="fa-solid fa-plus"></i> Criar campeonato</button></div>
        <div class="overview-strip"><div class="metric"><strong>2</strong><small>Em andamento</small></div><div class="metric"><strong>8</strong><small>Equipes convidadas</small></div><div class="metric"><strong>3</strong><small>Súmulas pendentes</small></div></div>
        <section class="panel"><div class="panel-header"><div><h2>Meus campeonatos</h2><p>Você administra estas competições</p></div></div><div class="champ-list"><button class="champ-row" data-management="champ-detail"><span class="module-icon"><i class="fa-solid fa-trophy"></i></span><span class="row-copy"><strong>Copa Arena 2026</strong><small>Grupos + mata-mata · 8 equipes · Em andamento</small></span><span class="status confirmed">Rodada 3</span></button><div class="champ-row"><span class="module-icon"><i class="fa-solid fa-arrows-left-right"></i></span><span class="row-copy"><strong>Taça Primavera</strong><small>Pontos corridos · 6 equipes · Inscrições</small></span><span class="status pending">Convites</span></div></div></section>`;
}

function championshipWizard() {
    const steps = [1,2,3,4].map(n=>`<span class="${n<=state.wizardStep?'done':''}"></span>`).join('');
    let content = '';
    if (state.wizardStep === 1) content = `<div class="form-card"><label>Nome do campeonato<input id="champName" value="Copa Arena 2026"></label><label>Descrição<input value="Competição regional de futebol amador"></label><label>Data prevista<input type="date" value="2026-10-03"></label></div>`;
    if (state.wizardStep === 2) content = `<div class="format-grid"><button class="format-option ${state.format==='groups'?'selected':''}" data-format="groups"><i class="fa-solid fa-diagram-project"></i><strong>Grupos + mata-mata</strong><small>Fase classificatória seguida por eliminatórias.</small></button><button class="format-option ${state.format==='knockout'?'selected':''}" data-format="knockout"><i class="fa-solid fa-code-branch"></i><strong>Mata-mata</strong><small>Confrontos eliminatórios desde o início.</small></button><button class="format-option ${state.format==='league'?'selected':''}" data-format="league"><i class="fa-solid fa-table-list"></i><strong>Pontos corridos</strong><small>Todos contra todos por classificação.</small></button></div>`;
    if (state.wizardStep === 3) content = `<div class="team-checks">${teams.map((t,i)=>`<label class="team-check"><input type="checkbox" ${i<5?'checked':''}><span class="crest small">${t.split(' ').map(w=>w[0]).slice(0,2).join('')}</span><span>${t}</span></label>`).join('')}</div>`;
    if (state.wizardStep === 4) content = `<div class="hero-card"><div><span class="eyebrow">Revisão</span><h2>Copa Arena 2026</h2><p>${formatName(state.format)} · Mocidade FC + 5 convidados</p></div><span class="status confirmed">Pronto para criar</span></div><div class="panel"><p>Os convidados receberão uma notificação. A tabela será gerada somente após o encerramento das confirmações.</p></div>`;
    return `<div class="page-heading"><div><button class="ghost-button" data-management="championships"><i class="fa-solid fa-xmark"></i> Cancelar</button><span class="eyebrow"> Novo campeonato</span><h1>Etapa ${state.wizardStep} de 4</h1></div></div><div class="wizard-progress">${steps}</div>${content}<div class="button-row" style="margin-top:1rem"><button class="ghost-button" id="wizardBack" ${state.wizardStep===1?'disabled':''}>Voltar</button><button class="primary-button" id="wizardNext">${state.wizardStep===4?'Criar campeonato':'Continuar'}</button></div>`;
}

function formatName(format) { return ({groups:'Grupos + mata-mata',knockout:'Mata-mata',league:'Pontos corridos'})[format]; }

function championshipDetail() {
    let body = '';
    if (state.champTab === 'overview') body = `<div class="overview-strip"><div class="metric"><strong>8</strong><small>Equipes</small></div><div class="metric"><strong>12</strong><small>Jogos realizados</small></div><div class="metric"><strong>38</strong><small>Gols</small></div></div><section class="panel"><div class="panel-header"><div><h2>Próximos jogos</h2><p>As partidas também aparecem na agenda dos times</p></div></div><div class="match-list"><div class="match-row"><span class="event-date"><b>05</b><small>SET</small></span><span class="row-copy"><strong>Mocidade FC × Estrela Azul</strong><small>Semifinal · 15:30</small></span><button class="secondary-button" id="openSheet">Súmula</button></div></div></section>`;
    if (state.champTab === 'table') body = `<section class="panel"><div class="panel-header"><div><h2>Grupo A</h2><p>Os dois primeiros avançam</p></div></div><table class="standings"><thead><tr><th>#</th><th>Equipe</th><th>J</th><th>SG</th><th>PTS</th></tr></thead><tbody><tr><td>1</td><td>Mocidade FC</td><td>3</td><td>+5</td><td>7</td></tr><tr><td>2</td><td>Estrela Azul</td><td>3</td><td>+2</td><td>6</td></tr><tr><td>3</td><td>União da Serra</td><td>3</td><td>-1</td><td>3</td></tr><tr><td>4</td><td>Vila Nova</td><td>3</td><td>-6</td><td>1</td></tr></tbody></table></section>`;
    if (state.champTab === 'bracket') body = `<section class="panel bracket-wrap"><div class="panel-header"><div><h2>Mata-mata</h2><p>Chaveamento após a fase de grupos</p></div></div><div class="bracket"><div class="round"><h3>Semifinais</h3><div class="bracket-match"><div class="winner"><span>Mocidade FC</span><b>3</b></div><div><span>Real Primavera</span><b>1</b></div></div><div class="bracket-match"><div><span>Estrela Azul</span><b>–</b></div><div><span>União da Serra</span><b>–</b></div></div></div><div class="round"><h3>Final</h3><div class="bracket-match"><div class="winner"><span>Mocidade FC</span><b>–</b></div><div><span>A definir</span><b>–</b></div></div></div></div></section>`;
    if (state.champTab === 'stats') body = `<section class="panel"><div class="panel-header"><div><h2>Artilharia</h2><p>Somente súmulas publicadas</p></div></div><table class="standings"><thead><tr><th>#</th><th>Atleta</th><th>Equipe</th><th>Gols</th></tr></thead><tbody><tr><td>1</td><td>Rafael Santos</td><td>Mocidade</td><td>6</td></tr><tr><td>2</td><td>Caio Mendes</td><td>Estrela</td><td>4</td></tr><tr><td>3</td><td>Lucas Almeida</td><td>Mocidade</td><td>3</td></tr></tbody></table></section>`;
    if (state.champTab === 'sheet') body = matchSheet();
    return `<div class="page-heading"><div><button class="ghost-button" data-management="championships"><i class="fa-solid fa-arrow-left"></i> Campeonatos</button><span class="eyebrow"> Em andamento</span><h1><i class="fa-solid fa-trophy"></i> Copa Arena 2026</h1><p>Grupos + mata-mata · Você é o organizador</p></div></div><div class="subnav"><button class="${state.champTab==='overview'?'active':''}" data-champ-tab="overview">Visão geral</button><button class="${state.champTab==='table'?'active':''}" data-champ-tab="table">Classificação</button><button class="${state.champTab==='bracket'?'active':''}" data-champ-tab="bracket">Chaves</button><button class="${state.champTab==='stats'?'active':''}" data-champ-tab="stats">Estatísticas</button></div>${body}`;
}

function matchSheet() {
    const playerRows = (names, prefix) => names.map((name,i)=>`<div class="sheet-player"><span>${i+1}. ${name}</span><span class="counter"><button data-counter="${prefix}-g-${i}" data-delta="-1">−</button><span id="${prefix}-g-${i}">0</span><button data-counter="${prefix}-g-${i}" data-delta="1">+</button></span><span class="counter"><button data-counter="${prefix}-c-${i}" data-delta="-1">−</button><span id="${prefix}-c-${i}">0</span><button data-counter="${prefix}-c-${i}" data-delta="1">+</button></span></div>`).join('');
    return `<section class="panel"><div class="panel-header"><div><h2>Súmula da partida</h2><p>Mocidade FC × Estrela Azul · Controle do organizador</p></div><span class="status pending">Rascunho</span></div><div class="sheet-player"><strong>Atleta</strong><small>⚽ Gols</small><small>🟨 Cartões</small></div><div class="sheet-team"><h3>Mocidade FC</h3>${playerRows(['Rafael Santos','Lucas Almeida','Bruno Lima'],'m')}</div><div class="sheet-team"><h3>Estrela Azul</h3>${playerRows(['Caio Mendes','Felipe Rocha','João Vitor'],'e')}</div><div class="button-row"><button class="ghost-button" data-champ-tab="overview">Cancelar</button><button class="primary-button" id="publishSheet"><i class="fa-solid fa-check"></i> Publicar súmula</button></div></section>`;
}

function rankingModule() {
    return `<div class="page-heading"><div><span class="eyebrow">Módulo principal</span><h1><i class="fa-solid fa-ranking-star"></i> Ranking</h1><p>A classificação geral permanece acessível diretamente na barra principal.</p></div></div><section class="panel"><table class="standings"><thead><tr><th>#</th><th>Equipe</th><th>J</th><th>V</th><th>PTS</th></tr></thead><tbody><tr><td>1</td><td>Mocidade FC</td><td>18</td><td>13</td><td>42</td></tr><tr><td>2</td><td>Estrela Azul</td><td>18</td><td>11</td><td>37</td></tr><tr><td>3</td><td>União da Serra</td><td>17</td><td>10</td><td>33</td></tr></tbody></table></section>`;
}

function renderManagement() {
    if (state.management === 'hub') page.innerHTML = managementHub();
    else if (state.management === 'team') page.innerHTML = teamModule();
    else if (['championships','wizard','champ-detail'].includes(state.management)) page.innerHTML = championshipModule();
    else if (state.management === 'ranking') page.innerHTML = rankingModule();
}

function bindPageEvents() {
    page.querySelectorAll('[data-management]').forEach(button => button.addEventListener('click', () => { if(button.dataset.openTeamTab) state.teamTab=button.dataset.openTeamTab; state.management = button.dataset.management; if(state.management==='wizard') state.wizardStep=1; render(); }));
    page.querySelectorAll('[data-demo-toast]').forEach(button => button.addEventListener('click', () => {
        if (!state.pro && button.closest('.premium-list')) { state.management='team'; render(); return; }
        notify(button.dataset.demoToast);
    }));
    page.querySelectorAll('[data-team-tab]').forEach(button => button.addEventListener('click', () => { state.teamTab = button.dataset.teamTab; render(); }));
    page.querySelectorAll('[data-champ-tab]').forEach(button => button.addEventListener('click', () => { state.champTab = button.dataset.champTab; render(); }));
    page.querySelectorAll('[data-format]').forEach(button => button.addEventListener('click', () => { state.format = button.dataset.format; render(); }));
    page.querySelectorAll('[data-counter]').forEach(button => button.addEventListener('click', () => { const target=document.getElementById(button.dataset.counter); target.textContent=Math.max(0,Number(target.textContent)+Number(button.dataset.delta)); }));
    document.getElementById('upgradeDemo')?.addEventListener('click', () => { state.pro=true; syncPlan(); render(); notify('Plano PRO ativado na demonstração'); });
    document.getElementById('addAthlete')?.addEventListener('click', () => { state.selectedAthlete=null; state.teamTab='form'; render(); });
    page.querySelectorAll('[data-edit-athlete]').forEach(button => button.addEventListener('click', () => { state.selectedAthlete=Number(button.dataset.editAthlete); state.teamTab='form'; render(); }));
    document.getElementById('cancelAthlete')?.addEventListener('click', () => { state.teamTab='elenco'; state.selectedAthlete=null; render(); });
    document.getElementById('saveAthlete')?.addEventListener('click', () => {
        const name=document.getElementById('athleteName').value.trim();
        const number=Number(document.getElementById('athleteNumber').value);
        const position=document.getElementById('athletePosition').value;
        if(!name || !number){ notify('Preencha nome e número da camisa'); return; }
        const existing=athletes.find(a=>a.id===state.selectedAthlete);
        if(existing){ Object.assign(existing,{name,number,position,initials:name.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase()}); }
        else { athletes.push({id:Date.now(),name,number,position,goals:0,cards:0,initials:name.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase()}); }
        state.teamTab='elenco'; state.selectedAthlete=null; notify(existing?'Atleta atualizado':'Atleta adicionado ao elenco'); render();
    });
    document.getElementById('openSheet')?.addEventListener('click', () => { state.champTab='sheet'; render(); });
    document.getElementById('publishSheet')?.addEventListener('click', () => { notify('Súmula publicada e estatísticas atualizadas'); state.champTab='overview'; setTimeout(render,550); });
    const back=document.getElementById('wizardBack'); if(back) back.addEventListener('click',()=>{ if(state.wizardStep>1){state.wizardStep--;render();} });
    const next=document.getElementById('wizardNext'); if(next) next.addEventListener('click',()=>{ if(state.wizardStep<4){state.wizardStep++;render();}else{state.created=true;state.management='championships';notify('Campeonato criado e convites enviados');render();} });
}

function syncPlan() {
    document.getElementById('planLabel').textContent = state.pro ? 'PRO ATIVO' : 'BÁSICO';
    const switcher=document.getElementById('planSwitch'); switcher.setAttribute('aria-pressed',String(state.pro));
    switcher.querySelector('strong').textContent=state.pro?'Plano PRO':'Plano Básico';
    switcher.querySelector('small').textContent=state.pro?'Toque para simular Básico':'Toque para simular PRO';
}

function notify(message) { toast.textContent=message; toast.classList.add('show'); clearTimeout(notify.timer); notify.timer=setTimeout(()=>toast.classList.remove('show'),2200); }

function render() {
    renderNav();
    if (state.route === 'menu') renderManagement(); else if (state.route === 'ranking') page.innerHTML = rankingModule(); else renderTemplate(state.route);
    bindPageEvents();
    window.scrollTo({top:0,behavior:'smooth'});
}

document.getElementById('planSwitch').addEventListener('click', () => { state.pro=!state.pro; syncPlan(); if(state.route==='menu') render(); });
document.getElementById('mobilePlanSwitch').addEventListener('click', () => { state.pro=!state.pro; syncPlan(); if(state.route==='menu') render(); });
syncPlan();
render();
