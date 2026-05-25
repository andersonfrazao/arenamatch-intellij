package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ConversaInboxService conversaInboxService;
    private final ChatPartidaService chatPartidaService;
    private final ChatLigaService chatLigaService;

    public ChatService(
            ConversaInboxService conversaInboxService,
            ChatPartidaService chatPartidaService,
            ChatLigaService chatLigaService) {
        this.conversaInboxService = conversaInboxService;
        this.chatPartidaService = chatPartidaService;
        this.chatLigaService = chatLigaService;
    }

    public List<ConversaInboxDTO> listarConversasAtivas(Long meuTimeId) {
        return conversaInboxService.listarConversasAtivas(meuTimeId);
    }

    public List<MensagemChatDTO> buscarHistoricoPartida(Long idPartida, Long meuTimeId) {
        return chatPartidaService.buscarHistoricoPartida(idPartida, meuTimeId);
    }

    public void enviarMensagem(Long idPartida, Long idRemetente, String texto) {
        chatPartidaService.enviarMensagem(idPartida, idRemetente, texto);
    }

    public Long contarNaoLidasGeral(Long meuTimeId) {
        return chatPartidaService.contarNaoLidasGeral(meuTimeId)
                + chatLigaService.contarNaoLidasGeral(meuTimeId);
    }

    public void marcarComoLidas(Long idPartida, Long meuTimeId) {
        chatPartidaService.marcarComoLidas(idPartida, meuTimeId);
    }

    public List<MensagemChatDTO> buscarHistoricoLiga(Long idLiga, Long meuTimeId) {
        return chatLigaService.buscarHistoricoLiga(idLiga, meuTimeId);
    }

    public void enviarMensagemLiga(Long idLiga, Long idRemetente, String texto) {
        chatLigaService.enviarMensagemLiga(idLiga, idRemetente, texto);
    }

    public void marcarComoLidasLiga(Long idLiga, Long meuTimeId) {
        chatLigaService.marcarComoLidasLiga(idLiga, meuTimeId);
    }
}
