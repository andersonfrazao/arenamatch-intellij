package br.com.arenamatch.service;

import br.com.arenamatch.dto.MensagemChatDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.MensagemChatLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.MensagemChatLigaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatLigaService {

    private final MensagemChatLigaRepository mensagemLigaRepository;
    private final LigaRepository ligaRepository;
    private final SimpMessagingTemplate mensageiro;

    public ChatLigaService(
            MensagemChatLigaRepository mensagemLigaRepository,
            LigaRepository ligaRepository,
            SimpMessagingTemplate mensageiro) {
        this.mensagemLigaRepository = mensagemLigaRepository;
        this.ligaRepository = ligaRepository;
        this.mensageiro = mensageiro;
    }

    @Transactional(readOnly = true)
    public List<MensagemChatDTO> buscarHistoricoLiga(Long idLiga, Long meuTimeId) {
        List<MensagemChatLiga> mensagens = mensagemLigaRepository.findByLigaIdOrderByDataHoraAsc(idLiga);
        List<MensagemChatDTO> historico = new ArrayList<>();

        for (MensagemChatLiga mensagem : mensagens) {
            MensagemChatDTO dto = new MensagemChatDTO();
            dto.setId(mensagem.getId());
            dto.setIdPartida(null);
            dto.setIdRemetente(mensagem.getRemetente().getId());
            dto.setNomeRemetente(mensagem.getRemetente().getNome());
            dto.setEscudoRemetente(mensagem.getRemetente().getEscudo());
            dto.setTexto(mensagem.getTexto());
            dto.setDataHora(mensagem.getDataHora());
            dto.setEnviadaPorMim(mensagem.getRemetente().getId().equals(meuTimeId));
            historico.add(dto);
        }
        return historico;
    }

    @Transactional
    public void enviarMensagemLiga(Long idLiga, Long idRemetente, String texto) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada"));

        Time remetente = new Time();
        remetente.setId(idRemetente);

        MensagemChatLiga novaMensagem = new MensagemChatLiga();
        novaMensagem.setLiga(liga);
        novaMensagem.setRemetente(remetente);
        novaMensagem.setTexto(texto);
        novaMensagem.setDataHora(LocalDateTime.now());

        novaMensagem = mensagemLigaRepository.save(novaMensagem);
        mensageiro.convertAndSend("/topic/chat/liga/" + idLiga, toDTO(novaMensagem, idRemetente));
        liga.getTimes().stream()
                .map(Time::getId)
                .filter(idTime -> !idTime.equals(idRemetente))
                .forEach(idTime -> mensageiro.convertAndSend("/topic/notificacoes/" + idTime, "CHEGOU_CHAT"));
    }

    @Transactional(readOnly = true)
    public Long contarNaoLidasGeral(Long meuTimeId) {
        Long total = mensagemLigaRepository.contarMensagensNaoLidasGeral(meuTimeId);
        return total != null ? total : 0L;
    }

    @Transactional
    public void marcarComoLidasLiga(Long idLiga, Long meuTimeId) {
        mensagemLigaRepository.marcarMensagensComoLidas(idLiga, meuTimeId);
    }

    private MensagemChatDTO toDTO(MensagemChatLiga mensagem, Long idRemetente) {
        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(mensagem.getId());
        dto.setIdPartida(null);
        dto.setIdRemetente(idRemetente);
        dto.setNomeRemetente(mensagem.getRemetente().getNome());
        dto.setEscudoRemetente(mensagem.getRemetente().getEscudo());
        dto.setTexto(mensagem.getTexto());
        dto.setDataHora(mensagem.getDataHora());
        return dto;
    }
}
