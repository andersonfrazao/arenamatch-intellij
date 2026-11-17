/* Chat de partidas e ligas (ARENA-94)
 *
 * - WebSocket (SockJS/STOMP): inscreve na sala da conversa aberta, reconecta com
 *   espera progressiva e, ao voltar, busca o que chegou enquanto estava offline.
 * - Viewport: no celular a conversa ocupa a área visível (visualViewport), então o
 *   campo e o botão de envio ficam acima do teclado.
 * - Rolagem: só desce sozinha se o leitor já estava no fim; senão mostra
 *   "Novas mensagens".
 * - Composição: Enter envia apenas com teclado físico; o envio não fecha o teclado;
 *   o rascunho sobrevive a atualizações da página (ex.: notificações globais).
 *
 * Mantém os nomes globais usados pelo ChatBean: conectarWebSocketChat e rolarParaFim.
 */
(function (window, document) {
    "use strict";

    if (window.chatIniciado) {
        return;
    }
    window.chatIniciado = true;

    const root = document.documentElement;
    const celular = window.matchMedia("(max-width: 767.98px)");
    const tecladoFisico = window.matchMedia("(hover: hover) and (pointer: fine)");
    const reduzirMovimento = window.matchMedia("(prefers-reduced-motion: reduce)");

    function porId(id) { return document.getElementById(id); }
    function blocoChat() { return porId("formChat:chat"); }
    function campo() { return porId("formChat:mensagemTexto"); }
    function rolagem() { return porId("painelMensagensScroll"); }

    function conversaAtual() {
        const bloco = blocoChat();
        if (!bloco || !bloco.dataset.conversaTipo || !bloco.dataset.conversaId) {
            return null;
        }
        return { tipo: bloco.dataset.conversaTipo, id: bloco.dataset.conversaId };
    }

    /* =================================================================
       WebSocket
       ================================================================= */
    let cliente = null;
    let conectado = false;
    let conectando = false;
    let inscricao = null;
    let salaAtual = null;
    let tentativas = 0;
    let timerReconexao = null;
    let jaConectouAntes = false;

    function rotaDaSala(tipo, id) {
        if (tipo === "JOGO") { return "/topic/chat/" + id; }
        if (tipo === "LIGA") { return "/topic/chat/liga/" + id; }
        return null;
    }

    function mostrarConexao(texto) {
        const alvo = porId("chatConexao");
        if (alvo) {
            alvo.textContent = texto || "";
            alvo.hidden = !texto;
        }
    }

    function atualizarConversa() {
        if (typeof window.rcAtualizarChat === "function" && conversaAtual()) {
            window.rcAtualizarChat();
        }
    }

    function inscrever() {
        if (!conectado || !salaAtual) {
            return;
        }
        if (inscricao) {
            try { inscricao.unsubscribe(); } catch (e) { /* conexão já caiu */ }
        }
        inscricao = cliente.subscribe(salaAtual, function () {
            atualizarConversa();
        });
    }

    function agendarReconexao() {
        if (timerReconexao || !salaAtual) {
            return;
        }
        const espera = Math.min(30000, 1000 * Math.pow(2, tentativas));
        tentativas += 1;
        mostrarConexao("Sem conexão. Tentando reconectar...");
        timerReconexao = window.setTimeout(function () {
            timerReconexao = null;
            conectar();
        }, espera);
    }

    function conectar() {
        if (conectado || conectando || !salaAtual) {
            return;
        }
        if (typeof window.SockJS !== "function" || typeof window.Stomp === "undefined") {
            mostrarConexao("Atualização automática indisponível. Recarregue a página para ver novas mensagens.");
            return;
        }
        conectando = true;
        const socket = new window.SockJS("/ws-arenamatch");
        cliente = window.Stomp.over(socket);
        cliente.debug = null;
        cliente.connect({}, function () {
            conectando = false;
            conectado = true;
            tentativas = 0;
            mostrarConexao("");
            inscrever();
            // Na reconexão, busca o que pode ter chegado enquanto a conexão estava fora.
            if (jaConectouAntes) {
                atualizarConversa();
            }
            jaConectouAntes = true;
        }, function () {
            conectando = false;
            conectado = false;
            inscricao = null;
            agendarReconexao();
        });
    }

    function desinscrever() {
        salaAtual = null;
        if (inscricao) {
            try { inscricao.unsubscribe(); } catch (e) { /* conexão já caiu */ }
            inscricao = null;
        }
        window.clearTimeout(timerReconexao);
        timerReconexao = null;
        mostrarConexao("");
    }

    // Chamado também pelo ChatBean (executeScript) ao selecionar uma conversa.
    window.conectarWebSocketChat = function (tipo, id) {
        if (!tipo || !id || id === "null") {
            return;
        }
        const rota = rotaDaSala(String(tipo), String(id));
        if (!rota) {
            return;
        }
        if (rota === salaAtual && (conectado || conectando)) {
            return;
        }
        salaAtual = rota;
        if (conectado) {
            inscrever();
        } else {
            conectar();
        }
    };

    function sincronizarSala() {
        const conversa = conversaAtual();
        if (conversa) {
            window.conectarWebSocketChat(conversa.tipo, conversa.id);
        } else if (salaAtual) {
            desinscrever();
        }
    }

    // Celulares suspendem a conexão em segundo plano: ao voltar, reconecta ou atualiza.
    document.addEventListener("visibilitychange", function () {
        if (document.visibilityState !== "visible" || !salaAtual) {
            return;
        }
        if (conectado) {
            atualizarConversa();
        } else {
            window.clearTimeout(timerReconexao);
            timerReconexao = null;
            conectar();
        }
    });

    window.addEventListener("online", function () {
        if (salaAtual && !conectado) {
            window.clearTimeout(timerReconexao);
            timerReconexao = null;
            conectar();
        }
    });

    /* =================================================================
       Viewport (teclado, orientação) e modo tela cheia no celular
       ================================================================= */
    function ajustarViewport() {
        const vv = window.visualViewport;
        root.style.setProperty("--chat-vv-altura", (vv ? vv.height : window.innerHeight) + "px");
        root.style.setProperty("--chat-vv-topo", (vv ? vv.offsetTop : 0) + "px");
        if (noFim) {
            descerAteOFim();
        }
    }

    function atualizarModo() {
        const bloco = blocoChat();
        const telaCheia = Boolean(bloco && bloco.classList.contains("chat--conversa") && celular.matches);
        root.classList.toggle("chat-tela-cheia", telaCheia);
    }

    if (window.visualViewport) {
        window.visualViewport.addEventListener("resize", ajustarViewport);
        window.visualViewport.addEventListener("scroll", ajustarViewport);
    }
    window.addEventListener("resize", ajustarViewport);
    window.addEventListener("orientationchange", ajustarViewport);
    if (celular.addEventListener) {
        celular.addEventListener("change", atualizarModo);
    }

    /* =================================================================
       Rolagem das mensagens
       ================================================================= */
    let noFim = true;
    let forcarProxima = true;

    function descerAteOFim() {
        const area = rolagem();
        if (area) {
            area.scrollTop = area.scrollHeight;
        }
        noFim = true;
        const botao = porId("chatNovasBtn");
        if (botao) {
            botao.hidden = true;
        }
    }

    window.rolarParaFim = function (forcar) {
        if (forcar === true || forcarProxima || noFim) {
            forcarProxima = false;
            descerAteOFim();
            return;
        }
        const botao = porId("chatNovasBtn");
        if (botao) {
            botao.hidden = false;
        }
    };

    document.addEventListener("scroll", function (event) {
        if (event.target && event.target.id === "painelMensagensScroll") {
            const area = event.target;
            noFim = area.scrollHeight - area.scrollTop - area.clientHeight < 48;
            if (noFim) {
                const botao = porId("chatNovasBtn");
                if (botao) {
                    botao.hidden = true;
                }
            }
        }
    }, true);

    /* =================================================================
       Composição: Enter, envio sem fechar o teclado e rascunho
       ================================================================= */
    function chaveRascunho() {
        const conversa = conversaAtual();
        return conversa ? "chat-rascunho:" + conversa.tipo + ":" + conversa.id : null;
    }

    function lerRascunho() {
        const chave = chaveRascunho();
        try { return chave ? window.sessionStorage.getItem(chave) : null; } catch (e) { return null; }
    }

    function gravarRascunho(texto) {
        const chave = chaveRascunho();
        if (!chave) { return; }
        try {
            if (texto) { window.sessionStorage.setItem(chave, texto); } else { window.sessionStorage.removeItem(chave); }
        } catch (e) { /* armazenamento indisponível */ }
    }

    // O campo cresce com o texto até o limite do CSS (max-height); depois rola.
    function ajustarAlturaCampo(area) {
        if (!area) { return; }
        area.style.height = "auto";
        area.style.height = area.scrollHeight + 2 + "px";
    }

    function restaurarRascunho() {
        const area = campo();
        const rascunho = lerRascunho();
        if (area && !area.value && rascunho) {
            area.value = rascunho;
            ajustarAlturaCampo(area);
        }
    }

    document.addEventListener("input", function (event) {
        if (event.target && event.target.id === "formChat:mensagemTexto") {
            gravarRascunho(event.target.value);
            ajustarAlturaCampo(event.target);
        }
    });

    // Enter envia só com teclado físico; no celular Enter quebra a linha e o envio é pelo botão.
    window.chatTeclaNoCampo = function (event) {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing && tecladoFisico.matches) {
            event.preventDefault();
            const botao = porId("formChat:enviarMensagem");
            if (botao) {
                botao.click();
            }
            return false;
        }
        return true;
    };

    // Tocar no botão de envio não tira o foco do campo (o teclado continua aberto).
    document.addEventListener("pointerdown", function (event) {
        if (event.target.closest && event.target.closest(".chat-enviar") && document.activeElement === campo()) {
            event.preventDefault();
        }
    });

    window.chatAoEnviar = function () {
        forcarProxima = true;
    };

    window.chatAposEnviar = function (status) {
        const area = campo();
        if (status !== "success" || !area) {
            return;
        }
        area.value = "";
        area.style.height = "";
        gravarRascunho("");
        if (document.activeElement === document.body || document.activeElement === area) {
            area.focus({ preventScroll: true });
        }
    };

    /* =================================================================
       Abrir e voltar da conversa (toque e teclado)
       ================================================================= */
    let itemDeOrigem = null;
    let focarTituloAoAbrir = false;
    let focarItemAoVoltar = false;

    window.chatAoAbrirConversa = function (item) {
        forcarProxima = true;
        itemDeOrigem = item && item.id ? item.id : null;
        focarTituloAoAbrir = true;
    };

    window.chatAoVoltar = function () {
        focarItemAoVoltar = true;
    };

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && root.classList.contains("chat-tela-cheia")) {
            const voltar = porId("formChat:voltarInbox");
            if (voltar) {
                event.preventDefault();
                voltar.click();
            }
        }
    });

    /* =================================================================
       Ciclo de atualização (carga e AJAX)
       ================================================================= */
    let campoTinhaFoco = false;

    function aposAtualizar() {
        atualizarModo();
        sincronizarSala();
        restaurarRascunho();

        if (focarTituloAoAbrir) {
            focarTituloAoAbrir = false;
            const titulo = porId("chatConversaNome");
            if (titulo) {
                titulo.focus({ preventScroll: true });
            }
            if (!celular.matches && blocoChat()) {
                blocoChat().scrollIntoView({ block: "nearest", behavior: reduzirMovimento.matches ? "auto" : "smooth" });
            }
        } else if (focarItemAoVoltar) {
            focarItemAoVoltar = false;
            const item = (itemDeOrigem && porId(itemDeOrigem)) || document.querySelector(".chat-item");
            if (item) {
                item.focus({ preventScroll: false });
            }
        } else if (campoTinhaFoco && document.activeElement === document.body && campo()) {
            // A página foi recriada por outra atualização (ex.: notificações): devolve o foco ao campo.
            campo().focus({ preventScroll: true });
        }
        campoTinhaFoco = false;

        // Conversa aberta agora, envio, ou leitor que já estava no fim: mantém o fim visível.
        if ((forcarProxima || noFim) && rolagem()) {
            window.rolarParaFim(true);
        }
    }

    function iniciar() {
        ajustarViewport();
        aposAtualizar();
        window.rolarParaFim(true);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", iniciar);
    } else {
        iniciar();
    }

    if (window.jQuery) {
        window.jQuery(document).on("pfAjaxSend", function () {
            campoTinhaFoco = document.activeElement === campo();
        });
        window.jQuery(document).on("pfAjaxComplete", aposAtualizar);
    }
})(window, document);
