package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ConviteLigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private ConviteLigaRepository conviteLigaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private SimpMessagingTemplate mensageiro;

    @Mock
    private PlacarPendenteService placarPendenteService;

    @Mock
    private BanimentoLigaService banimentoLigaService;

    private ConviteLigaService conviteLigaService;

    @BeforeEach
    void setUp() {
        conviteLigaService = new ConviteLigaService(
                ligaRepository,
                conviteLigaRepository,
                timeRepository,
                mensageiro,
                placarPendenteService,
                banimentoLigaService);
    }

    @Test
    void deveBloquearConviteParaTimeBanido() {
        Liga liga = new Liga();
        liga.setId(1L);
        liga.setAdmin(time(10L));
        Time convidado = time(20L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(convidado));
        org.mockito.Mockito.doThrow(new RuntimeException("Este time esta banido desta liga."))
                .when(banimentoLigaService).validarTimeNaoBanido(1L, 20L);

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> conviteLigaService.enviarConvite(1L, 20L, "Convite"));

        assertEquals("Este time esta banido desta liga.", erro.getMessage());
        verify(conviteLigaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Time time(Long id) {
        Time time = new Time();
        time.setId(id);
        time.setNome("Time " + id);
        return time;
    }
}
