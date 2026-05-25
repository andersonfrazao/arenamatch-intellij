package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelamentoPartidaService {

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;
    private final ParametroSistemaService parametroSistemaService;
    private final PartidaMensagemService partidaMensagemService;

    public CancelamentoPartidaService(
            PartidaRepository partidaRepository,
            TimeRepository timeRepository,
            ParametroSistemaService parametroSistemaService,
            PartidaMensagemService partidaMensagemService) {
        this.partidaRepository = partidaRepository;
        this.timeRepository = timeRepository;
        this.parametroSistemaService = parametroSistemaService;
        this.partidaMensagemService = partidaMensagemService;
    }

    @Transactional
    public void solicitarCancelamento(Long idPartida, Long idTimeSolicitante, String motivo) {
        Time timeSolicitante = timeRepository.findById(idTimeSolicitante)
                .orElseThrow(() -> new RuntimeException("Time solicitante não encontrado"));

        solicitarCancelamento(idPartida, timeSolicitante, motivo);
    }

    @Transactional
    public void solicitarCancelamento(Long idPartida, Time timeSolicitante, String motivo) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));

        validarSolicitacao(partida, timeSolicitante, motivo);

        String motivoLimpo = motivo.trim();
        partida.setStatus(StatusPartida.SOLICITACAO_CANCELAMENTO);
        partida.setSolicitanteCancelamento(timeSolicitante);
        partida.setMotivoCancelamento(motivoLimpo);
        partida.setDataSolicitacao(LocalDateTime.now());

        partidaRepository.save(partida);
        partidaMensagemService.criarMensagemCancelamento(partida, timeSolicitante, motivoLimpo);
    }

    @Transactional
    public void responderCancelamento(Long idPartida, Long idTimeRespondente, boolean aceitar) {
        Time timeRespondente = timeRepository.findById(idTimeRespondente)
                .orElseThrow(() -> new RuntimeException("Time respondente não encontrado"));

        responderCancelamento(idPartida, timeRespondente, aceitar);
    }

    @Transactional
    public void responderCancelamento(Long idPartida, Time timeRespondente, boolean aceitar) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));

        validarResposta(partida, timeRespondente);

        if (aceitar) {
            partida.setStatus(StatusPartida.CANCELADO);
            partidaMensagemService.criarMensagemRespostaCancelamento(partida, timeRespondente, true);
        } else {
            partida.setStatus(StatusPartida.AGENDADO);
            partidaMensagemService.criarMensagemRespostaCancelamento(partida, timeRespondente, false);
            partida.setSolicitanteCancelamento(null);
            partida.setMotivoCancelamento(null);
        }

        partidaRepository.save(partida);
    }

    private void validarSolicitacao(Partida partida, Time timeSolicitante, String motivo) {
        if (!partida.getMandante().equals(timeSolicitante) && !partida.getVisitante().equals(timeSolicitante)) {
            throw new RuntimeException("Você não participa deste jogo.");
        }

        if (partida.getStatus() != StatusPartida.AGENDADO) {
            throw new RuntimeException("Somente jogos agendados podem ter cancelamento solicitado.");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("Informe o motivo do cancelamento.");
        }

        if (motivo.trim().length() > 350) {
            throw new RuntimeException("O motivo do cancelamento deve ter no maximo 350 caracteres.");
        }

        int minDiasAntecedencia = parametroSistemaService.buscarMinDiasAntecedenciaCancelamento();
        long diasAteOJogo = ChronoUnit.DAYS.between(LocalDateTime.now(), partida.getDataHora());

        if (diasAteOJogo < minDiasAntecedencia) {
            throw new RuntimeException("Cancelamento nao permitido! Faltam menos de "
                    + minDiasAntecedencia + " dias para o jogo. Combine via Chat.");
        }
    }

    private void validarResposta(Partida partida, Time timeRespondente) {
        if (partida.getStatus() != StatusPartida.SOLICITACAO_CANCELAMENTO) {
            throw new RuntimeException("Esta partida nao possui solicitacao de cancelamento pendente.");
        }

        if (!partida.getMandante().equals(timeRespondente) && !partida.getVisitante().equals(timeRespondente)) {
            throw new RuntimeException("Voce nao participa deste jogo.");
        }

        if (partida.getSolicitanteCancelamento() == null) {
            throw new RuntimeException("Solicitante do cancelamento nao encontrado.");
        }

        if (partida.getSolicitanteCancelamento().equals(timeRespondente)) {
            throw new RuntimeException("Você não pode responder sua própria solicitação.");
        }
    }
}
