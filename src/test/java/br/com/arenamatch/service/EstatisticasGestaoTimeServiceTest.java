package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.EventoSumula;
import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.entity.ParticipacaoPartida;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.TipoEventoSumula;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.EstatisticasRepository;
import br.com.arenamatch.repository.ParticipacaoPartidaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class EstatisticasGestaoTimeServiceTest {
    private EstatisticasRepository estatisticasRepository;
    private AtletaRepository atletaRepository;
    private ParticipacaoPartidaRepository participacaoRepository;
    private GestaoTimeAuthorizationService authorizationService;
    private EstatisticasGestaoTimeService service;
    private Time time;

    @BeforeEach
    void configurar() {
        estatisticasRepository = mock(EstatisticasRepository.class);
        atletaRepository = mock(AtletaRepository.class);
        participacaoRepository = mock(ParticipacaoPartidaRepository.class);
        authorizationService = mock(GestaoTimeAuthorizationService.class);
        service = new EstatisticasGestaoTimeService(estatisticasRepository, atletaRepository,
                participacaoRepository, authorizationService);
        time = new Time();
        time.setId(10L);
        when(authorizationService.exigirAcessoPro()).thenReturn(
                new GestaoTimeAuthorizationService.ContextoAcesso(new Usuario(), time, true));
    }

    @Test
    void deveCalcularResumoColetivoComAproveitamentoEMedias() {
        var projection = mock(EstatisticasRepository.ResumoTimeProjection.class);
        when(projection.getJogos()).thenReturn(4L);
        when(projection.getVitorias()).thenReturn(2L);
        when(projection.getEmpates()).thenReturn(1L);
        when(projection.getDerrotas()).thenReturn(1L);
        when(projection.getGolsPro()).thenReturn(7L);
        when(projection.getGolsContra()).thenReturn(5L);
        when(estatisticasRepository.resumirTime(any(), any(), any())).thenReturn(projection);

        var resumo = service.resumirTime(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(58.33, resumo.getAproveitamento());
        assertEquals(1.75, resumo.getMediaGolsPro());
        assertEquals(2, resumo.getSaldoGols());
    }

    @Test
    void deveBuscarSemAcentosEManterInativoSemEstatistica() {
        Atleta anderson = atleta(1L, "Andérson Frazão", "Frazão", SituacaoAtleta.ATIVO);
        Atleta carlos = atleta(2L, "Carlos", null, SituacaoAtleta.INATIVO);
        var projection = mock(EstatisticasRepository.ResumoJogadorProjection.class);
        when(projection.getAtletaId()).thenReturn(1L);
        when(projection.getPartidas()).thenReturn(3L);
        when(projection.getGols()).thenReturn(4L);
        when(projection.getCartoesAmarelos()).thenReturn(1L);
        when(projection.getCartoesVermelhos()).thenReturn(0L);
        when(estatisticasRepository.resumirJogadores(any(), any(), any())).thenReturn(List.of(projection));
        when(atletaRepository.findByTimeIdOrderByNomeAsc(10L)).thenReturn(List.of(anderson, carlos));

        var resultado = service.resumirJogadores(LocalDate.now().minusDays(30), LocalDate.now(),
                "frazao", "gols");

        assertEquals(1, resultado.getJogadores().size());
        assertEquals(4, resultado.getJogadores().get(0).getGols());
        assertEquals(1, resultado.getArtilheiros().size());
    }

    @Test
    void deveMontarHistoricoComPlacarPapelEventosEMinutos() {
        Atleta atleta = atleta(1L, "Anderson", "Frazão", SituacaoAtleta.ATIVO);
        Time adversario = new Time(); adversario.setId(20L); adversario.setNome("Rival FC");
        Partida partida = new Partida(); partida.setId(50L); partida.setMandante(time);
        partida.setVisitante(adversario); partida.setDataHora(LocalDateTime.now().minusDays(2));
        partida.setGolsMandante(2); partida.setGolsVisitante(1);
        GestaoPartida gestao = new GestaoPartida(); gestao.setPartida(partida); gestao.setTime(time);
        ParticipacaoPartida participacao = new ParticipacaoPartida(); participacao.setId(80L);
        participacao.setAtleta(atleta); participacao.setGestaoPartida(gestao);
        participacao.setPapel(PapelParticipacao.TITULAR); participacao.setNumeroCamisa(10);
        EventoSumula gol = evento(participacao, TipoEventoSumula.GOL, 22);
        EventoSumula amarelo = evento(participacao, TipoEventoSumula.CARTAO_AMARELO, 70);
        gestao.setEventos(List.of(gol, amarelo));
        when(atletaRepository.findById(1L)).thenReturn(java.util.Optional.of(atleta));
        when(participacaoRepository.buscarHistoricoEstatistico(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(participacao)));
        when(estatisticasRepository.resumirJogadores(any(), any(), any())).thenReturn(List.of());

        var detalhe = service.detalharJogador(1L, LocalDate.now().minusMonths(1), LocalDate.now(), 0);

        assertEquals("Rival FC", detalhe.getHistorico().get(0).getAdversario());
        assertEquals("22'", detalhe.getHistorico().get(0).getMinutosGols());
        assertEquals("70'", detalhe.getHistorico().get(0).getMinutosCartoes());
        verify(authorizationService).exigirRecursoDoTime(10L, time, "Jogador");
    }

    @Test
    void deveBloquearAntesDeConsultarDados() {
        when(authorizationService.exigirAcessoPro()).thenThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN));
        assertThrows(ResponseStatusException.class,
                () -> service.resumirTime(LocalDate.now(), LocalDate.now()));
        verify(estatisticasRepository, never()).resumirTime(any(), any(), any());
    }

    @Test
    void deveListarAnoAtualEAnosComPartidasSemDuplicidade() {
        int anoAtual = LocalDate.now().getYear();
        when(estatisticasRepository.listarAnosComPartidas(10L))
                .thenReturn(List.of(anoAtual - 2, anoAtual, anoAtual - 1));

        var anos = service.listarAnosDisponiveis();

        assertEquals(List.of(anoAtual, anoAtual - 1, anoAtual - 2), anos);
    }

    private Atleta atleta(Long id, String nome, String apelido, SituacaoAtleta situacao) {
        Atleta atleta = new Atleta(); atleta.setId(id); atleta.setNome(nome); atleta.setApelido(apelido);
        atleta.setSituacao(situacao); atleta.setTime(time); return atleta;
    }

    private EventoSumula evento(ParticipacaoPartida participacao, TipoEventoSumula tipo, int minuto) {
        EventoSumula evento = new EventoSumula(); evento.setParticipacao(participacao);
        evento.setTipo(tipo); evento.setMinuto(minuto); return evento;
    }
}
