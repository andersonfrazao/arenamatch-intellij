package br.com.arenamatch.service;

import br.com.arenamatch.dto.MensagemChatDTO;
import br.com.arenamatch.entity.MensagemChat;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.MensagemChatRepository;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatPartidaService {

    private final PartidaRepository partidaRepository;
    private final MensagemChatRepository mensagemRepository;
    private final SimpMessagingTemplate mensageiro;
    private final ChatBloqueioPolicy chatBloqueioPolicy;

    public ChatPartidaService(
            PartidaRepository partidaRepository,
            MensagemChatRepository mensagemRepository,
            SimpMessagingTemplate mensageiro,
            ChatBloqueioPolicy chatBloqueioPolicy) {
        this.partidaRepository = partidaRepository;
        this.mensagemRepository = mensagemRepository;
        this.mensageiro = mensageiro;
        this.chatBloqueioPolicy = chatBloqueioPolicy;
    }

    @Transactional(readOnly = true)
    public List<MensagemChatDTO> buscarHistoricoPartida(Long idPartida, Long meuTimeId) {
        List<MensagemChat> mensagens = mensagemRepository.findByPartidaIdOrderByDataHoraAsc(idPartida);
        List<MensagemChatDTO> historico = new ArrayList<>();

        for (MensagemChat mensagem : mensagens) {
            MensagemChatDTO dto = new MensagemChatDTO();
            dto.setId(mensagem.getId());
            dto.setIdPartida(idPartida);
            dto.setIdRemetente(mensagem.getRemetente().getId());
            dto.setNomeRemetente(mensagem.getRemetente().getNome());
            dto.setTexto(mensagem.getTexto());
            dto.setDataHora(mensagem.getDataHora());
            dto.setEnviadaPorMim(mensagem.getRemetente().getId().equals(meuTimeId));
            historico.add(dto);
        }
        return historico;
    }

    @Transactional
    public void enviarMensagem(Long idPartida, Long idRemetente, String texto) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));

        chatBloqueioPolicy.validarEnvioPermitido(partida);

        Time remetente = new Time();
        remetente.setId(idRemetente);

        MensagemChat novaMensagem = new MensagemChat();
        novaMensagem.setPartida(partida);
        novaMensagem.setRemetente(remetente);
        novaMensagem.setTexto(texto);
        novaMensagem.setDataHora(LocalDateTime.now());

        novaMensagem = mensagemRepository.save(novaMensagem);
        mensageiro.convertAndSend("/topic/chat/" + idPartida, toDTO(novaMensagem, idPartida, idRemetente));
        Long idDestinatario = partida.getMandante().getId().equals(idRemetente)
                ? partida.getVisitante().getId()
                : partida.getMandante().getId();
        mensageiro.convertAndSend("/topic/notificacoes/" + idDestinatario, "CHEGOU_CHAT");
    }

    @Transactional(readOnly = true)
    public Long contarNaoLidasGeral(Long meuTimeId) {
        Long total = mensagemRepository.contarMensagensNaoLidasGeral(meuTimeId);
        return total != null ? total : 0L;
    }

    @Transactional
    public void marcarComoLidas(Long idPartida, Long meuTimeId) {
        mensagemRepository.marcarMensagensComoLidas(idPartida, meuTimeId);
    }

    private MensagemChatDTO toDTO(MensagemChat mensagem, Long idPartida, Long idRemetente) {
        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(mensagem.getId());
        dto.setIdPartida(idPartida);
        dto.setIdRemetente(idRemetente);
        dto.setTexto(mensagem.getTexto());
        dto.setDataHora(mensagem.getDataHora());
        return dto;
    }
}
