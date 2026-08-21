package br.com.arenamatch.service;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesafioPartidaService {

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;
    private final AssinaturaService assinaturaService;
    private final ParametroSistemaService parametroSistemaService;
    private final PlacarPendenteService placarPendenteService;
    private final PartidaMensagemService partidaMensagemService;
    private final PartidaHorarioService partidaHorarioService;
    private final SimpMessagingTemplate mensageiro;

    public DesafioPartidaService(
            PartidaRepository partidaRepository,
            TimeRepository timeRepository,
            AssinaturaService assinaturaService,
            ParametroSistemaService parametroSistemaService,
            PlacarPendenteService placarPendenteService,
            PartidaMensagemService partidaMensagemService,
            PartidaHorarioService partidaHorarioService,
            SimpMessagingTemplate mensageiro) {
        this.partidaRepository = partidaRepository;
        this.timeRepository = timeRepository;
        this.assinaturaService = assinaturaService;
        this.parametroSistemaService = parametroSistemaService;
        this.placarPendenteService = placarPendenteService;
        this.partidaMensagemService = partidaMensagemService;
        this.partidaHorarioService = partidaHorarioService;
        this.mensageiro = mensageiro;
    }

    @Transactional
    public void criarDesafio(DesafioDTO dto) {
        LocalDate dataJogo = dto.getDataHoraPartida().toLocalDate();
        parametroSistemaService.validarDataMinimaAgendamento(dataJogo);

        validarDisponibilidadeDoDesafiado(dto, dataJogo);

        Time desafiante = timeRepository.findById(dto.getIdTimeDesafiante()).orElseThrow();
        Time desafiado = timeRepository.findById(dto.getIdTimeDesafiado()).orElseThrow();

        validarTimeAtivo(desafiante, "O seu time nao esta ativo para criar desafios.");
        validarTimeAtivo(desafiado, "Este time nao esta ativo para receber desafios.");

        placarPendenteService.validarSemPlacarPendente(desafiante.getId());
        validarPermissaoParaCriarDesafio(desafiante);

        DayOfWeek diaEscolhido = dto.getDataHoraPartida().getDayOfWeek();
        String diaBanco = partidaHorarioService.traduzirDia(diaEscolhido);
        validarAgendaDosTimes(desafiante, desafiado, diaBanco);

        Partida partida = new Partida();
        Time mandante = definirMandante(desafiante, desafiado);
        Time visitante = mandante.getId().equals(desafiante.getId()) ? desafiado : desafiante;

        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setStatus(StatusPartida.PENDENTE);
        partida.setDataHora(partidaHorarioService.definirDataHoraPeloMandante(mandante, diaBanco, dto));
        partida.setDataSolicitacao(LocalDateTime.now());
        partida.setDesafiante(desafiante);
        partida.setMensagem(dto.getMensagem());

        partida = partidaRepository.save(partida);
        partidaMensagemService.criarMensagemInicialDoDesafio(partida, desafiante, dto.getMensagem());
        mensageiro.convertAndSend(
                "/topic/notificacoes/" + desafiado.getId(),
                "CHEGOU_CONVITE");
    }

    @Transactional
    public void aceitarDesafio(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida).orElseThrow();

        placarPendenteService.validarSemPlacarPendente(partida.getMandante().getId());
        placarPendenteService.validarSemPlacarPendente(partida.getVisitante().getId());

        boolean mandanteOcupado = partidaRepository.existsByTimeIdAndDataAndStatusAgendado(
                partida.getMandante().getId(), partida.getDataHora().toLocalDate());
        boolean visitanteOcupado = partidaRepository.existsByTimeIdAndDataAndStatusAgendado(
                partida.getVisitante().getId(), partida.getDataHora().toLocalDate());

        if (mandanteOcupado || visitanteOcupado) {
            partida.setStatus(StatusPartida.CANCELADO);
            partidaRepository.save(partida);
            throw new RuntimeException("Não é mais possível aceitar. Um dos times já possui um jogo agendado para esta data!");
        }

        partida.setStatus(StatusPartida.AGENDADO);
        partidaRepository.save(partida);
    }

    @Transactional
    public void excluirConvitePendente(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida nao encontrada para exclusao."));

        if (partida.getStatus() != StatusPartida.PENDENTE) {
            throw new RuntimeException("Somente convites pendentes podem ser removidos diretamente.");
        }

        partidaRepository.deleteById(idPartida);
    }

    @Transactional
    public void cancelarConvitePorAdversario(Long meuTimeId, Long adversarioId) {
        partidaRepository.deletarConvitePendente(meuTimeId, adversarioId);
    }

    private void validarDisponibilidadeDoDesafiado(DesafioDTO dto, LocalDate dataJogo) {
        boolean ocupado = partidaRepository.isTimeOcupadoNoDia(
                dto.getIdTimeDesafiado(),
                dataJogo.atStartOfDay(),
                dataJogo.atTime(23, 59, 59),
                StatusPartida.AGENDADO
        );

        if (ocupado) {
            throw new RuntimeException("Este time já possui um jogo confirmado para esta data!");
        }

        boolean temPendente = partidaRepository.isTimeOcupadoNoDia(
                dto.getIdTimeDesafiado(),
                dataJogo.atStartOfDay(),
                dataJogo.atTime(23, 59, 59),
                StatusPartida.PENDENTE
        );

        if (temPendente) {
            throw new RuntimeException("Já existe um convite pendente com este time nesta data. Use o chat para negociar!");
        }
    }

    private void validarAgendaDosTimes(Time desafiante, Time desafiado, String diaBanco) {
        boolean desafianteJogaNesseDia = desafiante.getAgendas().stream()
                .anyMatch(a -> a.getDiaSemana().equalsIgnoreCase(diaBanco));
        if (!desafianteJogaNesseDia) {
            throw new RuntimeException("O seu time não possui agenda cadastrada para jogar de " + diaBanco + "!");
        }

        boolean desafiadoJogaNesseDia = desafiado.getAgendas().stream()
                .anyMatch(a -> a.getDiaSemana().equalsIgnoreCase(diaBanco));
        if (!desafiadoJogaNesseDia) {
            throw new RuntimeException("O time " + desafiado.getNome() + " não joga de " + diaBanco + "!");
        }
    }

    private Time definirMandante(Time desafiante, Time desafiado) {
        if (desafiante.isMandoCampo() && !desafiado.isMandoCampo()) {
            return desafiante;
        }

        if (desafiado.isMandoCampo() && !desafiante.isMandoCampo()) {
            return desafiado;
        }

        return desafiado;
    }

    private void validarPermissaoParaCriarDesafio(Time desafiante) {
        if (assinaturaService.temAcessoCompleto(desafiante.getResponsavel())) {
            return;
        }

        if (desafiante.getResponsavel() == null
                || desafiante.getResponsavel().getPlanoAssinatura() != PlanoAssinatura.BASICO) {
            assinaturaService.validarAcessoCompleto(desafiante.getResponsavel());
            return;
        }

        int intervaloDias = parametroSistemaService.buscarDiasIntervaloAgendamentoPlanoBasico();
        List<Partida> partidasAtivas = partidaRepository.buscarPartidasFuturasAtivasPorTime(desafiante.getId());
        if (partidasAtivas.isEmpty()) {
            return;
        }

        Partida ultimaPartidaAtiva = partidasAtivas.get(0);
        LocalDate proximaDataPermitida = ultimaPartidaAtiva.getDataHora().toLocalDate().plusDays(intervaloDias);
        LocalDate hoje = LocalDate.now();

        if (hoje.isBefore(proximaDataPermitida)) {
            long diasRestantes = ChronoUnit.DAYS.between(hoje, proximaDataPermitida);
            String diaTexto = diasRestantes == 1 ? "1 dia" : diasRestantes + " dias";
            throw new RuntimeException("Voce ja tem jogo agendado ou desafio enviado! Seu plano BASICO so permite agendar jogos a cada "
                    + intervaloDias + " dias. Voce podera enviar um novo desafio em " + diaTexto + ".");
        }
    }

    private void validarTimeAtivo(Time time, String mensagem) {
        if (time == null || time.getResponsavel() == null
                || !StatusUsuario.ATIVO.equals(time.getResponsavel().getStatusUsuario())) {
            throw new RuntimeException(mensagem);
        }
    }
}
