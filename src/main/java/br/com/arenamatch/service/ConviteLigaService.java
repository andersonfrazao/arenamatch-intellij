package br.com.arenamatch.service;

import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConviteLigaService {

    private final LigaRepository ligaRepository;
    private final ConviteLigaRepository conviteLigaRepository;
    private final TimeRepository timeRepository;
    private final SimpMessagingTemplate mensageiro;
    private final PlacarPendenteService placarPendenteService;

    public ConviteLigaService(
            LigaRepository ligaRepository,
            ConviteLigaRepository conviteLigaRepository,
            TimeRepository timeRepository,
            SimpMessagingTemplate mensageiro,
            PlacarPendenteService placarPendenteService) {
        this.ligaRepository = ligaRepository;
        this.conviteLigaRepository = conviteLigaRepository;
        this.timeRepository = timeRepository;
        this.mensageiro = mensageiro;
        this.placarPendenteService = placarPendenteService;
    }

    @Transactional
    public void enviarConvite(Long idLiga, Long idTimeConvidado, String mensagem) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));

        Time convidado = timeRepository.findById(idTimeConvidado)
                .orElseThrow(() -> new RuntimeException("Time convidado não encontrado."));

        placarPendenteService.validarSemPlacarPendente(liga.getAdmin().getId());

        if (liga.getTimes().contains(convidado)) {
            throw new RuntimeException("Este time já faz parte da liga.");
        }

        boolean jaConvidado = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(
                idLiga, idTimeConvidado, StatusConviteLiga.PENDENTE);
        if (jaConvidado) {
            throw new RuntimeException("Já existe um convite pendente para este time.");
        }

        ConviteLiga convite = new ConviteLiga();
        convite.setLiga(liga);
        convite.setTimeConvidado(convidado);
        convite.setMensagem(mensagem);
        convite.setStatus(StatusConviteLiga.PENDENTE);
        convite.setDataConvite(LocalDateTime.now());

        conviteLigaRepository.save(convite);
        mensageiro.convertAndSend("/topic/notificacoes/" + idTimeConvidado, "CHEGOU_CONVITE");
    }

    @Transactional
    public void responderConvite(Long idConvite, boolean aceitar) {
        ConviteLiga convite = conviteLigaRepository.findById(idConvite)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado."));

        if (convite.getStatus() != StatusConviteLiga.PENDENTE) {
            throw new RuntimeException("Este convite já foi respondido anteriormente.");
        }

        if (aceitar) {
            aceitarConvite(convite);
        } else {
            convite.setStatus(StatusConviteLiga.RECUSADO);
        }

        conviteLigaRepository.save(convite);
    }

    public List<Long> buscarIdsTimesComConvitePendente(Long ligaId) {
        return conviteLigaRepository.findIdsTimesComConvitePendenteNaLiga(ligaId);
    }

    private void aceitarConvite(ConviteLiga convite) {
        convite.setStatus(StatusConviteLiga.ACEITO);
        Liga liga = convite.getLiga();
        Time time = convite.getTimeConvidado();

        placarPendenteService.validarSemPlacarPendente(time.getId());

        if (!liga.getTimes().contains(time)) {
            liga.getTimes().add(time);
            ligaRepository.save(liga);
        }
    }
}
