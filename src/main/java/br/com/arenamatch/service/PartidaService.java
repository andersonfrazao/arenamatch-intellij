package br.com.arenamatch.service;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;
    private final PartidaMapper partidaMapper;
    private final DesafioPartidaService desafioPartidaService;
    private final CancelamentoPartidaService cancelamentoPartidaService;
    private final PlacarService placarService;

    public PartidaService(
            PartidaRepository partidaRepository,
            TimeRepository timeRepository,
            PartidaMapper partidaMapper,
            DesafioPartidaService desafioPartidaService,
            CancelamentoPartidaService cancelamentoPartidaService,
            PlacarService placarService) {
        this.partidaRepository = partidaRepository;
        this.timeRepository = timeRepository;
        this.partidaMapper = partidaMapper;
        this.desafioPartidaService = desafioPartidaService;
        this.cancelamentoPartidaService = cancelamentoPartidaService;
        this.placarService = placarService;
    }

    @Transactional(readOnly = true)
    public List<PartidaDTO> listarProximosJogos(Long idTime) {
        Time time = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time não encontrado com o ID: " + idTime));

        return partidaRepository.buscarPorTime(time).stream()
                .map(partidaMapper::toDTO)
                .toList();
    }

    public void criarDesafio(DesafioDTO dto) {
        desafioPartidaService.criarDesafio(dto);
    }

    public void aceitarDesafio(Long idPartida) {
        desafioPartidaService.aceitarDesafio(idPartida);
    }

    public void excluir(Long idPartida) {
        desafioPartidaService.excluirConvitePendente(idPartida);
    }

    public void cancelarConvitePorId(Long idPartida) {
        desafioPartidaService.excluirConvitePendente(idPartida);
    }

    public void cancelarConvitePorAdversario(Long meuTimeId, Long adversarioId) {
        desafioPartidaService.cancelarConvitePorAdversario(meuTimeId, adversarioId);
    }

    public void solicitarCancelamento(Long idPartida, Long idTimeSolicitante, String motivo) {
        cancelamentoPartidaService.solicitarCancelamento(idPartida, idTimeSolicitante, motivo);
    }

    public void solicitarCancelamento(Long idPartida, Time timeSolicitante, String motivo) {
        cancelamentoPartidaService.solicitarCancelamento(idPartida, timeSolicitante, motivo);
    }

    public void responderCancelamento(Long idPartida, Long idTimeRespondente, boolean aceitar) {
        cancelamentoPartidaService.responderCancelamento(idPartida, idTimeRespondente, aceitar);
    }

    public void responderCancelamento(Long idPartida, Time timeRespondente, boolean aceitar) {
        cancelamentoPartidaService.responderCancelamento(idPartida, timeRespondente, aceitar);
    }

    public void informarPlacar(Long idPartida, Integer golsMandante, Integer golsVisitante, Long idTimeInformante) {
        placarService.informarPlacar(idPartida, golsMandante, golsVisitante, idTimeInformante);
    }

    public void confirmarPlacar(Long idPartida) {
        placarService.confirmarPlacar(idPartida);
    }

    public void contestarPlacar(Long idPartida) {
        placarService.contestarPlacar(idPartida);
    }
}
