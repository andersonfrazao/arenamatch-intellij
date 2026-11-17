(function () {
    "use strict";

    const formulario = document.getElementById("formChat");
    if (!formulario) return;
    document.body.classList.add("am-page-chat");

    const estados = new Map();
    const viewport = window.visualViewport;
    const desktop = window.matchMedia("(min-width: 62rem)");
    const navegacao = document.getElementById("amBottomNav");
    const container = document.getElementById("chatContainer");
    let chaveAtual = "";
    let ultimaConversa = "";
    let rolagemInbox = 0;
    let focoInbox = null;
    let idFocoInbox = null;
    let ocupado = false;
    let atualizando = false;
    let falhaAtualizacao = false;
    let atualizacaoPendente = false;
    let temporizadorAtualizacao = null;
    let temporizadorReconexao = null;
    let cliente = null;
    let inscricao = null;
    let salaInscrita = "";
    let geracaoConexao = 0;
    let conectado = false;
    let quadroMedidas = null;

    function conversaAtual() {
        const painel = document.getElementById("formChat:painelChat");
        const tipo = painel.dataset.tipo;
        const id = painel.dataset.conversa;
        return tipo && id ? { chave: tipo + ":" + id, rota: tipo === "JOGO" ? "/topic/chat/" + id : "/topic/chat/liga/" + id } : { chave: "", rota: "" };
    }

    function guardarEstado() {
        const area = document.getElementById("painelMensagensScroll");
        if (!area || !chaveAtual) return;
        const campo = document.getElementById("formChat:novaMensagem");
        const anterior = estados.get(chaveAtual) || {};
        const topo = area.getBoundingClientRect().top;
        const ancora = Array.from(area.querySelectorAll(".chat-linha"))
            .find(item => item.getBoundingClientRect().bottom > topo);
        estados.set(chaveAtual, {
            texto: campo ? campo.value : anterior.texto,
            inicio: campo ? campo.selectionStart : anterior.inicio,
            fim: campo ? campo.selectionEnd : anterior.fim,
            topo: area.scrollTop,
            seguirFim: area.scrollHeight - area.scrollTop - area.clientHeight < 64,
            ancora: ancora ? ancora.id : null,
            deslocamento: ancora ? ancora.getBoundingClientRect().top - topo : 0,
            ultimaMensagem: area.querySelector(".chat-linha:last-child")?.id
        });
    }

    function restaurarRolagem(forcarFim) {
        const area = document.getElementById("painelMensagensScroll");
        if (!area || !chaveAtual) return;
        const estado = estados.get(chaveAtual);
        if (forcarFim || !estado || estado.seguirFim) {
            area.scrollTop = area.scrollHeight;
            document.getElementById("chatNovasMensagens").hidden = true;
        } else {
            const ancora = estado.ancora && document.getElementById(estado.ancora);
            area.scrollTop = estado.topo;
            if (ancora) area.scrollTop += ancora.getBoundingClientRect().top - area.getBoundingClientRect().top - estado.deslocamento;
            const ultima = area.querySelector(".chat-linha:last-child")?.id;
            if (ultima !== estado.ultimaMensagem) {
                document.getElementById("chatNovasMensagens").hidden = false;
                document.getElementById("chatLeituraStatus").textContent = "Novas mensagens na conversa.";
            }
        }
    }

    function restaurarTexto() {
        const campo = document.getElementById("formChat:novaMensagem");
        const estado = estados.get(chaveAtual);
        if (campo && estado && estado.texto !== undefined && campo.value !== estado.texto) {
            campo.value = estado.texto;
            campo.setSelectionRange(estado.inicio || 0, estado.fim || 0);
        }
    }

    function atualizarMedidas() {
        quadroMedidas = null;
        const topo = viewport ? viewport.offsetTop : 0;
        const altura = viewport ? viewport.height : window.innerHeight;
        const limitesNav = navegacao.getBoundingClientRect();
        const reservaNav = Math.max(0, Math.min(topo + altura, limitesNav.bottom) - Math.max(topo, limitesNav.top));
        const espacoInferior = desktop.matches ? parseFloat(window.getComputedStyle(formulario.parentElement).paddingBottom) : 0;
        const inicio = desktop.matches ? Math.max(topo, container.getBoundingClientRect().top) : topo;
        formulario.style.setProperty("--chat-topo", topo + "px");
        formulario.style.setProperty("--chat-altura", Math.max(0, topo + altura - inicio - reservaNav - espacoInferior) + "px");
        restaurarRolagem(false);
    }

    function agendarMedidas() {
        if (quadroMedidas === null) quadroMedidas = window.requestAnimationFrame(atualizarMedidas);
    }

    function estadoConexao(texto, permitirTentativa) {
        document.getElementById("chatEstadoConexao").textContent = texto;
        document.getElementById("chatReconectar").hidden = !permitirTentativa;
        document.getElementById("chatConexao").hidden = !texto;
    }

    function acaoEmCurso(valor) {
        ocupado = valor;
        formulario.setAttribute("aria-busy", String(valor));
        document.getElementById("chatAcaoStatus").hidden = !valor;
    }

    function focalizarErro() {
        const mensagens = document.getElementById("formChat:msgs");
        if (!mensagens.querySelector(".ui-messages-error, .ui-messages-warn")) return false;
        mensagens.focus({ preventScroll: true });
        return true;
    }

    function solicitarAtualizacao() {
        if (!atualizacaoPendente || ocupado || atualizando || document.hidden) return;
        atualizacaoPendente = false;
        atualizando = true;
        window.rcAtualizarChat();
    }

    function agendarAtualizacao() {
        atualizacaoPendente = true;
        if (temporizadorAtualizacao !== null) return;
        temporizadorAtualizacao = window.setTimeout(function () {
            temporizadorAtualizacao = null;
            solicitarAtualizacao();
        }, 100);
    }

    function sincronizarSala() {
        if (!conectado) return;
        const rota = conversaAtual().rota;
        if (rota === salaInscrita) return;
        if (inscricao) inscricao.unsubscribe();
        inscricao = rota ? cliente.subscribe(rota, agendarAtualizacao) : null;
        salaInscrita = rota;
    }

    function conectar() {
        window.clearTimeout(temporizadorReconexao);
        const geracao = ++geracaoConexao;
        if (cliente) cliente.disconnect();
        conectado = false;
        inscricao = null;
        salaInscrita = "";
        estadoConexao("Conectando…", false);
        if (!window.SockJS || !window.Stomp) {
            estadoConexao("Conexão indisponível. Tente novamente.", true);
            return;
        }
        cliente = window.Stomp.over(new window.SockJS(formulario.dataset.websocket));
        cliente.debug = null;
        cliente.connect({}, function () {
            if (geracao !== geracaoConexao) return;
            conectado = true;
            cliente.subscribe("/topic/notificacoes/" + formulario.dataset.time, function (evento) {
                if (evento.body === "CHEGOU_CHAT" || evento.body === "CHEGOU_CONVITE") agendarAtualizacao();
            });
            sincronizarSala();
            estadoConexao("", false);
            agendarAtualizacao();
        }, function () {
            if (geracao !== geracaoConexao) return;
            conectado = false;
            estadoConexao("Conexão interrompida. Tentando reconectar…", true);
            temporizadorReconexao = window.setTimeout(conectar, 5000);
        });
    }

    function concluirSelecao() {
        const novaChave = conversaAtual().chave;
        const anterior = chaveAtual;
        chaveAtual = novaChave;
        if (anterior) ultimaConversa = anterior;
        acaoEmCurso(false);
        sincronizarSala();
        restaurarTexto();
        restaurarRolagem(false);
        document.getElementById("chatInboxLista").scrollTop = rolagemInbox;
        if (!focalizarErro()) {
            let destino = document.getElementById("chatConversaTitulo");
            if (!novaChave) destino = Array.from(formulario.querySelectorAll("a[data-conversa]"))
                .find(item => item.dataset.conversa === ultimaConversa) || document.getElementById("chatInboxTitulo");
            destino?.focus({ preventScroll: true });
        }
        guardarEstado();
        agendarMedidas();
        solicitarAtualizacao();
    }

    window.ArenaChat = {
        prepararSelecao: function () {
            guardarEstado();
            rolagemInbox = document.getElementById("chatInboxLista").scrollTop;
            acaoEmCurso(true);
        },
        concluirSelecao: concluirSelecao,
        prepararAtualizacao: function () {
            falhaAtualizacao = false;
            rolagemInbox = document.getElementById("chatInboxLista").scrollTop;
            const conversaFocada = document.activeElement?.closest("a[data-conversa]");
            focoInbox = conversaFocada ? conversaFocada.dataset.conversa : null;
            idFocoInbox = conversaFocada ? conversaFocada.id : null;
            guardarEstado();
        },
        concluirAtualizacao: function (args) {
            atualizando = false;
            if (falhaAtualizacao || (args && args.chatAtualizado === false)) {
                estadoConexao("Não foi possível atualizar a conversa. Tente novamente.", true);
                return;
            }
            const novaChave = conversaAtual().chave;
            if (novaChave !== chaveAtual) {
                concluirSelecao();
                return;
            }
            restaurarTexto();
            restaurarRolagem(false);
            document.getElementById("chatInboxLista").scrollTop = rolagemInbox;
            if (focoInbox && (document.activeElement === document.body || document.activeElement?.id === idFocoInbox)) {
                Array.from(formulario.querySelectorAll("a[data-conversa]"))
                    .find(item => item.dataset.conversa === focoInbox)?.focus({ preventScroll: true });
            }
            guardarEstado();
            agendarMedidas();
            if (conectado) estadoConexao("", false);
            solicitarAtualizacao();
        },
        falhouAtualizacao: function () {
            atualizando = false;
            falhaAtualizacao = true;
            estadoConexao("Falha ao atualizar a conversa. Tente novamente.", true);
        },
        prepararEnvio: function () {
            if (ocupado) return false;
            guardarEstado();
            acaoEmCurso(true);
            return true;
        },
        concluirEnvio: function (args) {
            const enviada = args && args.mensagemEnviada === true;
            if (enviada) estados.delete(chaveAtual);
            else restaurarTexto();
            acaoEmCurso(false);
            restaurarRolagem(enviada);
            if (!focalizarErro()) document.getElementById("formChat:novaMensagem")?.focus({ preventScroll: true });
            guardarEstado();
            agendarMedidas();
            solicitarAtualizacao();
        },
        falhouAcao: function () {
            acaoEmCurso(false);
            restaurarTexto();
            estadoConexao("Falha na conexão. Confira o histórico antes de tentar enviar novamente.", true);
        }
    };

    document.addEventListener("keydown", function (evento) {
        if (evento.target.id === "formChat:novaMensagem" && evento.key === "Enter" && !evento.shiftKey && !evento.isComposing) {
            evento.preventDefault();
            const enviar = document.getElementById("formChat:enviar");
            if (enviar && !enviar.disabled && !ocupado) enviar.click();
        }
    });
    document.addEventListener("input", function (evento) {
        if (evento.target.id === "formChat:novaMensagem") guardarEstado();
    });
    document.addEventListener("scroll", function (evento) {
        if (evento.target.id === "painelMensagensScroll") {
            guardarEstado();
            if (estados.get(chaveAtual)?.seguirFim) document.getElementById("chatNovasMensagens").hidden = true;
        }
        if (evento.target.id === "chatInboxLista") rolagemInbox = evento.target.scrollTop;
    }, true);
    document.addEventListener("click", function (evento) {
        if (evento.target.closest("#chatNovasMensagens")) {
            restaurarRolagem(true);
            guardarEstado();
            document.getElementById("painelMensagensScroll").focus({ preventScroll: true });
        }
        if (evento.target.closest("#chatReconectar")) {
            if (conectado) agendarAtualizacao();
            else conectar();
        }
    });
    document.addEventListener("visibilitychange", function () {
        if (!document.hidden) agendarAtualizacao();
    });
    window.addEventListener("online", conectar);
    window.addEventListener("resize", agendarMedidas);
    window.addEventListener("scroll", agendarMedidas);
    if (viewport) {
        viewport.addEventListener("resize", agendarMedidas);
        viewport.addEventListener("scroll", agendarMedidas);
    }
    desktop.addEventListener("change", agendarMedidas);
    const medidas = new ResizeObserver(agendarMedidas);
    medidas.observe(navegacao);
    window.addEventListener("pagehide", function () {
        geracaoConexao += 1;
        window.clearTimeout(temporizadorReconexao);
        window.clearTimeout(temporizadorAtualizacao);
        if (cliente) cliente.disconnect();
    });
    window.addEventListener("pageshow", function (evento) {
        if (evento.persisted) conectar();
    });

    chaveAtual = conversaAtual().chave;
    atualizarMedidas();
    restaurarRolagem(true);
    guardarEstado();
    conectar();
}());
