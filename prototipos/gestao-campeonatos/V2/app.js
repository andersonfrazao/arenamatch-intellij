const navItems = [
    { id: 'agenda', label: 'Agenda', icon: 'fa-regular fa-calendar-days' },
    { id: 'buscar', label: 'Buscar', icon: 'fa-solid fa-magnifying-glass' },
    { id: 'ranking', label: 'Ranking', icon: 'fa-solid fa-list-ol' },
    { id: 'chat', label: 'Chat', icon: 'fa-regular fa-comments' },
    { id: 'menu', label: 'Menu', icon: 'fa-solid fa-bars' }
];

const athletes = [
    { id: 1, name: 'Lucas Almeida', number: 10, position: 'Meia', active: true, present: true, role: 'Titular', matchPosition: 'Meia', goals: 1, yellow: 0, red: false, seasonGoals: 8, games: 12 },
    { id: 2, name: 'Rafael Santos', number: 9, position: 'Atacante', active: true, present: true, role: 'Titular', matchPosition: 'Atacante', goals: 2, yellow: 1, red: false, seasonGoals: 12, games: 11 },
    { id: 3, name: 'Bruno Lima', number: 5, position: 'Volante', active: true, present: true, role: 'Titular', matchPosition: 'Volante', goals: 0, yellow: 0, red: false, seasonGoals: 2, games: 10 },
    { id: 4, name: 'Diego Souza', number: 1, position: 'Goleiro', active: true, present: true, role: 'Titular', matchPosition: 'Goleiro', goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 12 },
    { id: 5, name: 'Marcos Vieira', number: 14, position: 'Lateral', active: true, present: true, role: 'Titular', matchPosition: 'Lateral', goals: 0, yellow: 0, red: false, seasonGoals: 1, games: 8 },
    { id: 6, name: 'André Costa', number: 7, position: 'Atacante', active: true, present: true, role: 'Titular', matchPosition: 'Atacante', goals: 0, yellow: 0, red: false, seasonGoals: 4, games: 9 },
    { id: 7, name: 'Felipe Rocha', number: 2, position: 'Zagueiro', active: true, present: true, role: 'Titular', matchPosition: 'Zagueiro', goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 10 },
    { id: 8, name: 'Caio Mendes', number: 3, position: 'Zagueiro', active: true, present: true, role: 'Titular', matchPosition: 'Zagueiro', goals: 0, yellow: 0, red: false, seasonGoals: 1, games: 11 },
    { id: 9, name: 'João Vitor', number: 6, position: 'Lateral', active: true, present: true, role: 'Titular', matchPosition: 'Lateral', goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 9 },
    { id: 10, name: 'Gustavo Alves', number: 8, position: 'Meia', active: true, present: true, role: 'Titular', matchPosition: 'Meia', goals: 0, yellow: 0, red: false, seasonGoals: 3, games: 10 },
    { id: 11, name: 'Pedro Henrique', number: 11, position: 'Atacante', active: true, present: true, role: 'Titular', matchPosition: 'Atacante', goals: 0, yellow: 0, red: false, seasonGoals: 5, games: 11 },
    { id: 12, name: 'Eduardo Silva', number: 12, position: 'Goleiro', active: true, present: true, role: 'Reserva', matchPosition: 'Goleiro', goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 3 },
    { id: 13, name: 'Matheus Lima', number: 13, position: 'Zagueiro', active: true, present: true, role: 'Reserva', matchPosition: 'Zagueiro', goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 5 },
    { id: 14, name: 'Renan Souza', number: 15, position: 'Volante', active: true, present: true, role: 'Reserva', matchPosition: 'Volante', goals: 0, yellow: 0, red: false, seasonGoals: 1, games: 6 },
    { id: 15, name: 'Thiago Martins', number: 18, position: 'Atacante', active: true, present: true, role: 'Reserva', matchPosition: 'Atacante', goals: 0, yellow: 0, red: false, seasonGoals: 2, games: 5 }
];

const positions = ['Goleiro','Zagueiro','Lateral','Volante','Meia','Atacante'];
const formationSlots = {
    '4-4-2': [
        ['GOL',50,91], ['LE',14,74], ['ZAG',38,78], ['ZAG',62,78], ['LD',86,74],
        ['ME',14,49], ['MC',38,54], ['MC',62,54], ['MD',86,49], ['ATA',38,21], ['ATA',62,21]
    ],
    '4-3-3': [
        ['GOL',50,91], ['LE',14,74], ['ZAG',38,78], ['ZAG',62,78], ['LD',86,74],
        ['VOL',50,61], ['MC',29,48], ['MC',71,48], ['PE',18,23], ['ATA',50,17], ['PD',82,23]
    ],
    '4-2-3-1': [
        ['GOL',50,91], ['LE',14,75], ['ZAG',38,78], ['ZAG',62,78], ['LD',86,75],
        ['VOL',36,60], ['VOL',64,60], ['ME',18,40], ['MEI',50,42], ['MD',82,40], ['ATA',50,17]
    ],
    '3-5-2': [
        ['GOL',50,91], ['ZAG',24,76], ['ZAG',50,79], ['ZAG',76,76],
        ['ALA',11,50], ['VOL',36,59], ['MC',50,47], ['VOL',64,59], ['ALA',89,50], ['ATA',37,20], ['ATA',63,20]
    ]
};
const state = {
    route: 'agenda',
    pro: true,
    screen: 'home',
    teamTab: 'roster',
    matchStep: 1,
    scoreHome: 3,
    scoreAway: 1,
    scoreSaved: false,
    selectedAthlete: null,
    statsPublished: false,
    formation: '4-3-3',
    lineupMode: 'field',
    selectedBoardPlayer: null,
    boardOrder: [4, 5, 7, 8, 9, 3, 1, 10, 6, 2, 11, 12, 13, 14, 15]
};

const page = document.getElementById('page');
const toast = document.getElementById('toast');
let boardDrag = null;
let ignoreBoardClick = false;

function initials(name) {
    return name.split(' ').filter(Boolean).map(word => word[0]).slice(0,2).join('').toUpperCase();
}

function navMarkup(item) {
    return `<a href="#${item.id}" class="nav-item ${state.route === item.id ? 'active' : ''}" data-route="${item.id}"><i class="${item.icon}"></i><span>${item.label}</span></a>`;
}

function renderNav() {
    const markup = navItems.map(navMarkup).join('');
    document.querySelector('.desktop-nav').innerHTML = markup;
    document.querySelector('.bottom-nav').innerHTML = markup;
}

function agendaPage() {
    return `
        <div class="page-heading"><div><span class="eyebrow">Módulo principal</span><h1><i class="fa-regular fa-calendar-days"></i> Minha Agenda</h1><p>Seus jogos, convites e resultados continuam concentrados aqui.</p></div></div>
        <button class="agenda-summary" data-screen="games"><strong>Todos meus jogos</strong><i class="fa-solid fa-chevron-right"></i></button>
        <section class="calendar-card">
            <div class="calendar-head"><button aria-label="Mês anterior"><i class="fa-solid fa-chevron-left"></i></button><h2>Agosto / Setembro 2026</h2><button aria-label="Próximo mês"><i class="fa-solid fa-chevron-right"></i></button></div>
            <p class="calendar-help">Navegue pelas datas para ver jogos e convites</p>
            <div class="legend"><span><i class="fa-solid fa-circle green"></i>Confirmado</span><span><i class="fa-solid fa-circle blue"></i>Realizado</span><span><i class="fa-solid fa-circle purple"></i>Placar pendente</span><span><i class="fa-solid fa-circle yellow"></i>Amistoso pendente</span></div>
            <div class="days"><div class="day"><small>Qui</small><b>27</b></div><div class="day"><small>Sex</small><b>28</b></div><div class="day active"><small>Sáb</small><b>29</b><span class="game-dot"><i class="fa-solid fa-futbol"></i> Jogo</span></div></div>
        </section>
        <h2 class="section-title">Partida que precisa da sua atenção</h2>
        <div class="event-list">
            <article class="event-card"><span class="event-date"><b>29</b><small>AGO</small></span><span class="row-copy"><strong>Mocidade FC × União da Serra</strong><small>16:00 · Arena Central · Partida realizada</small></span><span class="event-actions"><span class="status pending">Placar pendente</span><button class="primary-button" data-screen="score"><i class="fa-solid fa-futbol"></i> Informar placar</button></span></article>
        </div>
        <h2 class="section-title">Próximo compromisso</h2>
        <div class="event-list"><article class="event-card"><span class="event-date"><b>05</b><small>SET</small></span><span class="row-copy"><strong>Mocidade FC × Estrela Azul</strong><small>15:30 · Campo do Mocidade · Amistoso</small></span><span class="event-actions"><span class="status confirmed">Confirmado</span></span></article></div>`;
}

function gamesPage() {
    return `
        <div class="page-heading"><div><button class="ghost-button" data-screen="home"><i class="fa-solid fa-arrow-left"></i> Agenda</button><span class="eyebrow"> Histórico de partidas</span><h1>Todos meus jogos</h1><p>Partidas do Arena Match e o andamento das informações do seu time.</p></div></div>
        <div class="match-list">
            <article class="match-row"><span class="event-date"><b>29</b><small>AGO</small></span><span class="row-copy"><strong>Mocidade FC × União da Serra</strong><small>Partida realizada · Placar ainda não informado</small></span><span class="event-actions"><span class="status pending">Ação necessária</span><button class="primary-button" data-screen="score">Informar placar</button></span></article>
            <article class="match-row"><span class="event-date"><b>22</b><small>AGO</small></span><span class="row-copy"><strong>Mocidade FC 2 × 2 Nacional da Vila</strong><small>Placar informado · Estatísticas em rascunho</small></span><span class="event-actions"><span class="status draft">Rascunho PRO</span><button class="secondary-button" data-open-match="1">Continuar</button></span></article>
            <article class="match-row"><span class="event-date"><b>15</b><small>AGO</small></span><span class="row-copy"><strong>Real Primavera 1 × 2 Mocidade FC</strong><small>Placar e estatísticas concluídos</small></span><span class="event-actions"><span class="status confirmed">Concluído</span></span></article>
        </div>`;
}

function scorePage() {
    return `
        <div class="page-heading"><div><button class="ghost-button" data-screen="home"><i class="fa-solid fa-arrow-left"></i> Agenda</button><span class="eyebrow">Resultado obrigatório</span><h1><i class="fa-solid fa-futbol"></i> Informar placar</h1><p>O placar alimenta o ranking gratuito do Arena Match.</p></div></div>
        <div class="context-card"><i class="fa-regular fa-calendar-check"></i><div><strong>29 de agosto de 2026 · 16:00</strong><small>Arena Central · Amistoso · Partida realizada</small></div></div>
        <section class="panel scoreboard">
            <label class="score-team"><span class="crest">MF</span><strong>Mocidade FC</strong><input id="scoreHome" type="number" min="0" max="99" value="${state.scoreHome}" aria-label="Gols do Mocidade FC"></label>
            <span class="score-x">×</span>
            <label class="score-team"><span class="crest">US</span><strong>União da Serra</strong><input id="scoreAway" type="number" min="0" max="99" value="${state.scoreAway}" aria-label="Gols do União da Serra"></label>
        </section>
        <div class="button-row" style="margin-top:1rem"><button class="ghost-button" data-screen="home">Cancelar</button><button class="primary-button" id="saveScore"><i class="fa-solid fa-check"></i> Salvar placar</button></div>`;
}

function scoreSuccessPage() {
    const action = state.pro
        ? `<button class="primary-button" data-open-match="1"><i class="fa-solid fa-clipboard-list"></i> Adicionar estatísticas da partida</button>`
        : `<button class="primary-button" data-upgrade="1"><i class="fa-solid fa-crown"></i> Conhecer o plano PRO</button>`;
    return `
        <div class="success-card">
            <span class="success-icon"><i class="fa-solid fa-check"></i></span>
            <span class="eyebrow">Resultado registrado</span><h2>Mocidade FC ${state.scoreHome} × ${state.scoreAway} União da Serra</h2>
            <p>O placar foi salvo e será considerado no ranking do Arena Match.</p>
            <div class="pro-offer"><strong><i class="fa-solid fa-crown"></i> Gestão do Time PRO</strong><ul><li>Informe quem participou da partida</li><li>Registre titulares, posições, gols e cartões</li><li>Atualize automaticamente as estatísticas dos atletas</li></ul></div>
            <div class="button-row" style="justify-content:center"><button class="ghost-button" data-screen="home">Agora não</button>${action}</div>
        </div>`;
}

function searchPage() {
    return `<div class="page-heading"><div><span class="eyebrow">Módulo principal</span><h1><i class="fa-solid fa-location-dot"></i> Buscar jogo</h1><p>Encontre adversários por disponibilidade e proximidade.</p></div></div><div class="form-card"><label>Data do jogo<input type="date" value="2026-09-12"></label><label>Categoria<select><option>Esporte (Livre)</option><option>Veterano (35+)</option></select></label><label>Distância máxima<select><option>Até 25 km</option><option>Até 50 km</option></select></label><button class="primary-button" data-toast="Busca simulada"><i class="fa-solid fa-magnifying-glass"></i> Buscar adversários</button></div>`;
}

function rankingPage() {
    return `<div class="page-heading"><div><span class="eyebrow">Módulo principal gratuito</span><h1><i class="fa-solid fa-ranking-star"></i> Ranking</h1><p>Classificação gerada pelos placares informados após as partidas.</p></div></div><section class="panel"><div class="table-wrap"><table class="standings"><thead><tr><th>#</th><th>Equipe</th><th>J</th><th>V</th><th>SG</th><th>PTS</th></tr></thead><tbody><tr><td>1</td><td>Mocidade FC</td><td>18</td><td>13</td><td>+21</td><td>42</td></tr><tr><td>2</td><td>Estrela Azul</td><td>18</td><td>11</td><td>+15</td><td>37</td></tr><tr><td>3</td><td>União da Serra</td><td>17</td><td>10</td><td>+9</td><td>33</td></tr></tbody></table></div></section>`;
}

function chatPage() {
    return `<div class="page-heading"><div><span class="eyebrow">Conversas</span><h1><i class="fa-regular fa-comments"></i> Chat</h1><p>Negociações de partidas e avisos importantes.</p></div></div><div class="menu-list general"><button class="menu-entry" data-toast="Conversa aberta"><span class="crest small">US</span><span class="menu-copy"><strong>União da Serra</strong><small>Obrigado pelo jogo! Placar confirmado.</small></span><span class="soon">10:42</span><i class="fa-solid fa-chevron-right"></i></button><button class="menu-entry" data-toast="Conversa aberta"><span class="crest small">EA</span><span class="menu-copy"><strong>Estrela Azul</strong><small>Combinado! Chegaremos às 15:00.</small></span><span class="soon">Ontem</span><i class="fa-solid fa-chevron-right"></i></button></div>`;
}

function menuPage() {
    const lock = state.pro ? '' : '<i class="fa-solid fa-lock soon"></i>';
    return `
        <div class="page-heading"><div><span class="eyebrow">Módulos e configurações</span><h1><i class="fa-solid fa-bars"></i> Menu</h1><p>O Arena Match continua gratuito para encontrar adversários e marcar jogos. A gestão detalhada do time é PRO.</p></div></div>
        <section class="menu-section"><h2><i class="fa-solid fa-bolt"></i> Gestão do Time <span class="section-badge"><i class="fa-solid fa-crown"></i> PRO</span></h2><div class="menu-list">
            <button class="menu-entry" data-team-tab="roster"><span class="menu-icon"><i class="fa-solid fa-people-group"></i></span><span class="menu-copy"><strong>Meu Elenco</strong><small>Jogadores, camisas e posições habituais</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
            <button class="menu-entry featured-entry" data-open-match="1"><span class="menu-icon"><i class="fa-solid fa-chalkboard-user"></i></span><span class="menu-copy"><strong>Prancheta Tática</strong><small>Formação, campo, titulares e banco de reservas</small></span><span class="status pro">Abrir</span><i class="fa-solid fa-chevron-right"></i></button>
            <button class="menu-entry" data-team-tab="matches"><span class="menu-icon"><i class="fa-solid fa-clipboard-list"></i></span><span class="menu-copy"><strong>Partidas e Estatísticas</strong><small>Escalações, gols, cartões e rascunhos</small></span><span class="count-badge">2</span><i class="fa-solid fa-chevron-right"></i></button>
            <button class="menu-entry" data-team-tab="stats"><span class="menu-icon"><i class="fa-solid fa-chart-line"></i></span><span class="menu-copy"><strong>Desempenho do Time</strong><small>Artilharia, presenças e histórico</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
        </div></section>
        <section class="menu-section"><h2><i class="fa-solid fa-trophy"></i> Campeonatos <span class="section-badge">PRÓXIMA ETAPA</span></h2><div class="menu-list">
            <button class="menu-entry" data-toast="Módulo previsto para uma próxima versão"><span class="menu-icon"><i class="fa-solid fa-diagram-project"></i></span><span class="menu-copy"><strong>Criar Campeonato</strong><small>Grupos, mata-mata ou pontos corridos</small></span><span class="soon">Em breve</span><i class="fa-solid fa-chevron-right"></i></button>
        </div></section>
        <section class="menu-section"><h2><i class="fa-solid fa-sliders"></i> Geral</h2><div class="menu-list general">
            <button class="menu-entry" data-toast="Dados do representante e do time"><span class="menu-icon"><i class="fa-regular fa-id-card"></i></span><span class="menu-copy"><strong>Meus Dados</strong><small>Responsável e informações do time</small></span><i class="fa-solid fa-chevron-right"></i></button>
            <button class="menu-entry" id="menuPlanSwitch"><span class="menu-icon"><i class="fa-solid fa-crown"></i></span><span class="menu-copy"><strong>Plano e Assinatura</strong><small>${state.pro ? 'Plano PRO ativo · toque para simular Básico' : 'Plano Básico · toque para simular PRO'}</small></span><i class="fa-solid fa-chevron-right"></i></button>
            <button class="menu-entry" data-toast="Canal de suporte aberto"><span class="menu-icon"><i class="fa-solid fa-headset"></i></span><span class="menu-copy"><strong>Suporte</strong><small>Fale com o Arena Match</small></span><i class="fa-solid fa-chevron-right"></i></button>
        </div></section>`;
}

function teamPage() {
    if (!state.pro) return paywall();
    let body = '';
    if (state.teamTab === 'roster') body = rosterPanel();
    if (state.teamTab === 'form') body = athleteForm();
    if (state.teamTab === 'matches') body = teamMatchesPanel();
    if (state.teamTab === 'stats') body = statsPanel();
    return `
        <div class="page-heading"><div><button class="ghost-button" data-route="menu"><i class="fa-solid fa-arrow-left"></i> Menu</button><span class="eyebrow"> Gestão do Time · PRO</span><h1><i class="fa-solid fa-people-group"></i> Meu Time</h1><p>Os dados são vinculados às partidas já realizadas no Arena Match.</p></div>${state.teamTab === 'roster' ? '<button class="primary-button" id="addAthlete"><i class="fa-solid fa-user-plus"></i> Novo atleta</button>' : ''}</div>
        <div class="overview-strip"><div class="metric"><strong>${athletes.filter(a => a.active).length}</strong><small>Atletas ativos</small></div><div class="metric"><strong>27</strong><small>Gols registrados</small></div><div class="metric"><strong>2</strong><small>Ações pendentes</small></div></div>
        <div class="subnav"><button class="${['roster','form'].includes(state.teamTab) ? 'active' : ''}" data-team-tab="roster">Elenco</button><button class="${state.teamTab === 'matches' ? 'active' : ''}" data-team-tab="matches">Partidas</button><button class="${state.teamTab === 'stats' ? 'active' : ''}" data-team-tab="stats">Desempenho</button></div>${body}`;
}

function rosterPanel() {
    const rows = athletes.map(a => `<div class="athlete-row"><span class="avatar">${initials(a.name)}</span><span class="row-copy"><strong>${a.number} · ${a.name}</strong><small>${a.position} · ${a.active ? 'Ativo' : 'Inativo'}</small></span><span class="row-stats"><span><b>${a.games}</b> jogos</span><span><b>${a.seasonGoals}</b> gols</span><button class="row-action" data-edit-athlete="${a.id}" aria-label="Editar ${a.name}"><i class="fa-solid fa-pen"></i></button></span></div>`).join('');
    return `<section class="panel"><div class="panel-header"><div><h2>Elenco principal</h2><p>Posição habitual; a posição pode mudar em cada partida.</p></div></div><div class="athlete-list">${rows}</div></section>`;
}

function athleteForm() {
    const athlete = athletes.find(a => a.id === state.selectedAthlete) || { name: '', number: '', position: 'Atacante', active: true };
    return `<section class="panel"><div class="panel-header"><div><h2>${state.selectedAthlete ? 'Editar atleta' : 'Novo atleta'}</h2><p>Cadastre apenas os dados necessários para escalações e estatísticas.</p></div></div><div class="form-card embedded"><label>Nome do atleta<input id="athleteName" value="${athlete.name}" placeholder="Nome completo"></label><label>Número da camisa<input id="athleteNumber" type="number" min="1" max="99" value="${athlete.number}" placeholder="10"></label><label>Posição habitual<select id="athletePosition">${positions.map(p => `<option ${p === athlete.position ? 'selected' : ''}>${p}</option>`).join('')}</select></label><label>Situação<select id="athleteActive"><option value="true" ${athlete.active ? 'selected' : ''}>Ativo</option><option value="false" ${!athlete.active ? 'selected' : ''}>Inativo</option></select></label><div class="button-row"><button class="ghost-button" data-team-tab="roster">Cancelar</button><button class="primary-button" id="saveAthlete"><i class="fa-solid fa-check"></i> Salvar atleta</button></div></div></section>`;
}

function teamMatchesPanel() {
    return `<section class="panel"><div class="panel-header"><div><h2>Partidas do Arena Match</h2><p>Complete somente as informações do seu elenco.</p></div></div><div class="match-list"><article class="match-row"><span class="event-date"><b>29</b><small>AGO</small></span><span class="row-copy"><strong>Mocidade FC ${state.scoreHome} × ${state.scoreAway} União da Serra</strong><small>Placar informado · Estatísticas pendentes</small></span><span class="event-actions"><span class="status pending">Ação necessária</span><button class="primary-button" data-open-match="1"><i class="fa-solid fa-chalkboard-user"></i> Abrir prancheta</button></span></article><article class="match-row"><span class="event-date"><b>22</b><small>AGO</small></span><span class="row-copy"><strong>Mocidade FC 2 × 2 Nacional da Vila</strong><small>Escalação iniciada · Rascunho salvo</small></span><span class="event-actions"><span class="status draft">Rascunho</span><button class="secondary-button" data-open-match="1">Continuar prancheta</button></span></article><article class="match-row"><span class="event-date"><b>15</b><small>AGO</small></span><span class="row-copy"><strong>Real Primavera 1 × 2 Mocidade FC</strong><small>Estatísticas publicadas</small></span><span class="status confirmed">Concluído</span></article></div></section>`;
}

function statsPanel() {
    const sorted = [...athletes].sort((a,b) => b.seasonGoals - a.seasonGoals);
    return `<section class="panel"><div class="panel-header"><div><h2>Desempenho em 2026</h2><p>Dados das partidas com estatísticas publicadas.</p></div><span class="status pro"><i class="fa-solid fa-crown"></i> PRO</span></div><div class="table-wrap"><table class="standings"><thead><tr><th>#</th><th>Atleta</th><th>Jogos</th><th>Titular</th><th>Gols</th><th>Cartões</th></tr></thead><tbody>${sorted.map((a,i) => `<tr><td>${i+1}</td><td>${a.name}<br><small style="color:var(--muted)">${a.position}</small></td><td>${a.games}</td><td>${Math.max(0,a.games-2)}</td><td>${a.seasonGoals}</td><td>${a.yellow + (a.red ? 1 : 0)}</td></tr>`).join('')}</tbody></table></div></section>`;
}

function matchManagementPage() {
    const progress = [1,2,3].map(step => `<span class="${step <= state.matchStep ? 'done' : ''}"></span>`).join('');
    let body = state.matchStep === 1 ? lineupStep() : state.matchStep === 2 ? eventsStep() : reviewStep();
    return `
        <div class="page-heading"><div><button class="ghost-button" data-close-match="1"><i class="fa-solid fa-arrow-left"></i> Partidas</button><span class="eyebrow"> Estatísticas da partida · PRO</span><h1>Mocidade FC ${state.scoreHome} × ${state.scoreAway} União da Serra</h1><p>29 de agosto de 2026 · Os dados abaixo são somente do Mocidade FC.</p></div><span class="status draft">Rascunho salvo</span></div>
        <div class="wizard-progress">${progress}</div><div class="step-labels"><span>Escalação</span><span>Ocorrências</span><span>Revisão</span></div>${body}`;
}

function lineupStep() {
    const rows = athletes.filter(a => a.active).map(a => `<div class="lineup-row"><input type="checkbox" data-present="${a.id}" ${a.present ? 'checked' : ''} aria-label="${a.name} participou"><span class="lineup-player"><strong>${a.number} · ${a.name}</strong><small>Posição habitual: ${a.position}</small></span><select data-role="${a.id}" ${a.present ? '' : 'disabled'} aria-label="Condição de ${a.name}"><option ${a.role === 'Titular' ? 'selected' : ''}>Titular</option><option ${a.role === 'Reserva' ? 'selected' : ''}>Reserva</option></select><select data-match-position="${a.id}" ${a.present ? '' : 'disabled'} aria-label="Posição de ${a.name} na partida">${positions.map(p => `<option ${p === a.matchPosition ? 'selected' : ''}>${p}</option>`).join('')}</select></div>`).join('');
    const field = tacticalBoard();
    return `<section class="panel"><div class="panel-header"><div><h2>Escalação e prancheta</h2><p>Monte o time visualmente ou use a lista para ajustar os participantes.</p></div><span class="status pro">${athletes.filter(a => a.present).length} relacionados</span></div>
        <div class="lineup-view-switch"><button class="${state.lineupMode === 'field' ? 'active' : ''}" data-lineup-mode="field"><i class="fa-solid fa-futbol"></i> Prancheta</button><button class="${state.lineupMode === 'list' ? 'active' : ''}" data-lineup-mode="list"><i class="fa-solid fa-list-check"></i> Lista</button></div>
        ${state.lineupMode === 'field' ? field : `<div class="lineup-list">${rows}</div>`}
        <div class="button-row" style="margin-top:1rem"><button class="ghost-button" data-save-draft="1">Salvar rascunho</button><button class="primary-button" data-next-step="2">Continuar para ocorrências</button></div></section>`;
}

function tacticalBoard() {
    const ordered = state.boardOrder.map(id => athletes.find(athlete => athlete.id === id)).filter(Boolean);
    const starters = ordered.filter(athlete => athlete.present && athlete.role === 'Titular').slice(0, 11);
    const reserves = ordered.filter(athlete => athlete.present && athlete.role === 'Reserva');
    const slots = formationSlots[state.formation];
    const players = slots.map((slot, index) => {
        const athlete = starters[index];
        if (!athlete) return `<button class="field-player empty" style="--x:${slot[1]}%;--y:${slot[2]}%" type="button"><span class="shirt"><i class="fa-solid fa-plus"></i></span><small>${slot[0]}</small></button>`;
        const selected = state.selectedBoardPlayer === athlete.id ? 'selected' : '';
        return `<button class="field-player ${selected}" style="--x:${slot[1]}%;--y:${slot[2]}%" data-board-player="${athlete.id}" type="button" aria-label="${athlete.name}, ${slot[0]}"><span class="shirt">${athlete.number}</span><strong>${firstName(athlete.name)}</strong><small>${slot[0]}</small></button>`;
    }).join('');
    const bench = reserves.map(athlete => `<button class="bench-player ${state.selectedBoardPlayer === athlete.id ? 'selected' : ''}" data-board-player="${athlete.id}" type="button"><span class="mini-shirt">${athlete.number}</span><span><strong>${firstName(athlete.name)}</strong><small>${athlete.position}</small></span></button>`).join('');
    const hint = state.selectedBoardPlayer
        ? '<i class="fa-solid fa-hand-pointer"></i> Agora toque em outro jogador para trocar posição ou realizar uma substituição.'
        : '<i class="fa-solid fa-up-down-left-right"></i> Arraste um jogador sobre outro ou toque em dois jogadores. Campo e banco aceitam trocas.';
    return `<div class="tactical-toolbar"><label>Formação<select id="formationSelect">${Object.keys(formationSlots).map(formation => `<option ${formation === state.formation ? 'selected' : ''}>${formation}</option>`).join('')}</select></label><span class="live-chip"><i class="fa-solid fa-circle"></i> Prancheta do jogo</span></div>
        <div class="board-hint">${hint}</div>
        <div class="pitch-wrap"><div class="pitch" aria-label="Campo tático na formação ${state.formation}"><span class="pitch-half"></span><span class="center-circle"></span><span class="area top"></span><span class="area bottom"></span><span class="goal top"></span><span class="goal bottom"></span>${players}</div></div>
        <div class="bench"><div class="bench-heading"><strong><i class="fa-solid fa-chair"></i> Banco de reservas</strong><small>${reserves.length} jogadores</small></div><div class="bench-list">${bench || '<p class="empty-bench">Nenhum reserva relacionado.</p>'}</div></div>`;
}

function firstName(name) {
    return name.split(' ')[0];
}

function swapBoardPlayers(firstId, secondId) {
    const first = athletes.find(athlete => athlete.id === firstId);
    const second = athletes.find(athlete => athlete.id === secondId);
    if (!first || !second) return;
    const firstIndex = state.boardOrder.indexOf(firstId);
    const secondIndex = state.boardOrder.indexOf(secondId);
    [state.boardOrder[firstIndex], state.boardOrder[secondIndex]] = [state.boardOrder[secondIndex], state.boardOrder[firstIndex]];
    if (first.role !== second.role) [first.role, second.role] = [second.role, first.role];
}

function bindBoardDragging() {
    page.querySelectorAll('[data-board-player]').forEach(player => {
        player.addEventListener('pointerdown', event => {
            if (event.pointerType === 'mouse' && event.button !== 0) return;
            boardDrag = { id: Number(player.dataset.boardPlayer), element: player, pointerId: event.pointerId, startX: event.clientX, startY: event.clientY, moved: false };
            player.setPointerCapture?.(event.pointerId);
        });
        player.addEventListener('pointermove', event => {
            if (!boardDrag || boardDrag.element !== player || boardDrag.pointerId !== event.pointerId) return;
            const deltaX = event.clientX - boardDrag.startX;
            const deltaY = event.clientY - boardDrag.startY;
            if (!boardDrag.moved && Math.hypot(deltaX, deltaY) < 7) return;
            boardDrag.moved = true; ignoreBoardClick = true; event.preventDefault();
            player.classList.add('dragging');
            player.style.transform = player.classList.contains('field-player')
                ? `translate(-50%,-50%) translate(${deltaX}px,${deltaY}px)`
                : `translate(${deltaX}px,${deltaY}px)`;
        });
        player.addEventListener('pointerup', event => finishBoardDrag(event, player));
        player.addEventListener('pointercancel', () => cancelBoardDrag(player));
    });
}

function finishBoardDrag(event, player) {
    if (!boardDrag || boardDrag.element !== player) return;
    const draggedId = boardDrag.id;
    const moved = boardDrag.moved;
    player.releasePointerCapture?.(boardDrag.pointerId);
    player.style.pointerEvents = 'none';
    const target = moved ? document.elementFromPoint(event.clientX, event.clientY)?.closest('[data-board-player]') : null;
    player.style.pointerEvents = '';
    player.style.transform = ''; player.classList.remove('dragging'); boardDrag = null;
    if (target && Number(target.dataset.boardPlayer) !== draggedId) {
        swapBoardPlayers(draggedId, Number(target.dataset.boardPlayer)); state.selectedBoardPlayer = null; notify('Jogadores reposicionados'); render();
        return;
    }
    if (moved) setTimeout(() => { ignoreBoardClick = false; }, 0);
}

function cancelBoardDrag(player) {
    player.style.transform = ''; player.classList.remove('dragging'); boardDrag = null; ignoreBoardClick = false;
}

function eventsStep() {
    const present = athletes.filter(a => a.present);
    const rows = present.map(a => `<div class="event-player"><strong>${a.number} · ${a.name}</strong><span class="counter" data-label="Gols"><button data-counter="${a.id}" data-kind="goals" data-delta="-1">−</button><b>${a.goals}</b><button data-counter="${a.id}" data-kind="goals" data-delta="1">+</button></span><span class="counter" data-label="Amarelos"><button data-counter="${a.id}" data-kind="yellow" data-delta="-1">−</button><b>${a.yellow}</b><button data-counter="${a.id}" data-kind="yellow" data-delta="1">+</button></span><button class="red-toggle ${a.red ? 'active' : ''}" data-red="${a.id}"><i class="fa-solid fa-square"></i> Vermelho</button></div>`).join('');
    const totalGoals = present.reduce((sum,a) => sum + a.goals,0);
    const validationClass = totalGoals === state.scoreHome ? 'validation' : 'validation error';
    const validationText = totalGoals === state.scoreHome ? `Os ${state.scoreHome} gols do placar foram atribuídos.` : `Atribua ${state.scoreHome} gols. No momento há ${totalGoals}.`;
    return `<section class="panel"><div class="panel-header"><div><h2>Gols e cartões</h2><p>Registre apenas as ocorrências dos jogadores do seu time.</p></div></div><div class="events-head"><span>Atleta</span><span>⚽ Gols</span><span>🟨 Amarelos</span><span>🟥 Vermelho</span></div><div class="lineup-list">${rows}</div><div class="${validationClass}"><i class="fa-solid ${totalGoals === state.scoreHome ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i><span>${validationText}</span></div><div class="button-row" style="margin-top:1rem"><button class="ghost-button" data-next-step="1">Voltar</button><button class="ghost-button" data-save-draft="1">Salvar rascunho</button><button class="primary-button" data-next-step="3" ${totalGoals === state.scoreHome ? '' : 'disabled'}>Revisar estatísticas</button></div></section>`;
}

function reviewStep() {
    const present = athletes.filter(a => a.present);
    const starters = present.filter(a => a.role === 'Titular').length;
    const yellow = present.reduce((sum,a) => sum + a.yellow,0);
    const red = present.filter(a => a.red).length;
    const scorers = present.filter(a => a.goals > 0).map(a => `<div><span>${a.name}</span><strong>${a.goals} ${a.goals === 1 ? 'gol' : 'gols'}</strong></div>`).join('');
    return `<section class="panel"><div class="panel-header"><div><h2>Revise antes de publicar</h2><p>A publicação atualizará as estatísticas dos atletas.</p></div></div><div class="review-grid"><div class="review-card"><strong>${present.length}</strong><small>Jogadores participantes</small></div><div class="review-card"><strong>${starters}</strong><small>Titulares</small></div><div class="review-card"><strong>${yellow}</strong><small>Cartões amarelos</small></div><div class="review-card"><strong>${red}</strong><small>Cartões vermelhos</small></div></div><h3>Autores dos gols</h3><div class="review-list">${scorers || '<div><span>Nenhum gol atribuído</span></div>'}</div><div class="validation"><i class="fa-solid fa-circle-check"></i><span>Os gols atribuídos correspondem ao placar ${state.scoreHome} × ${state.scoreAway}.</span></div><div class="button-row" style="margin-top:1rem"><button class="ghost-button" data-next-step="2">Voltar</button><button class="ghost-button" data-save-draft="1">Salvar rascunho</button><button class="primary-button" id="publishStats"><i class="fa-solid fa-check"></i> Publicar estatísticas</button></div></section>`;
}

function publishedPage() {
    return `<div class="success-card"><span class="success-icon"><i class="fa-solid fa-chart-line"></i></span><span class="eyebrow">Estatísticas publicadas</span><h2>Partida concluída</h2><p>A participação, os gols e os cartões foram adicionados ao histórico do Mocidade FC.</p><div class="button-row" style="justify-content:center"><button class="ghost-button" data-route="agenda">Voltar para a Agenda</button><button class="primary-button" data-team-tab="stats"><i class="fa-solid fa-chart-line"></i> Ver desempenho</button></div></div>`;
}

function paywall() {
    return `<div class="page-heading"><button class="ghost-button" data-route="menu"><i class="fa-solid fa-arrow-left"></i> Menu</button></div><div class="paywall"><i class="fa-solid fa-lock"></i><span class="eyebrow" style="display:block;margin-top:1rem">Exclusivo PRO</span><h2>Gestão do Time</h2><p>Agenda, busca, chat, placar e ranking continuam gratuitos. O PRO libera elenco, escalações e estatísticas dos atletas.</p><button class="primary-button" data-upgrade="1"><i class="fa-solid fa-crown"></i> Simular ativação PRO</button></div>`;
}

function render() {
    renderNav();
    if (state.screen === 'match') page.innerHTML = matchManagementPage();
    else if (state.screen === 'published') page.innerHTML = publishedPage();
    else if (state.route === 'agenda') {
        if (state.screen === 'score') page.innerHTML = scorePage();
        else if (state.screen === 'score-success') page.innerHTML = scoreSuccessPage();
        else if (state.screen === 'games') page.innerHTML = gamesPage();
        else page.innerHTML = agendaPage();
    } else if (state.route === 'buscar') page.innerHTML = searchPage();
    else if (state.route === 'ranking') page.innerHTML = rankingPage();
    else if (state.route === 'chat') page.innerHTML = chatPage();
    else if (state.route === 'menu') page.innerHTML = state.screen === 'team' ? teamPage() : menuPage();
    bindEvents();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function bindEvents() {
    document.querySelectorAll('[data-route]').forEach(link => link.addEventListener('click', event => {
        event.preventDefault();
        state.route = link.dataset.route;
        state.screen = link.dataset.route === 'menu' ? 'menu' : 'home';
        render();
    }));
    page.querySelectorAll('[data-screen]').forEach(button => button.addEventListener('click', () => { state.screen = button.dataset.screen; render(); }));
    page.querySelectorAll('[data-toast]').forEach(button => button.addEventListener('click', () => notify(button.dataset.toast)));
    page.querySelectorAll('[data-team-tab]').forEach(button => button.addEventListener('click', () => {
        if (!state.pro) { state.route = 'menu'; state.screen = 'team'; render(); return; }
        state.route = 'menu'; state.screen = 'team'; state.teamTab = button.dataset.teamTab; render();
    }));
    page.querySelectorAll('[data-open-match]').forEach(button => button.addEventListener('click', () => {
        if (!state.pro) { state.route = 'menu'; state.screen = 'team'; render(); return; }
        state.screen = 'match'; state.matchStep = 1; render();
    }));
    page.querySelectorAll('[data-close-match]').forEach(button => button.addEventListener('click', () => { state.route = 'menu'; state.screen = 'team'; state.teamTab = 'matches'; render(); }));
    page.querySelectorAll('[data-next-step]').forEach(button => button.addEventListener('click', () => { state.matchStep = Number(button.dataset.nextStep); render(); }));
    page.querySelectorAll('[data-save-draft]').forEach(button => button.addEventListener('click', () => notify('Rascunho salvo com sucesso')));
    page.querySelectorAll('[data-lineup-mode]').forEach(button => button.addEventListener('click', () => { state.lineupMode = button.dataset.lineupMode; state.selectedBoardPlayer = null; render(); }));
    document.getElementById('formationSelect')?.addEventListener('change', event => { state.formation = event.target.value; state.selectedBoardPlayer = null; render(); });
    page.querySelectorAll('[data-board-player]').forEach(button => button.addEventListener('click', () => {
        if (ignoreBoardClick) { ignoreBoardClick = false; return; }
        const playerId = Number(button.dataset.boardPlayer);
        if (!state.selectedBoardPlayer) { state.selectedBoardPlayer = playerId; render(); return; }
        if (state.selectedBoardPlayer === playerId) { state.selectedBoardPlayer = null; render(); return; }
        swapBoardPlayers(state.selectedBoardPlayer, playerId); state.selectedBoardPlayer = null; notify('Prancheta atualizada'); render();
    }));
    bindBoardDragging();
    page.querySelectorAll('[data-present]').forEach(input => input.addEventListener('change', () => {
        const athlete = athletes.find(a => a.id === Number(input.dataset.present)); athlete.present = input.checked; render();
    }));
    page.querySelectorAll('[data-role]').forEach(select => select.addEventListener('change', () => { athletes.find(a => a.id === Number(select.dataset.role)).role = select.value; }));
    page.querySelectorAll('[data-match-position]').forEach(select => select.addEventListener('change', () => { athletes.find(a => a.id === Number(select.dataset.matchPosition)).matchPosition = select.value; }));
    page.querySelectorAll('[data-counter]').forEach(button => button.addEventListener('click', () => {
        const athlete = athletes.find(a => a.id === Number(button.dataset.counter));
        athlete[button.dataset.kind] = Math.max(0, athlete[button.dataset.kind] + Number(button.dataset.delta)); render();
    }));
    page.querySelectorAll('[data-red]').forEach(button => button.addEventListener('click', () => { const athlete = athletes.find(a => a.id === Number(button.dataset.red)); athlete.red = !athlete.red; render(); }));
    page.querySelectorAll('[data-upgrade]').forEach(button => button.addEventListener('click', () => { state.pro = true; syncPlan(); state.route = 'menu'; state.screen = 'team'; state.teamTab = 'roster'; notify('Plano PRO ativado na demonstração'); render(); }));
    page.querySelectorAll('[data-edit-athlete]').forEach(button => button.addEventListener('click', () => { state.selectedAthlete = Number(button.dataset.editAthlete); state.teamTab = 'form'; render(); }));
    document.getElementById('addAthlete')?.addEventListener('click', () => { state.selectedAthlete = null; state.teamTab = 'form'; render(); });
    document.getElementById('saveAthlete')?.addEventListener('click', saveAthlete);
    document.getElementById('saveScore')?.addEventListener('click', () => {
        state.scoreHome = Math.max(0, Number(document.getElementById('scoreHome').value));
        state.scoreAway = Math.max(0, Number(document.getElementById('scoreAway').value));
        state.scoreSaved = true; state.screen = 'score-success'; render();
    });
    document.getElementById('publishStats')?.addEventListener('click', () => { state.statsPublished = true; state.screen = 'published'; render(); notify('Estatísticas publicadas'); });
    document.getElementById('menuPlanSwitch')?.addEventListener('click', togglePlan);
}

function saveAthlete() {
    const name = document.getElementById('athleteName').value.trim();
    const number = Number(document.getElementById('athleteNumber').value);
    const position = document.getElementById('athletePosition').value;
    const active = document.getElementById('athleteActive').value === 'true';
    if (!name || !number) { notify('Preencha o nome e o número da camisa'); return; }
    const existing = athletes.find(a => a.id === state.selectedAthlete);
    if (existing) Object.assign(existing, { name, number, position, active });
    else {
        const newAthlete = { id: Date.now(), name, number, position, active, present: false, role: 'Reserva', matchPosition: position, goals: 0, yellow: 0, red: false, seasonGoals: 0, games: 0 };
        athletes.push(newAthlete); state.boardOrder.push(newAthlete.id);
    }
    state.selectedAthlete = null; state.teamTab = 'roster'; notify(existing ? 'Atleta atualizado' : 'Atleta adicionado ao elenco'); render();
}

function togglePlan() {
    state.pro = !state.pro; syncPlan(); state.route = 'menu'; state.screen = 'menu'; notify(state.pro ? 'Plano PRO ativado na demonstração' : 'Plano Básico ativado na demonstração'); render();
}

function syncPlan() {
    const label = document.getElementById('planLabel');
    label.innerHTML = state.pro ? '<i class="fa-solid fa-crown"></i> PRO ATIVO' : '<i class="fa-solid fa-shield"></i> BÁSICO';
    const switcher = document.getElementById('planSwitch');
    switcher.setAttribute('aria-pressed', String(state.pro));
    switcher.querySelector('strong').textContent = state.pro ? 'Plano PRO' : 'Plano Básico';
    switcher.querySelector('small').textContent = state.pro ? 'Toque para simular o Básico' : 'Toque para simular o PRO';
}

function notify(message) {
    toast.textContent = message; toast.classList.add('show'); clearTimeout(notify.timer); notify.timer = setTimeout(() => toast.classList.remove('show'), 2400);
}

document.getElementById('planSwitch').addEventListener('click', togglePlan);
document.querySelector('.topbar').addEventListener('click', event => {
    const target = event.target.closest('[data-toast]'); if (target) notify(target.dataset.toast);
});
syncPlan();
render();
