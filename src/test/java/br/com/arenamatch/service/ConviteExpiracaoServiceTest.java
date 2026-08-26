package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConviteExpiracaoServiceTest {
    @Mock private PartidaRepository partidaRepository;
    @InjectMocks private ConviteExpiracaoService service;

    @Test
    void deveExpirarConvitesPendentesDeDiasAnteriores() {
        LocalDate hoje = LocalDate.of(2026, 8, 29);
        Partida convite = new Partida();
        convite.setStatus(StatusPartida.PENDENTE);
        when(partidaRepository.buscarConvitesPendentesAnterioresA(hoje.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(convite));

        service.expirarConvitesAnterioresA(hoje);

        assertEquals(StatusPartida.EXPIRADO, convite.getStatus());
        verify(partidaRepository).saveAll(List.of(convite));
    }
}
