package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import br.com.arenamatch.jsf.client.ChatClient;
import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
@Slf4j
public class ChatBean implements Serializable {

    @Inject
    private ChatClient chatClient;

    @Inject
    private SessaoBean sessaoBean;

    @Getter @Setter
    private List<ConversaInboxDTO> inbox = new ArrayList<>();

    @Getter @Setter
    private ConversaInboxDTO conversaSelecionada;

    @Getter @Setter
    private List<MensagemChatDTO> mensagens = new ArrayList<>();

    @Getter @Setter
    private String novaMensagem;

    @Getter
    private boolean falhaInbox;

    @PostConstruct
    public void init() {
        carregarInbox();
        selecionarConversaPorParametro();
    }

    public void carregarInbox() {
        try {
            atualizarInbox();
        } catch (Exception e) {
            log.error("Erro ao carregar conversas", e);
            falhaInbox = true;
            mensagem(FacesMessage.SEVERITY_ERROR, "Não foi possível carregar suas conversas. Tente atualizar a lista.");
        }
    }

    private void atualizarInbox() {
        inbox = chatClient.listarInbox(sessaoBean.getUsuarioLogado().getIdTime());
        falhaInbox = false;
    }

    public void selecionarConversa(ConversaInboxDTO conversa) {
        try {
            List<MensagemChatDTO> historico = buscarHistorico(conversa);
            novaMensagem = "";
            conversaSelecionada = conversa;
            mensagens = historico;
            marcarConversaSelecionadaComoLida();
            atualizarInbox();
            atualizarConversaSelecionadaDaInbox();
            sessaoBean.atualizarNotificacoesChat();
        } catch (Exception e) {
            log.error("Erro ao abrir conversa", e);
            mensagem(FacesMessage.SEVERITY_ERROR, "Não foi possível abrir a conversa. Tente novamente.");
        }
    }

    public void voltarParaInbox() {
        conversaSelecionada = null;
        mensagens = new ArrayList<>();
        novaMensagem = "";
    }

    public String getTituloContextoConversa() {
        if (conversaSelecionada == null) {
            return "";
        }

        if ("LIGA".equals(conversaSelecionada.getTipo())) {
            return "Grupo da Liga";
        }

        if ("PENDENTE".equals(conversaSelecionada.getStatusPartida())) {
            return "Negociação para agendar jogo";
        }

        if ("AGENDADO".equals(conversaSelecionada.getStatusPartida())) {
            return conversaSelecionada.isEncerrada() ? "Jogo finalizado" : "Jogo marcado";
        }

        if ("CANCELADO".equals(conversaSelecionada.getStatusPartida())) {
            return "Jogo cancelado";
        }

        if ("EXPIRADO".equals(conversaSelecionada.getStatusPartida())) {
            return "Convite expirado";
        }

        return "Conversa de jogo";
    }

    public String getIconeContextoConversa() {
        if (conversaSelecionada == null || "LIGA".equals(conversaSelecionada.getTipo())) {
            return "fas fa-users";
        }

        if ("PENDENTE".equals(conversaSelecionada.getStatusPartida())) {
            return "fas fa-handshake";
        }

        if ("AGENDADO".equals(conversaSelecionada.getStatusPartida())) {
            return "fas fa-futbol";
        }

        return "fas fa-info-circle";
    }

    public void recarregarMensagens() {
        if (conversaSelecionada != null) {
            mensagens = buscarHistorico(conversaSelecionada);
        }
    }

    private List<MensagemChatDTO> buscarHistorico(ConversaInboxDTO conversa) {
        Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
        if ("JOGO".equals(conversa.getTipo())) {
            return chatClient.buscarHistorico(conversa.getIdPartida(), meuTimeId);
        }
        return chatClient.buscarHistoricoLiga(conversa.getIdLiga(), meuTimeId);
    }

    public void enviarMensagem() {
        PrimeFaces.current().ajax().addCallbackParam("mensagemEnviada", false);
        if (novaMensagem == null || novaMensagem.isBlank() || conversaSelecionada == null) {
            mensagem(FacesMessage.SEVERITY_WARN, "Escreva uma mensagem antes de enviar.");
            return;
        }
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            
            if ("JOGO".equals(conversaSelecionada.getTipo())) {
                chatClient.enviarMensagem(conversaSelecionada.getIdPartida(), meuTimeId, novaMensagem);
            } else {
                chatClient.enviarMensagemLiga(conversaSelecionada.getIdLiga(), meuTimeId, novaMensagem);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem", e);
            mensagem(FacesMessage.SEVERITY_ERROR, "Não foi possível enviar a mensagem. Seu texto foi mantido para tentar novamente.");
            return;
        }

        novaMensagem = "";
        PrimeFaces.current().ajax().addCallbackParam("mensagemEnviada", true);
        try {
            recarregarMensagens();
            atualizarInbox();
            atualizarConversaSelecionadaDaInbox();
        } catch (Exception e) {
            log.error("Mensagem enviada, mas houve falha ao atualizar a conversa", e);
            mensagem(FacesMessage.SEVERITY_WARN, "Mensagem enviada. Atualize a conversa para carregar as mensagens mais recentes.");
        }
    }
    
    public void chegouMensagemPeloWebSocket() {
        try {
            boolean estavaEncerrada = conversaSelecionada != null && conversaSelecionada.isEncerrada();
            marcarConversaSelecionadaComoLida();
            atualizarInbox();
            atualizarConversaSelecionadaDaInbox();
            recarregarMensagens();
            sessaoBean.atualizarNotificacoesChat();
            if (conversaSelecionada != null && estavaEncerrada != conversaSelecionada.isEncerrada()) {
                PrimeFaces.current().ajax().update("formChat:painelCompositor");
            }
            PrimeFaces.current().ajax().addCallbackParam("chatAtualizado", true);
        } catch (Exception e) {
            log.error("Erro ao atualizar conversa", e);
            PrimeFaces.current().ajax().addCallbackParam("chatAtualizado", false);
        }
    }

    private void marcarConversaSelecionadaComoLida() {
        if (conversaSelecionada == null) {
            return;
        }

        Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
        if ("JOGO".equals(conversaSelecionada.getTipo())) {
            chatClient.marcarComoLidas(conversaSelecionada.getIdPartida(), meuTimeId);
        } else {
            chatClient.marcarComoLidasLiga(conversaSelecionada.getIdLiga(), meuTimeId);
        }
    }

    private void atualizarConversaSelecionadaDaInbox() {
        if (conversaSelecionada == null) {
            return;
        }

        Optional<ConversaInboxDTO> atualizada = inbox.stream()
                .filter(this::mesmaConversaSelecionada)
                .findFirst();
        if (atualizada.isPresent()) {
            conversaSelecionada = atualizada.get();
        } else {
            voltarParaInbox();
            PrimeFaces.current().ajax().update("formChat:painelChat");
        }
    }

    private boolean mesmaConversaSelecionada(ConversaInboxDTO conversa) {
        if (!conversa.getTipo().equals(conversaSelecionada.getTipo())) {
            return false;
        }

        if ("JOGO".equals(conversa.getTipo())) {
            return conversa.getIdPartida().equals(conversaSelecionada.getIdPartida());
        }

        return conversa.getIdLiga().equals(conversaSelecionada.getIdLiga());
    }

    private void mensagem(FacesMessage.Severity gravidade, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(gravidade, "Chat", texto));
    }

    private void selecionarConversaPorParametro() {
        String idPartidaParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("p");

        if (idPartidaParam == null || idPartidaParam.isBlank()) {
            return;
        }

        try {
            Long idPartida = Long.valueOf(idPartidaParam);
            Optional<ConversaInboxDTO> conversa = inbox.stream()
                    .filter(c -> "JOGO".equals(c.getTipo()))
                    .filter(c -> idPartida.equals(c.getIdPartida()))
                    .findFirst();

            conversa.ifPresent(this::selecionarConversa);
        } catch (NumberFormatException e) {
            // Ignora parametro invalido.
        }
    }
}
