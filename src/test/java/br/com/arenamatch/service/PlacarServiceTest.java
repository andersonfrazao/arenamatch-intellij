package br.com.arenamatch.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlacarServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private PlacarPendenteService placarPendenteService;

    @Mock
    private NotificacaoService notificacaoService;

    private PlacarService placarService;

    @BeforeEach
    void setUp() {
        placarService = new PlacarService(partidaRepository, placarPendenteService, notificacaoService);
    }

    @Test
    void deveIncluirOsDoisTimesNoTextoDaNotificacaoDePlacar() {
        Time mandante = new Time();
        mandante.setId(10L);
        mandante.setNome("Time A");

        Time visitante = new Time();
        visitante.setId(20L);
        visitante.setNome("Time B");

        Partida partida = new Partida();
        partida.setId(30L);
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setDataHora(LocalDateTime.of(2026, 6, 14, 20, 0));

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));

        placarService.informarPlacar(30L, 3, 2, 10L);

        verify(notificacaoService).criarNotificacao(
                20L,
                "PLACAR",
                30L,
                "Placar informado por Time A",
                "Confirma o placar Time A 3 x 2 Time B? Jogo do dia 14/06/2026.");
    }
}
