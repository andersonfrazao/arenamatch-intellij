package br.com.arenamatch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private DesafioPartidaService desafioPartidaService;

    @Test
    void devePermitirDesafiosIlimitadosParaPlanoBasicoQuandoIntervaloForZero() {
        LocalDateTime dataPartida = LocalDateTime.now().plusDays(7);
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
    }
}
