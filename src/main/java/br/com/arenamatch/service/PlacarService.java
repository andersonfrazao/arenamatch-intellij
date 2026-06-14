package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlacarService {

    private final PartidaRepository partidaRepository;
    private final PlacarPendenteService placarPendenteService;
    private final NotificacaoService notificacaoService;

    public PlacarService(
            PartidaRepository partidaRepository,
            PlacarPendenteService placarPendenteService,
            NotificacaoService notificacaoService) {
        this.partidaRepository = partidaRepository;
        this.placarPendenteService = placarPendenteService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public void informarPlacar(Long idPartida, Integer golsMandante, Integer golsVisitante, Long idTimeInformante) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        placarPendenteService.registrarInformacaoPlacar(partida, idTimeInformante);

        partidaRepository.save(partida);
        notificacaoService.deletarNotificacaoPlacar(idPartida);
        notificarAdversario(partida, golsMandante, golsVisitante, idTimeInformante);
    }

    @Transactional
    public void confirmarPlacar(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        if (partida.getStatusPlacar() != StatusPlacar.AGUARDANDO_CONFIRMACAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido para confirmação");
        }

        placarPendenteService.confirmarPlacar(partida);
        notificacaoService.deletarNotificacaoPlacar(idPartida);
    }

    @Transactional
    public void contestarPlacar(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        partida.setStatusPlacar(StatusPlacar.EM_DISPUTA);
        partidaRepository.save(partida);
        notificacaoService.deletarNotificacaoPlacar(idPartida);
    }

    private void notificarAdversario(Partida partida, Integer golsMandante, Integer golsVisitante, Long idTimeInformante) {
        Long idAdversario = partida.getMandante().getId().equals(idTimeInformante)
                ? partida.getVisitante().getId()
                : partida.getMandante().getId();

        Time timeQueInformou = partida.getMandante().getId().equals(idTimeInformante)
                ? partida.getMandante()
                : partida.getVisitante();

        String dataDoJogo = partida.getDataHora() != null
                ? partida.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Data indefinida";

        notificacaoService.criarNotificacao(
                idAdversario,
                "PLACAR",
                partida.getId(),
                "Placar: " + timeQueInformou.getNome(),
                "Jogo do dia " + dataDoJogo + ". Resultado: " + golsMandante + " x " + golsVisitante + ". Confirma?"
        );
    }
}
