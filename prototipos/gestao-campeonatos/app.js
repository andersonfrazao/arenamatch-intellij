const navItems = [
    { id: 'agenda', label: 'Agenda', icon: 'fa-regular fa-calendar-days' },
    { id: 'buscar', label: 'Buscar', icon: 'fa-solid fa-magnifying-glass' },
    { id: 'ranking', label: 'Ranking', icon: 'fa-solid fa-list-ol' },
    { id: 'chat', label: 'Chat', icon: 'fa-regular fa-comments' },
    { id: 'menu', label: 'Menu', icon: 'fa-solid fa-bars' }
];

const athletes = [
    { id: 1, name: 'Lucas Almeida', nickname: 'Luquinha', active: true, goals: 8, yellow: 2, red: 0, appearances: 14, initials: 'LA' },
    { id: 2, name: 'Rafael Santos', nickname: 'Rafa', active: true, goals: 12, yellow: 1, red: 0, appearances: 16, initials: 'RS' },
    { id: 3, name: 'Bruno Lima', nickname: '', active: true, goals: 2, yellow: 4, red: 1, appearances: 13, initials: 'BL' },
    { id: 4, name: 'Diego Souza', nickname: 'Muralha', active: true, goals: 0, yellow: 1, red: 0, appearances: 15, initials: 'DS' },
    { id: 5, name: 'Caio Nunes', nickname: '', active: true, goals: 4, yellow: 0, red: 0, appearances: 10, initials: 'CN' },
    { id: 6, name: 'João Vitor', nickname: 'JV', active: true, goals: 3, yellow: 3, red: 0, appearances: 12, initials: 'JV' },
    { id: 7, name: 'Marcos Paulo', nickname: '', active: true, goals: 1, yellow: 2, red: 0, appearances: 9, initials: 'MP' },
    { id: 8, name: 'Felipe Rocha', nickname: 'Lipão', active: true, goals: 5, yellow: 1, red: 0, appearances: 11, initials: 'FR' }
];

const formations = {
    '3-5-2': [
        { id:'gk', label:'Goleiro', x:50, y:88 }, { id:'z1', label:'Zagueiro', x:22, y:68 },
        { id:'z2', label:'Zagueiro', x:50, y:72 }, { id:'z3', label:'Zagueiro', x:78, y:68 },
        { id:'m1', label:'Ala esquerda', x:12, y:45 }, { id:'m2', label:'Meia', x:32, y:51 },
        { id:'m3', label:'Volante', x:50, y:56 }, { id:'m4', label:'Meia', x:68, y:51 },
        { id:'m5', label:'Ala direita', x:88, y:45 }, { id:'a1', label:'Atacante', x:35, y:22 },
        { id:'a2', label:'Atacante', x:65, y:22 }
    ],
    '4-4-2': [
        { id:'gk', label:'Goleiro', x:50, y:88 }, { id:'d1', label:'Lateral esquerdo', x:12, y:68 },
        { id:'d2', label:'Zagueiro', x:38, y:72 }, { id:'d3', label:'Zagueiro', x:62, y:72 },
        { id:'d4', label:'Lateral direito', x:88, y:68 }, { id:'m1', label:'Meia esquerda', x:15, y:45 },
        { id:'m2', label:'Meia', x:38, y:51 }, { id:'m3', label:'Meia', x:62, y:51 },
        { id:'m4', label:'Meia direita', x:85, y:45 }, { id:'a1', label:'Atacante', x:35, y:22 },
        { id:'a2', label:'Atacante', x:65, y:22 }
    ],
    '4-3-3': [
        { id:'gk', label:'Goleiro', x:50, y:88 }, { id:'d1', label:'Lateral esquerdo', x:12, y:68 },
        { id:'d2', label:'Zagueiro', x:38, y:72 }, { id:'d3', label:'Zagueiro', x:62, y:72 },
        { id:'d4', label:'Lateral direito', x:88, y:68 }, { id:'m1', label:'Meia', x:25, y:49 },
        { id:'m2', label:'Volante', x:50, y:56 }, { id:'m3', label:'Meia', x:75, y:49 },
        { id:'a1', label:'Ponta esquerda', x:17, y:23 }, { id:'a2', label:'Atacante', x:50, y:18 },
        { id:'a3', label:'Ponta direita', x:83, y:23 }
    ]
};

const matches = [
    { id:1, opponent:'União da Serra', date:'29/08/2026', time:'16:00', kind:'Amistoso', status:'Agendado', score:'–' },
    { id:2, opponent:'Estrela Azul', date:'22/08/2026', time:'15:30', kind:'Partida principal', status:'Placar confirmado', score:'3 × 1' },
    { id:3, opponent:'Real Primavera', date:'15/08/2026', time:'10:00', kind:'Amistoso', status:'Súmula concluída', score:'2 × 2' }
];

let lineup = {
    matchId: 1, formation:'3-5-2', customFormation:'', status:'Rascunho', selectedAthlete:null,
    slots:{ gk:{athleteId:4,shirt:'1',position:'Goleiro'}, m3:{athleteId:3,shirt:'5',position:'Volante'}, a1:{athleteId:2,shirt:'9',position:'Atacante'} },
    bench:[1], undefinedPlayers:[5], events:{}
};

const teams = ['União da Serra', 'Estrela Azul', 'Real Primavera', 'Vila Nova', 'Atlético Norte', 'Juventude FC', 'Nacional da Vila'];
let state = { route: 'menu', pro: true, management: 'hub', teamTab: 'elenco', champTab: 'overview', wizardStep: 1, format: 'groups', created: false, selectedAthlete: null, teamView:'list', activeMatchId:null };

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
                <button class="menu-entry" data-management="team" data-open-team-tab="elenco"><span class="menu-icon"><i class="fa-solid fa-people-group"></i></span><span><strong>Meu Elenco</strong><small>Cadastro dos atletas ativos e inativos</small></span>${lock}<i class="fa-solid fa-chevron-right"></i></button>
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
    const rows = athletes.map(a => `<div class="athlete-row"><span class="avatar">${a.initials}</span><span class="row-copy"><strong>${a.name}</strong><small>${a.nickname ? `“${a.nickname}” · ` : ''}${a.active ? 'Ativo' : 'Inativo'}</small></span><span class="row-stats"><span><b>${a.appearances}</b> presenças</span><span><b>${a.goals}</b> gols</span><button class="row-action" data-edit-athlete="${a.id}" aria-label="Editar ${a.name}"><i class="fa-solid fa-pen"></i></button></span></div>`).join('');
    return `
        <div class="page-heading"><div><button class="ghost-button" data-management="hub"><i class="fa-solid fa-arrow-left"></i> Menu</button><span class="eyebrow"> Gestão do Time</span><h1><i class="fa-solid fa-people-group"></i> Meu Time</h1><p>O elenco é compartilhado com partidas amistosas e campeonatos.</p></div><button class="primary-button" id="addAthlete"><i class="fa-solid fa-user-plus"></i> Novo atleta</button></div>
        <div class="overview-strip"><div class="metric"><strong>${athletes.filter(a=>a.active).length}</strong><small>Atletas ativos</small></div><div class="metric"><strong>35</strong><small>Gols no período</small></div><div class="metric"><strong>15</strong><small>Cartões</small></div></div>
        <div class="subnav"><button class="${state.teamTab === 'elenco' ? 'active' : ''}" data-team-tab="elenco">Elenco</button><button class="${state.teamTab === 'stats' ? 'active' : ''}" data-team-tab="stats">Estatísticas</button><button class="${state.teamTab === 'matches' ? 'active' : ''}" data-team-tab="matches">Jogos</button></div>
        ${state.teamTab === 'elenco' ? `<section class="panel"><div class="panel-header"><div><h2>Elenco principal</h2><p>Dados permanentes; camisa e posição são definidas por partida</p></div></div><div class="athlete-list">${rows}</div></section>` : teamSecondary()}`;
}

function teamSecondary() {
    if (state.teamTab === 'form') {
        const athlete = athletes.find(a => a.id === state.selectedAthlete) || { name:'', nickname:'', active:true };
        return `<section class="panel"><div class="panel-header"><div><h2>${state.selectedAthlete ? 'Editar atleta' : 'Novo atleta'}</h2><p>Camisa, posição e titularidade serão informadas em cada partida</p></div></div><div class="form-card embedded"><label>Nome completo *<input id="athleteName" value="${athlete.name}" placeholder="Nome do atleta"></label><label>Apelido<input id="athleteNickname" value="${athlete.nickname || ''}" placeholder="Como é conhecido no time"></label><label class="toggle-field"><input id="athleteActive" type="checkbox" ${athlete.active ? 'checked' : ''}><span>Atleta disponível para novas escalações</span></label><div class="button-row"><button class="ghost-button" id="cancelAthlete">Cancelar</button><button class="primary-button" id="saveAthlete"><i class="fa-solid fa-check"></i> Salvar atleta</button></div></div></section>`;
    }
    if (state.teamTab === 'stats') return statsView();
    if (state.teamView === 'lineup') return lineupView();
    if (state.teamView === 'postmatch') return postMatchView();
    return matchesView();
}

function matchesView() {
    const rows = matches.map(match => `<button class="match-row match-button" data-open-match="${match.id}"><span class="event-date"><b>${match.date.slice(0,2)}</b><small>${match.date.slice(3,5)}</small></span><span class="row-copy"><strong>Mocidade FC ${match.score} ${match.opponent}</strong><small>${match.kind} · ${match.date} às ${match.time}</small></span><span class="status ${match.status === 'Agendado' ? 'pending' : 'confirmed'}">${match.status}</span></button>`).join('');
    return `<section class="panel"><div class="panel-header"><div><h2>Jogos e súmulas</h2><p>Planeje antes da partida e conclua o registro após o placar</p></div></div><div class="match-list">${rows}</div></section>`;
}

function formationSlots() {
    if (formations[lineup.formation]) return formations[lineup.formation];
    const rows=(lineup.customFormation||'').split('-').map(Number).filter(value=>value>0 && value<=8);
    if (!rows.length || rows.length>5) return formations['3-5-2'];
    const slots=[{id:'gk',label:'Goleiro',x:50,y:88}];
    rows.forEach((count,rowIndex)=>{
        const y=68-(rowIndex*(48/Math.max(1,rows.length-1)));
        for(let player=0;player<count;player++) slots.push({id:`c${rowIndex}-${player}`,label:rowIndex===0?'Defesa':rowIndex===rows.length-1?'Ataque':'Meio',x:((player+1)*100/(count+1)),y});
    });
    return slots;
}

function relatedIds() {
    return [...Object.values(lineup.slots).map(s=>s.athleteId), ...lineup.bench, ...lineup.undefinedPlayers];
}

function availableAthletes() {
    const related = relatedIds();
    return athletes.filter(a => a.active && !related.includes(a.id));
}

function athleteChip(id, context) {
    const athlete = athletes.find(a=>a.id===id);
    if (!athlete) return '';
    return `<button class="player-chip ${lineup.selectedAthlete===id?'selected':''}" draggable="true" data-athlete="${id}" data-context="${context}"><span class="avatar small-avatar">${athlete.initials}</span><span><strong>${athlete.nickname || athlete.name.split(' ')[0]}</strong><small>${athlete.name}</small></span></button>`;
}

function lineupView() {
    const match = matches.find(m=>m.id===state.activeMatchId) || matches[0];
    const slots = formationSlots();
    const pitch = slots.map(slot => {
        const assignment=lineup.slots[slot.id];
        const athlete=assignment && athletes.find(a=>a.id===assignment.athleteId);
        return `<button class="pitch-slot ${athlete?'filled':''}" style="left:${slot.x}%;top:${slot.y}%" data-slot="${slot.id}" ${athlete?`draggable="true" data-athlete="${athlete.id}"`:''} title="${slot.label}">${athlete ? `<span class="shirt">${assignment.shirt || '–'}</span><strong>${athlete.nickname || athlete.name.split(' ')[0]}</strong>` : `<i class="fa-solid fa-plus"></i><small>${slot.label}</small>`}</button>`;
    }).join('');
    return `<div class="lineup-heading"><button class="ghost-button" id="backToMatches"><i class="fa-solid fa-arrow-left"></i> Jogos</button><div><span class="eyebrow">${match.status}</span><h2>Mocidade FC × ${match.opponent}</h2><p>${match.date} às ${match.time} · ${match.kind}</p></div><span class="status pending">Rascunho salvo</span></div>
        <section class="panel formation-toolbar"><label>Formação inicial<select id="formationSelect">${Object.keys(formations).map(f=>`<option ${f===lineup.formation?'selected':''}>${f}</option>`).join('')}<option ${lineup.formation==='Personalizada'?'selected':''}>Personalizada</option></select></label>${lineup.formation==='Personalizada'?`<label>Descrição<input id="customFormation" value="${lineup.customFormation}" placeholder="Ex.: 2-3-1"></label>`:''}<button class="secondary-button" id="saveDraft"><i class="fa-regular fa-floppy-disk"></i> Salvar rascunho</button></section>
        <div class="lineup-workspace"><section class="pitch-panel"><div class="soccer-pitch" id="soccerPitch">${pitch}</div><p class="interaction-hint"><i class="fa-solid fa-hand-pointer"></i> Toque em um atleta e depois na posição. No desktop, você também pode arrastar.</p></section>
        <aside class="player-pools"><section><h3>Disponíveis <span>${availableAthletes().length}</span></h3><div class="player-list" data-drop-zone="available">${availableAthletes().map(a=>athleteChip(a.id,'available')).join('') || '<p class="empty-copy">Todos foram relacionados.</p>'}</div></section><section><h3>A definir <span>${lineup.undefinedPlayers.length}</span></h3><div class="player-list compact-list" data-drop-zone="undefined">${lineup.undefinedPlayers.map(id=>athleteChip(id,'undefined')).join('') || '<p class="empty-copy">Nenhum atleta pendente.</p>'}</div></section><section><h3>Reservas <span>${lineup.bench.length}</span></h3><div class="player-list compact-list" data-drop-zone="bench">${lineup.bench.map(id=>athleteChip(id,'bench')).join('') || '<p class="empty-copy">Nenhum reserva.</p>'}</div></section></aside></div>
        <div class="button-row lineup-actions"><button class="ghost-button" id="addUndefined"><i class="fa-solid fa-user-plus"></i> Relacionar selecionado</button><button class="secondary-button" id="moveToBench"><i class="fa-solid fa-chair"></i> Mover para reservas</button>${match.status==='Placar confirmado'?'<button class="primary-button" id="openPostMatch"><i class="fa-solid fa-clipboard-check"></i> Preencher pós-jogo</button>':''}</div>
        <dialog class="player-dialog" id="playerDialog"><form method="dialog"><button class="dialog-close" value="cancel" aria-label="Fechar">×</button><h3 id="dialogPlayerName">Editar atleta na partida</h3><label>Camisa nesta partida<input id="matchShirt" type="number" min="0" max="999" placeholder="Opcional"></label><label>Posição nesta partida<input id="matchPosition" placeholder="Opcional"></label><div class="button-row"><button class="danger-button" id="removeFromLineup" value="cancel">Remover</button><button class="primary-button" id="savePlayerAssignment" value="cancel">Salvar</button></div></form></dialog>`;
}

function postMatchView() {
    const match=matches.find(m=>m.id===state.activeMatchId);
    const rows=relatedIds().map(id=>{ const a=athletes.find(x=>x.id===id); const ev=lineup.events[id]||{goals:0,yellow:0,red:0}; return `<div class="stat-entry"><span class="avatar">${a.initials}</span><span class="row-copy"><strong>${a.name}</strong><small>Presença confirmada nesta súmula</small></span>${['goals','yellow','red'].map(type=>`<label><small>${type==='goals'?'Gols':type==='yellow'?'Amarelos':'Vermelhos'}</small><input class="event-input" type="number" min="0" value="${ev[type]}" data-event-athlete="${id}" data-event-type="${type}"></label>`).join('')}</div>`; }).join('');
    return `<div class="lineup-heading"><button class="ghost-button" id="backToLineup"><i class="fa-solid fa-arrow-left"></i> Escalação</button><div><span class="eyebrow">Pós-jogo</span><h2>Mocidade FC ${match.score} ${match.opponent}</h2><p>Complete os eventos e finalize a súmula</p></div></div><section class="panel"><div class="postmatch-head"><span><b>${relatedIds().length}</b><small> presenças</small></span><span><b>${match.score}</b><small> placar confirmado</small></span><span><b>1</b><small> gol sofrido pelo time</small></span></div><div class="stat-header"><span>Atleta</span><span>Eventos individuais</span></div>${rows}<div class="button-row lineup-actions"><button class="secondary-button" id="saveEvents">Salvar rascunho</button><button class="primary-button" id="finishSheet"><i class="fa-solid fa-check"></i> Finalizar súmula</button></div></section>`;
}

function statsView() {
    const ranking=[...athletes].sort((a,b)=>b.appearances-a.appearances);
    return `<section class="panel"><div class="panel-header stats-filter"><div><h2>Scout do time</h2><p>Dados originados das súmulas finalizadas</p></div><div class="date-filter"><label>De<input type="date" value="2026-01-01"></label><label>Até<input type="date" value="2026-08-31"></label><button class="secondary-button" id="applyStats">Aplicar</button></div></div><div class="highlight-grid"><article><i class="fa-solid fa-calendar-check"></i><small>Mais presente</small><strong>Rafael Santos</strong><b>16 jogos</b></article><article><i class="fa-solid fa-futbol"></i><small>Artilheiro</small><strong>Rafael Santos</strong><b>12 gols</b></article><article><i class="fa-solid fa-square"></i><small>Mais cartões</small><strong>Bruno Lima</strong><b>4 amarelos · 1 vermelho</b></article></div><div class="table-scroll"><table class="standings"><thead><tr><th>#</th><th>Atleta</th><th>Presenças</th><th>Gols</th><th>Amarelos</th><th>Vermelhos</th></tr></thead><tbody>${ranking.map((a,i)=>`<tr><td>${i+1}</td><td>${a.name}<small class="table-nickname">${a.nickname||''}</small></td><td>${a.appearances}</td><td>${a.goals}</td><td>${a.yellow}</td><td>${a.red}</td></tr>`).join('')}</tbody></table></div></section>`;
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

function detachAthlete(id) {
    Object.keys(lineup.slots).forEach(slotId => { if (lineup.slots[slotId].athleteId === id) delete lineup.slots[slotId]; });
    lineup.bench = lineup.bench.filter(playerId=>playerId!==id);
    lineup.undefinedPlayers = lineup.undefinedPlayers.filter(playerId=>playerId!==id);
}

function moveAthlete(id, destination) {
    if (!id) return;
    detachAthlete(id);
    if (destination === 'bench') lineup.bench.push(id);
    if (destination === 'undefined') lineup.undefinedPlayers.push(id);
    lineup.selectedAthlete = null;
    render();
}

function assignToSlot(id, slotId) {
    if (!id) return;
    const previous = lineup.slots[slotId];
    if (previous && previous.athleteId !== id) lineup.undefinedPlayers.push(previous.athleteId);
    detachAthlete(id);
    const slot=formationSlots().find(item=>item.id===slotId);
    lineup.slots[slotId]={athleteId:id,shirt:'',position:slot?.label||''};
    lineup.selectedAthlete=null;
    render();
}

function openPlayerDialog(slotId) {
    const assignment=lineup.slots[slotId];
    if (!assignment) return;
    const athlete=athletes.find(a=>a.id===assignment.athleteId);
    const dialog=document.getElementById('playerDialog');
    dialog.dataset.slot=slotId;
    document.getElementById('dialogPlayerName').textContent=athlete.name;
    document.getElementById('matchShirt').value=assignment.shirt||'';
    document.getElementById('matchPosition').value=assignment.position||'';
    dialog.showModal();
}

function storeEventDraft() {
    page.querySelectorAll('[data-event-athlete]').forEach(input=>{
        const id=Number(input.dataset.eventAthlete);
        lineup.events[id]=lineup.events[id]||{goals:0,yellow:0,red:0};
        lineup.events[id][input.dataset.eventType]=Number(input.value)||0;
    });
}

function bindPageEvents() {
    page.querySelectorAll('[data-management]').forEach(button => button.addEventListener('click', () => { if(button.dataset.openTeamTab){ state.teamTab=button.dataset.openTeamTab; state.teamView='list'; } state.management = button.dataset.management; if(state.management==='wizard') state.wizardStep=1; render(); }));
    page.querySelectorAll('[data-demo-toast]').forEach(button => button.addEventListener('click', () => {
        if (!state.pro && button.closest('.premium-list')) { state.management='team'; render(); return; }
        notify(button.dataset.demoToast);
    }));
    page.querySelectorAll('[data-team-tab]').forEach(button => button.addEventListener('click', () => { state.teamTab = button.dataset.teamTab; state.teamView='list'; render(); }));
    page.querySelectorAll('[data-champ-tab]').forEach(button => button.addEventListener('click', () => { state.champTab = button.dataset.champTab; render(); }));
    page.querySelectorAll('[data-format]').forEach(button => button.addEventListener('click', () => { state.format = button.dataset.format; render(); }));
    page.querySelectorAll('[data-counter]').forEach(button => button.addEventListener('click', () => { const target=document.getElementById(button.dataset.counter); target.textContent=Math.max(0,Number(target.textContent)+Number(button.dataset.delta)); }));
    document.getElementById('upgradeDemo')?.addEventListener('click', () => { state.pro=true; syncPlan(); render(); notify('Plano PRO ativado na demonstração'); });
    document.getElementById('addAthlete')?.addEventListener('click', () => { state.selectedAthlete=null; state.teamTab='form'; render(); });
    page.querySelectorAll('[data-edit-athlete]').forEach(button => button.addEventListener('click', () => { state.selectedAthlete=Number(button.dataset.editAthlete); state.teamTab='form'; render(); }));
    document.getElementById('cancelAthlete')?.addEventListener('click', () => { state.teamTab='elenco'; state.selectedAthlete=null; render(); });
    document.getElementById('saveAthlete')?.addEventListener('click', () => {
        const name=document.getElementById('athleteName').value.trim();
        const nickname=document.getElementById('athleteNickname').value.trim();
        const active=document.getElementById('athleteActive').checked;
        if(!name){ notify('Preencha o nome do atleta'); return; }
        const existing=athletes.find(a=>a.id===state.selectedAthlete);
        if(existing){ Object.assign(existing,{name,nickname,active,initials:name.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase()}); }
        else { athletes.push({id:Date.now(),name,nickname,active,goals:0,yellow:0,red:0,appearances:0,initials:name.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase()}); }
        state.teamTab='elenco'; state.selectedAthlete=null; notify(existing?'Atleta atualizado':'Atleta adicionado ao elenco'); render();
    });
    page.querySelectorAll('[data-open-match]').forEach(button=>button.addEventListener('click',()=>{ state.activeMatchId=Number(button.dataset.openMatch); lineup.matchId=state.activeMatchId; state.teamView='lineup'; render(); }));
    document.getElementById('backToMatches')?.addEventListener('click',()=>{ state.teamView='list'; render(); });
    document.getElementById('backToLineup')?.addEventListener('click',()=>{ state.teamView='lineup'; render(); });
    document.getElementById('formationSelect')?.addEventListener('change',event=>{
        const assigned=Object.values(lineup.slots).map(item=>item.athleteId);
        lineup.undefinedPlayers=[...new Set([...lineup.undefinedPlayers,...assigned])];
        lineup.slots={}; lineup.formation=event.target.value; render();
        notify('Formação alterada; atletas preservados em A definir');
    });
    document.getElementById('customFormation')?.addEventListener('change',event=>{
        const assigned=Object.values(lineup.slots).map(item=>item.athleteId);
        lineup.undefinedPlayers=[...new Set([...lineup.undefinedPlayers,...assigned])]; lineup.slots={};
        lineup.customFormation=event.target.value; render(); notify('Formação personalizada atualizada');
    });
    document.getElementById('saveDraft')?.addEventListener('click',()=>notify('Rascunho salvo. Você pode continuar depois.'));
    page.querySelectorAll('[data-athlete]').forEach(button=>{
        button.addEventListener('click',()=>{ lineup.selectedAthlete=Number(button.dataset.athlete); render(); });
        button.addEventListener('dragstart',event=>event.dataTransfer.setData('text/plain',button.dataset.athlete));
    });
    page.querySelectorAll('[data-slot]').forEach(slot=>{
        slot.addEventListener('click',()=>{ const assignment=lineup.slots[slot.dataset.slot]; if(assignment) openPlayerDialog(slot.dataset.slot); else if(lineup.selectedAthlete) assignToSlot(lineup.selectedAthlete,slot.dataset.slot); else notify('Selecione um atleta primeiro'); });
        slot.addEventListener('dragover',event=>event.preventDefault());
        slot.addEventListener('drop',event=>{ event.preventDefault(); assignToSlot(Number(event.dataTransfer.getData('text/plain')),slot.dataset.slot); });
    });
    page.querySelectorAll('[data-drop-zone]').forEach(zone=>{
        zone.addEventListener('dragover',event=>event.preventDefault());
        zone.addEventListener('drop',event=>{ event.preventDefault(); const target=zone.dataset.dropZone; const id=Number(event.dataTransfer.getData('text/plain')); if(target==='available'){detachAthlete(id);lineup.selectedAthlete=null;render();}else moveAthlete(id,target); });
    });
    document.getElementById('addUndefined')?.addEventListener('click',()=>lineup.selectedAthlete?moveAthlete(lineup.selectedAthlete,'undefined'):notify('Selecione um atleta disponível'));
    document.getElementById('moveToBench')?.addEventListener('click',()=>lineup.selectedAthlete?moveAthlete(lineup.selectedAthlete,'bench'):notify('Selecione um atleta'));
    document.getElementById('savePlayerAssignment')?.addEventListener('click',()=>{ const dialog=document.getElementById('playerDialog'); const assignment=lineup.slots[dialog.dataset.slot]; assignment.shirt=document.getElementById('matchShirt').value; assignment.position=document.getElementById('matchPosition').value.trim(); dialog.close(); render(); notify('Dados da partida atualizados'); });
    document.getElementById('removeFromLineup')?.addEventListener('click',()=>{ const dialog=document.getElementById('playerDialog'); const id=lineup.slots[dialog.dataset.slot].athleteId; detachAthlete(id); dialog.close(); render(); notify('Atleta removido da escalação'); });
    document.getElementById('openPostMatch')?.addEventListener('click',()=>{state.teamView='postmatch';render();});
    document.getElementById('saveEvents')?.addEventListener('click',()=>{storeEventDraft();notify('Eventos salvos como rascunho');});
    document.getElementById('finishSheet')?.addEventListener('click',()=>{storeEventDraft();lineup.status='Finalizada';notify('Súmula finalizada e scout atualizado');state.teamTab='stats';state.teamView='list';setTimeout(render,500);});
    document.getElementById('applyStats')?.addEventListener('click',()=>notify('Scout atualizado para o período informado'));
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
