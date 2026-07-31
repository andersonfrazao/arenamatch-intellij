package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingLigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private PartidaLigaRepository partidaLigaRepository;

    private RankingLigaService rankingLigaService;

    @BeforeEach
    void setUp() {
        rankingLigaService = new RankingLigaService(ligaRepository, partidaLigaRepository);
    }

    @Test
    void deveCalcularRankingSomenteComPartidasConfirmadasDaLiga() {
        Time mandante = criarTime(10L, "Mandante FC");
        Time visitante = criarTime(20L, "Visitante FC");
        Time semJogo = criarTime(30L, "Sem Jogo FC");

        Liga liga = new Liga();
        liga.setId(1L);
        liga.setTimes(List.of(mandante, visitante, semJogo));

        Partida confirmada = criarPartida(mandante, visitante, 3, 1, StatusPlacar.CONFIRMADO);
        Partida pendente = criarPartida(visitante, mandante, 5, 0, StatusPlacar.PENDENTE);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(partidaLigaRepository.buscarPorLiga(1L)).thenReturn(List.of(
                criarVinculo(confirmada),
                criarVinculo(pendente)));

        List<RankingLigaDTO> ranking = rankingLigaService.calcularRanking(1L);

        assertEquals(3, ranking.size());
        assertEquals("Mandante FC", ranking.get(0).getNomeTime());
        assertEquals(3, ranking.get(0).getPontos());
        assertEquals(1, ranking.get(0).getJogos());
        assertEquals(3, ranking.get(0).getGolsPro());
        assertEquals(1, ranking.get(0).getGolsContra());
        assertEquals("Sem Jogo FC", ranking.get(1).getNomeTime());
        assertEquals(0, ranking.get(1).getJogos());
        assertEquals("Visitante FC", ranking.get(2).getNomeTime());
    }

    private PartidaLiga criarVinculo(Partida partida) {
        PartidaLiga vinculo = new PartidaLiga();
        vinculo.setPartida(partida);
        vinculo.setContaRankingLiga(true);
        return vinculo;
    }

    private Partida criarPartida(Time mandante, Time visitante, int golsMandante, int golsVisitante, StatusPlacar statusPlacar) {
        Partida partida = new Partida();
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setStatusPlacar(statusPlacar);
        return partida;
    }

    private Time criarTime(Long id, String nome) {
        Time time = new Time();
        time.setId(id);
        time.setNome(nome);
        return time;
    }
}
