package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.StatusGestaoPartida;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.GestaoPartidaRepository;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GestaoPartidaHistoricoTest {

    @Test
    void deveListarHistoricoPaginadoComStatusDaGestao() {
        GestaoPartidaRepository gestaoRepository = mock(GestaoPartidaRepository.class);
        PartidaRepository partidaRepository = mock(PartidaRepository.class);
        GestaoTimeAuthorizationService authorizationService = mock(GestaoTimeAuthorizationService.class);
        Time meuTime = time(10L, "Arena FC", "/arena.png");
        Time adversario = time(20L, "Rivais FC", "/rivais.png");
        Usuario usuario = new Usuario();
        Partida primeira = partida(101L, meuTime, adversario, 3, 1, LocalDateTime.of(2026, 9, 8, 20, 0));
        Partida segunda = partida(100L, adversario, meuTime, 2, 2, LocalDateTime.of(2026, 9, 1, 19, 0));
        GestaoPartida gestao = new GestaoPartida();
        gestao.setPartida(primeira);
        gestao.setStatus(StatusGestaoPartida.PUBLICADO);
        PageRequest pagina = PageRequest.of(0, 10);
        when(authorizationService.exigirAcessoPro())
                .thenReturn(new GestaoTimeAuthorizationService.ContextoAcesso(usuario, meuTime, true));
        when(partidaRepository.buscarJogosComPlacarConfirmado(10L, pagina))
                .thenReturn(new PageImpl<>(List.of(primeira, segunda), pagina, 11));
        when(gestaoRepository.findByTimeIdAndPartidaIdIn(10L, List.of(101L, 100L)))
                .thenReturn(List.of(gestao));
        GestaoPartidaService service = service(gestaoRepository, partidaRepository, authorizationService);

        var resultado = service.buscarHistorico(0);

        assertEquals(2, resultado.partidas().size());
        assertEquals(101L, resultado.partidas().get(0).partidaId());
        assertEquals(StatusGestaoPartida.PUBLICADO, resultado.partidas().get(0).statusGestao());
        assertNull(resultado.partidas().get(1).statusGestao());
        assertEquals("Rivais FC", resultado.partidas().get(1).nomeTimeMandante());
        assertEquals("Arena FC", resultado.partidas().get(1).nomeTimeVisitante());
        assertEquals(true, resultado.temMais());
        verify(partidaRepository).buscarJogosComPlacarConfirmado(10L, pagina);
    }

    @Test
    void deveBloquearHistoricoAntesDeConsultarPartidas() {
        GestaoPartidaServiceFixture fixture = new GestaoPartidaServiceFixture();
        when(fixture.authorizationService.exigirAcessoPro()).thenThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso PRO necessário."));

        assertThrows(ResponseStatusException.class, () -> fixture.service.buscarHistorico(0));

        verifyNoInteractions(fixture.partidaRepository, fixture.gestaoRepository);
    }

    private GestaoPartidaService service(GestaoPartidaRepository gestaoRepository,
                                          PartidaRepository partidaRepository,
                                          GestaoTimeAuthorizationService authorizationService) {
        return new GestaoPartidaService(gestaoRepository, partidaRepository,
                mock(br.com.arenamatch.repository.ParticipacaoPartidaRepository.class), mock(AtletaRepository.class),
                mock(GestaoPartidaValidator.class), authorizationService);
    }

    private Time time(Long id, String nome, String escudo) {
        Time time = new Time();
        time.setId(id);
        time.setNome(nome);
        time.setEscudo(escudo);
        return time;
    }

    private Partida partida(Long id, Time mandante, Time visitante, int golsMandante,
                            int golsVisitante, LocalDateTime dataHora) {
        Partida partida = new Partida();
        partida.setId(id);
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setDataHora(dataHora);
        return partida;
    }

    private class GestaoPartidaServiceFixture {
        final GestaoPartidaRepository gestaoRepository = mock(GestaoPartidaRepository.class);
        final PartidaRepository partidaRepository = mock(PartidaRepository.class);
        final GestaoTimeAuthorizationService authorizationService = mock(GestaoTimeAuthorizationService.class);
        final GestaoPartidaService service = service(gestaoRepository, partidaRepository, authorizationService);
    }
}
