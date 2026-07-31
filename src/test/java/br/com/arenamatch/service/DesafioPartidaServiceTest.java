package br.com.arenamatch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DesafioPartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private ParametroSistemaService parametroSistemaService;

    @Mock
    private PlacarPendenteService placarPendenteService;

    @Mock
    private PartidaMensagemService partidaMensagemService;

    @Mock
    private PartidaHorarioService partidaHorarioService;

    private DesafioPartidaService desafioPartidaService;

    @BeforeEach
    void setUp() {
        desafioPartidaService = new DesafioPartidaService(
                partidaRepository,
                timeRepository,
                assinaturaService,
                parametroSistemaService,
                placarPendenteService,
                partidaMensagemService,
                partidaHorarioService);
    }

    @Test
    void deveManterCriacaoDeDesafioNormalSemDependenciaDeLiga() {
        Time desafiante = criarTime(10L, "Arena FC", true);
        Time desafiado = criarTime(20L, "Rival FC", false);
        LocalDateTime dataSolicitada = LocalDateTime.of(2026, 7, 18, 15, 0);
        LocalDateTime dataDefinida = LocalDateTime.of(2026, 7, 18, 14, 0);

        DesafioDTO dto = new DesafioDTO();
        dto.setIdTimeDesafiante(desafiante.getId());
        dto.setIdTimeDesafiado(desafiado.getId());
        dto.setDataHoraPartida(dataSolicitada);
        dto.setMensagem("Vamos jogar?");

        when(partidaRepository.isTimeOcupadoNoDia(
                desafiado.getId(),
                dataSolicitada.toLocalDate().atStartOfDay(),
                dataSolicitada.toLocalDate().atTime(23, 59, 59),
                br.com.arenamatch.enums.StatusPartida.AGENDADO)).thenReturn(false);
        when(partidaRepository.isTimeOcupadoNoDia(
                desafiado.getId(),
                dataSolicitada.toLocalDate().atStartOfDay(),
                dataSolicitada.toLocalDate().atTime(23, 59, 59),
                br.com.arenamatch.enums.StatusPartida.PENDENTE)).thenReturn(false);
        when(timeRepository.findById(desafiante.getId())).thenReturn(Optional.of(desafiante));
        when(timeRepository.findById(desafiado.getId())).thenReturn(Optional.of(desafiado));
        when(assinaturaService.temAcessoCompleto(desafiante.getResponsavel())).thenReturn(true);
        when(partidaHorarioService.traduzirDia(dataSolicitada.getDayOfWeek())).thenReturn("Sabado");
        when(partidaHorarioService.definirDataHoraPeloMandante(desafiante, "Sabado", dto)).thenReturn(dataDefinida);
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> {
            Partida partida = invocation.getArgument(0);
            partida.setId(99L);
            return partida;
        });

        desafioPartidaService.criarDesafio(dto);

        ArgumentCaptor<Partida> partidaCaptor = ArgumentCaptor.forClass(Partida.class);
        verify(partidaRepository).save(partidaCaptor.capture());
        verify(partidaMensagemService).criarMensagemInicialDoDesafio(partidaCaptor.getValue(), desafiante, "Vamos jogar?");
        verify(partidaRepository, never()).findById(any());
    }

    private Time criarTime(Long id, String nome, boolean mandoCampo) {
        Usuario responsavel = new Usuario();
        responsavel.setStatusUsuario(StatusUsuario.ATIVO);
        responsavel.setPlanoAssinatura(PlanoAssinatura.PRO);

        Agenda agenda = new Agenda();
        agenda.setDiaSemana("Sabado");

        Time time = new Time();
        time.setId(id);
        time.setNome(nome);
        time.setMandoCampo(mandoCampo);
        time.setResponsavel(responsavel);
        time.setAgendas(List.of(agenda));
        return time;
    }
}
