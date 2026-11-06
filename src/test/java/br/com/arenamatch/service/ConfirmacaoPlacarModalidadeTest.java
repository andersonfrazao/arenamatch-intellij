package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class ConfirmacaoPlacarModalidadeTest {
    @Test void naoDuplicaEstatisticasAoConfirmarNovamente() {
        PartidaRepository partidas = mock(PartidaRepository.class);
        TimeRepository times = mock(TimeRepository.class);
        EntityManager em = mock(EntityManager.class);
        PlacarPendenteService service = new PlacarPendenteService(partidas, times, mock(NotificacaoRepository.class), mock(ParametroSistemaService.class));
        ReflectionTestUtils.setField(service,"entityManager",em);
        Partida partida = new Partida(); partida.setStatusPlacar(StatusPlacar.CONFIRMADO);
        service.confirmarPlacar(partida);
        verify(em).refresh(partida, LockModeType.PESSIMISTIC_WRITE);
        verifyNoInteractions(partidas, times);
    }
}
