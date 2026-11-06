package br.com.arenamatch.service;

import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.repository.TimeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
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
}
