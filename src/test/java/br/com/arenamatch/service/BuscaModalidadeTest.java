package br.com.arenamatch.service;

import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.enums.Categoria;
import jakarta.persistence.Tuple;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.repository.TimeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BuscaModalidadeTest {
    @Mock private TimeRepository timeRepository;
    @Mock private PlacarPendenteService placarPendenteService;
    @Mock private ParametroSistemaService parametroSistemaService;
    @InjectMocks private BuscaService service;

    @Test void rejeitaBuscaSemModalidade() {
        assertThrows(RuntimeException.class, () -> service.buscarTimesDisponiveis(LocalDate.now().plusDays(10),30d,null,null,1L,null,null));
        verifyNoInteractions(timeRepository);
    }
    @Test void rejeitaModalidadeNaoCadastradaNoProprioTime() {
        Time time = new Time(); time.getModalidades().add(Modalidade.FUTEBOL_CAMPO);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        assertThrows(RuntimeException.class, () -> service.buscarTimesDisponiveis(LocalDate.now().plusDays(10),30d,null,null,1L,null,Modalidade.FUTSAL));
        verify(timeRepository,never()).buscarPorLocalizacaoEDia(any(),any(),any(),any(),any(),any(),any(),any(),any(),any());
    }

    @ParameterizedTest
    @CsvSource({"true,120,120", "true,0,0", "false,120,200"})
    void apresentaTaxaDoTimeMandante(boolean mandante, double minhaTaxa, double taxaEsperada) {
        Time time = new Time();
        time.setId(1L);
        time.setMandoCampo(mandante);
        time.setValorTaxa(minhaTaxa);
        time.getModalidades().add(Modalidade.FUTEBOL_CAMPO);
        Agenda horario = new Agenda();
        horario.setDiaSemana("Sábado");
        horario.setModalidade(Modalidade.FUTEBOL_CAMPO);
        horario.setCategoria(Categoria.ESPORTE);
        horario.setHoraInicio("12:00");
        horario.setHoraFim("14:00");
        time.setAgendas(List.of(horario));
        Tuple adversario = mock(Tuple.class);
        when(adversario.get("taxa", BigDecimal.class)).thenReturn(BigDecimal.valueOf(200));
        if (!mandante) {
            lenient().when(adversario.get("horaInicio")).thenReturn("12:00");
            lenient().when(adversario.get("horaFim")).thenReturn("14:00");
        }
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(timeRepository.buscarPorLocalizacaoEDia(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(List.of(adversario));
        LocalDate sabado = LocalDate.now().plusDays(7)
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));
        var resultados = service.buscarTimesDisponiveis(sabado, 30d, null, null, 1L, null, Modalidade.FUTEBOL_CAMPO);
        assertEquals(taxaEsperada, resultados.get(0).getValorTaxa());
    }
}
