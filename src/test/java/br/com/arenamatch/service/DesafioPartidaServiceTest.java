package br.com.arenamatch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class DesafioPartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepository;
    @Mock private PartidaRepository partidaRepository;
    @Mock private TimeRepository timeRepository;
    @Mock private AssinaturaService assinaturaService;
    @Mock private ParametroSistemaService parametroSistemaService;
    @Mock private PlacarPendenteService placarPendenteService;
    @Mock private PartidaMensagemService partidaMensagemService;
    @Mock private PartidaHorarioService partidaHorarioService;
    @Mock private SimpMessagingTemplate mensageiro;

    @Mock
    private TimeRepository timeRepository;
    private DesafioPartidaService service;

    @Mock
    private AssinaturaService assinaturaService;
    @BeforeEach
    void setUp() {
        service = new DesafioPartidaService(
                partidaRepository,
                timeRepository,
                assinaturaService,
                parametroSistemaService,
                placarPendenteService,
                partidaMensagemService,
                partidaHorarioService,
                mensageiro);
    }

    @Mock
    private ParametroSistemaService parametroSistemaService;
    @Test
    void naoDeveAceitarConviteQuandoExistePlacarPendente() {
        Time mandante = new Time();
        mandante.setId(10L);
        Time visitante = new Time();
        visitante.setId(20L);

    @Mock
    private PlacarPendenteService placarPendenteService;
        Partida partida = new Partida();
        partida.setId(30L);
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setDataHora(LocalDateTime.of(2026, 8, 30, 20, 0));

    @Mock
    private PartidaMensagemService partidaMensagemService;
        RuntimeException pendencia = new RuntimeException("Voce precisa informar o placar.");
        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        org.mockito.Mockito.doThrow(pendencia)
                .when(placarPendenteService).validarSemPlacarPendente(10L);

    @Mock
    private PartidaHorarioService partidaHorarioService;
        RuntimeException erro = assertThrows(RuntimeException.class, () -> service.aceitarDesafio(30L));

    @InjectMocks
    private DesafioPartidaService desafioPartidaService;
        assertSame(pendencia, erro);
        verify(placarPendenteService).validarSemPlacarPendente(10L);
        verify(placarPendenteService, never()).validarSemPlacarPendente(20L);
        verify(partidaRepository, never()).save(partida);
    }

    @Test
    void devePermitirDesafiosIlimitadosParaPlanoBasicoQuandoIntervaloForZero() {
        LocalDateTime dataPartida = LocalDateTime.now().plusDays(7);
    void deveNotificarTimeDesafiadoAoCriarConvite() {
        DesafioDTO dto = prepararCriacaoDeDesafio();

        service.criarDesafio(dto);

        verify(mensageiro).convertAndSend("/topic/notificacoes/20", "CHEGOU_CONVITE");
    }

    @Test
    void naoDeveNotificarQuandoMensagemInicialFalhar() {
        DesafioDTO dto = prepararCriacaoDeDesafio();
        org.mockito.Mockito.doThrow(new RuntimeException("Falha ao salvar mensagem"))
                .when(partidaMensagemService)
                .criarMensagemInicialDoDesafio(
                        org.mockito.ArgumentMatchers.any(Partida.class),
                        org.mockito.ArgumentMatchers.any(Time.class),
                        org.mockito.ArgumentMatchers.anyString());

        assertThrows(RuntimeException.class, () -> service.criarDesafio(dto));

        verify(mensageiro, never()).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private DesafioDTO prepararCriacaoDeDesafio() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 8, 30, 20, 0);
        Usuario responsavelDesafiante = new Usuario();
        responsavelDesafiante.setStatusUsuario(StatusUsuario.ATIVO);
        Usuario responsavelDesafiado = new Usuario();
        responsavelDesafiado.setStatusUsuario(StatusUsuario.ATIVO);

        Agenda agendaDesafiante = new Agenda();
        agendaDesafiante.setDiaSemana("DOMINGO");
        Agenda agendaDesafiado = new Agenda();
        agendaDesafiado.setDiaSemana("DOMINGO");

        Time desafiante = new Time();
        desafiante.setId(10L);
        desafiante.setResponsavel(responsavelDesafiante);
        desafiante.setAgendas(List.of(agendaDesafiante));
        Time desafiado = new Time();
        desafiado.setId(20L);
        desafiado.setResponsavel(responsavelDesafiado);
        desafiado.setAgendas(List.of(agendaDesafiado));

        DesafioDTO dto = new DesafioDTO();
        dto.setIdTimeDesafiante(1L);
        dto.setIdTimeDesafiado(2L);
        dto.setDataHoraPartida(dataPartida);

        Time desafiante = criarTimeBasicoAtivo(1L, "SEGUNDA", true);
        Time desafiado = criarTimeBasicoAtivo(2L, "SEGUNDA", false);

        when(timeRepository.findById(1L)).thenReturn(Optional.of(desafiante));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(desafiado));
        when(assinaturaService.temAcessoCompleto(desafiante.getResponsavel())).thenReturn(false);
        when(parametroSistemaService.buscarDiasIntervaloAgendamentoPlanoBasico()).thenReturn(0);
        when(partidaHorarioService.traduzirDia(any(DayOfWeek.class))).thenReturn("SEGUNDA");
        when(partidaHorarioService.definirDataHoraPeloMandante(eq(desafiante), eq("SEGUNDA"), eq(dto)))
                .thenReturn(dataPartida);
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        desafioPartidaService.criarDesafio(dto);

        verify(partidaRepository, never()).buscarPartidasFuturasAtivasPorTime(1L);
        verify(partidaRepository).save(any(Partida.class));
    }
        dto.setIdTimeDesafiante(10L);
        dto.setIdTimeDesafiado(20L);
        dto.setDataHoraPartida(dataHora);
        dto.setMensagem("Podemos jogar neste horario?");

        when(timeRepository.findById(10L)).thenReturn(Optional.of(desafiante));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(desafiado));
        when(assinaturaService.temAcessoCompleto(responsavelDesafiante)).thenReturn(true);
        when(partidaHorarioService.traduzirDia(dataHora.getDayOfWeek())).thenReturn("DOMINGO");
        when(partidaHorarioService.definirDataHoraPeloMandante(
                org.mockito.ArgumentMatchers.any(Time.class),
                org.mockito.ArgumentMatchers.eq("DOMINGO"),
                org.mockito.ArgumentMatchers.same(dto))).thenReturn(dataHora);
        when(partidaRepository.save(org.mockito.ArgumentMatchers.any(Partida.class)))
                .thenAnswer(invocacao -> {
                    Partida partida = invocacao.getArgument(0);
                    partida.setId(30L);
                    return partida;
                });
    private Time criarTimeBasicoAtivo(Long id, String diaSemana, boolean mandoCampo) {
        Usuario responsavel = new Usuario();
        responsavel.setPlanoAssinatura(PlanoAssinatura.BASICO);
        responsavel.setStatusUsuario(StatusUsuario.ATIVO);

        Time time = new Time();
        time.setId(id);
        time.setResponsavel(responsavel);
        time.setMandoCampo(mandoCampo);

        Agenda agenda = new Agenda();
        agenda.setDiaSemana(diaSemana);
        time.setAgendas(List.of(agenda));

        return time;
        return dto;
    }
}
