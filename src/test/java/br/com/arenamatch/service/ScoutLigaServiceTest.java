package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.ScoutLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoutLigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private PartidaLigaRepository partidaLigaRepository;

    @Mock
    private PartidaMapper partidaMapper;

    private ScoutLigaService scoutLigaService;

    @BeforeEach
    void setUp() {
        scoutLigaService = new ScoutLigaService(ligaRepository, partidaLigaRepository, partidaMapper);
    }

    @Test
    void buscarScoutDeveConsiderarSomentePartidasConfirmadasDaLiga() {
        Time arena = time(10L, "Arena FC");
        Time rival = time(20L, "Rival FC");
        Liga liga = liga(arena, rival);

        Partida vitoria = partida(arena, rival, 3, 1, StatusPlacar.CONFIRMADO, LocalDateTime.now().minusDays(1));
        Partida empate = partida(rival, arena, 2, 2, StatusPlacar.CONFIRMADO, LocalDateTime.now().minusDays(2));
        Partida pendente = partida(arena, rival, 5, 0, StatusPlacar.PENDENTE, LocalDateTime.now());

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(partidaLigaRepository.buscarPorLiga(1L)).thenReturn(List.of(
                vinculo(vitoria),
                vinculo(empate),
                vinculo(pendente)));
        when(partidaMapper.toDTO(any(Partida.class))).thenReturn(new PartidaDTO());

        ScoutLigaDTO scout = scoutLigaService.buscarScout(1L, 10L);

        assertEquals("Arena FC", scout.getNomeTime());
        assertEquals(2, scout.getJogos());
        assertEquals(1, scout.getVitorias());
        assertEquals(1, scout.getEmpates());
        assertEquals(0, scout.getDerrotas());
        assertEquals(5, scout.getGolsPro());
        assertEquals(3, scout.getGolsContra());
        assertEquals(2, scout.getSaldoGols());
        assertEquals(66.666, scout.getAproveitamento(), 0.01);
        assertEquals(2, scout.getUltimosJogos().size());
    }

    @Test
    void buscarScoutDeveBloquearTimeForaDaLiga() {
        Time arena = time(10L, "Arena FC");
        Liga liga = liga(arena);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));

        assertThrows(RuntimeException.class, () -> scoutLigaService.buscarScout(1L, 99L));
    }

    private PartidaLiga vinculo(Partida partida) {
        PartidaLiga vinculo = new PartidaLiga();
        vinculo.setLiga(new Liga());
        vinculo.setPartida(partida);
        vinculo.setContaRankingLiga(true);
        return vinculo;
    }

    private Liga liga(Time... times) {
        Liga liga = new Liga();
        liga.setId(1L);
        liga.getTimes().addAll(List.of(times));
        return liga;
    }

    private Partida partida(Time mandante, Time visitante, int golsMandante, int golsVisitante,
            StatusPlacar statusPlacar, LocalDateTime dataHora) {
        Partida partida = new Partida();
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setStatusPlacar(statusPlacar);
        partida.setDataHora(dataHora);
        return partida;
    }

    private Time time(Long id, String nome) {
        Time time = new Time();
        time.setId(id);
        time.setNome(nome);
        return time;
    }
}
