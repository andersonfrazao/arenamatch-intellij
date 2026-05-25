package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.MensagemChat;
import br.com.arenamatch.entity.MensagemChatLiga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.MensagemChatLigaRepository;
import br.com.arenamatch.repository.MensagemChatRepository;
import br.com.arenamatch.repository.PartidaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversaInboxService {

    private final PartidaRepository partidaRepository;
    private final MensagemChatRepository mensagemRepository;
    private final MensagemChatLigaRepository mensagemLigaRepository;
    private final LigaRepository ligaRepository;
    private final HorarioJogoService horarioJogoService;
    private final ChatBloqueioPolicy chatBloqueioPolicy;

    public ConversaInboxService(
            PartidaRepository partidaRepository,
            MensagemChatRepository mensagemRepository,
            MensagemChatLigaRepository mensagemLigaRepository,
            LigaRepository ligaRepository,
            HorarioJogoService horarioJogoService,
            ChatBloqueioPolicy chatBloqueioPolicy) {
        this.partidaRepository = partidaRepository;
        this.mensagemRepository = mensagemRepository;
        this.mensagemLigaRepository = mensagemLigaRepository;
        this.ligaRepository = ligaRepository;
        this.horarioJogoService = horarioJogoService;
        this.chatBloqueioPolicy = chatBloqueioPolicy;
    }

    @Transactional(readOnly = true)
    public List<ConversaInboxDTO> listarConversasAtivas(Long meuTimeId) {
        List<ConversaInboxDTO> conversas = new ArrayList<>();

        partidaRepository.buscarPartidasParaChat(meuTimeId).forEach(partida ->
                conversas.add(toConversaPartida(partida, meuTimeId)));
        ligaRepository.buscarLigasDoTime(meuTimeId).forEach(liga ->
                conversas.add(toConversaLiga(liga, meuTimeId)));

        conversas.sort((c1, c2) -> {
            if (c1.getHoraUltimaMensagem() == null) {
                return 1;
            }
            if (c2.getHoraUltimaMensagem() == null) {
                return -1;
            }
            return c2.getHoraUltimaMensagem().compareTo(c1.getHoraUltimaMensagem());
        });

        return conversas;
    }

    private ConversaInboxDTO toConversaPartida(Partida partida, Long meuTimeId) {
        ConversaInboxDTO dto = new ConversaInboxDTO();
        dto.setTipo("JOGO");
        dto.setIdPartida(partida.getId());
        dto.setStatusPartida(partida.getStatus() != null ? partida.getStatus().name() : null);
        dto.setDataJogo(horarioJogoService.resolverDataHoraMandante(partida));
        dto.setEncerrada(chatBloqueioPolicy.isEncerrada(partida));

        Time adversario = partida.getMandante().getId().equals(meuTimeId)
                ? partida.getVisitante()
                : partida.getMandante();
        dto.setIdAdversario(adversario.getId());
        dto.setNomeAdversario(adversario.getNome());
        dto.setQtdNaoLidas(mensagemRepository.contarNaoLidasPorPartida(partida.getId(), meuTimeId));

        MensagemChat ultimaMensagem = mensagemRepository.findFirstByPartidaIdOrderByDataHoraDesc(partida.getId());
        if (ultimaMensagem != null) {
            dto.setTextoUltimaMensagem(ultimaMensagem.getTexto());
            dto.setHoraUltimaMensagem(ultimaMensagem.getDataHora());
            dto.setEnviadaPorMim(ultimaMensagem.getRemetente().getId().equals(meuTimeId));
        } else {
            dto.setTextoUltimaMensagem("Inicie uma conversa...");
            dto.setHoraUltimaMensagem(partida.getDataSolicitacao());
            dto.setEnviadaPorMim(false);
        }
        return dto;
    }

    private ConversaInboxDTO toConversaLiga(Liga liga, Long meuTimeId) {
        ConversaInboxDTO dto = new ConversaInboxDTO();
        dto.setTipo("LIGA");
        dto.setIdLiga(liga.getId());
        dto.setNomeAdversario(liga.getNome());
        dto.setEncerrada(false);
        dto.setQtdNaoLidas(mensagemLigaRepository.contarNaoLidasPorLiga(liga.getId(), meuTimeId));

        MensagemChatLiga ultimaMensagem = mensagemLigaRepository.findFirstByLigaIdOrderByDataHoraDesc(liga.getId());
        if (ultimaMensagem != null) {
            dto.setTextoUltimaMensagem(ultimaMensagem.getTexto());
            dto.setHoraUltimaMensagem(ultimaMensagem.getDataHora());
            dto.setEnviadaPorMim(ultimaMensagem.getRemetente().getId().equals(meuTimeId));
        } else {
            dto.setTextoUltimaMensagem("Bem-vindo à liga!");
            dto.setHoraUltimaMensagem(liga.getDataCriacao());
            dto.setEnviadaPorMim(false);
        }
        return dto;
    }
}
