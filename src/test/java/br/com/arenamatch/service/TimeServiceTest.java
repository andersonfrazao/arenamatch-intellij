package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private PartidaRepository partidaRepository;

    @InjectMocks
    private TimeService timeService;

    private Usuario usuario;

    @BeforeEach
    void autenticarUsuario() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("time@arena.com", null, List.of()));

        usuario = new Usuario();
        usuario.setEmail("time@arena.com");
        usuario.setPerfil(Perfil.REPRESENTANTE);
        usuario.setPlanoAssinatura(PlanoAssinatura.PRO);
        usuario.setStatusPagamento(StatusPagamento.PAGO);
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarSomenteOTimeDoUsuarioAutenticado() {
        Time time = new Time();
        time.setNome("Arena FC");
        time.setPartidasJogadas(8);
        time.setVitorias(5);
        time.setDerrotas(3);
        time.setGolsPro(14);
        time.setGolsContra(9);

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(time));

        var scout = timeService.buscarScoutDoUsuarioAutenticado();

        assertEquals("Arena FC", scout.getNome());
        assertEquals(8, scout.getPartidasJogadas());
        assertEquals(5, scout.getVitorias());
        assertEquals(3, scout.getDerrotas());
        assertEquals(14, scout.getGolsPro());
        assertEquals(9, scout.getGolsContra());
        verify(timeRepository).findByResponsavel(usuario);
    }

    @Test
    void deveRetornarJogosConfirmadosNaPerspectivaDoTimeAutenticado() {
        Time meuTime = new Time();
        meuTime.setId(10L);
        meuTime.setNome("Areiao");

        Time mocidade = new Time();
        mocidade.setId(20L);
        mocidade.setNome("Mocidade");

        Time unidos = new Time();
        unidos.setId(30L);
        unidos.setNome("Unidos");

        Partida maisRecente = new Partida();
        maisRecente.setDataHora(LocalDateTime.of(2026, 5, 10, 12, 0));
        maisRecente.setMandante(meuTime);
        maisRecente.setVisitante(mocidade);
        maisRecente.setGolsMandante(3);
        maisRecente.setGolsVisitante(2);

        Partida maisAntiga = new Partida();
        maisAntiga.setDataHora(LocalDateTime.of(2026, 4, 20, 9, 0));
        maisAntiga.setMandante(unidos);
        maisAntiga.setVisitante(meuTime);
        maisAntiga.setGolsMandante(1);
        maisAntiga.setGolsVisitante(4);

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(meuTime));
        PageRequest primeiraPagina = PageRequest.of(0, 10);
        when(partidaRepository.buscarJogosComPlacarConfirmado(10L, primeiraPagina))
                .thenReturn(new PageImpl<>(
                        List.of(maisRecente, maisAntiga),
                        primeiraPagina,
                        12));

        var pagina = timeService.buscarJogosRealizadosDoUsuarioAutenticado(0);
        var jogos = pagina.getJogos();

        assertEquals(2, jogos.size());
        assertTrue(pagina.isTemMais());
        assertEquals(LocalDateTime.of(2026, 5, 10, 12, 0), jogos.get(0).getDataHora());
        assertEquals("Areiao", jogos.get(0).getNomeMeuTime());
        assertEquals(3, jogos.get(0).getGolsMeuTime());
        assertEquals(2, jogos.get(0).getGolsAdversario());
        assertEquals("Mocidade", jogos.get(0).getNomeAdversario());
        assertEquals(4, jogos.get(1).getGolsMeuTime());
        assertEquals(1, jogos.get(1).getGolsAdversario());
        assertEquals("Unidos", jogos.get(1).getNomeAdversario());
        verify(partidaRepository).buscarJogosComPlacarConfirmado(10L, primeiraPagina);
    }

    @Test
    void deveIndicarUltimaPaginaDoHistorico() {
        Time meuTime = new Time();
        meuTime.setId(10L);
        meuTime.setNome("Areiao");

        PageRequest segundaPagina = PageRequest.of(1, 10);
        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(meuTime));
        when(partidaRepository.buscarJogosComPlacarConfirmado(10L, segundaPagina))
                .thenReturn(new PageImpl<>(List.of(), segundaPagina, 10));

        var pagina = timeService.buscarJogosRealizadosDoUsuarioAutenticado(1);

        assertEquals(0, pagina.getJogos().size());
        assertFalse(pagina.isTemMais());
    }

    @Test
    void deveNormalizarNumeroDePaginaNegativo() {
        Time meuTime = new Time();
        meuTime.setId(10L);
        meuTime.setNome("Areiao");

        PageRequest primeiraPagina = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(meuTime));
        when(partidaRepository.buscarJogosComPlacarConfirmado(10L, primeiraPagina))
                .thenReturn(new PageImpl<>(List.of(), primeiraPagina, 0));

        timeService.buscarJogosRealizadosDoUsuarioAutenticado(-3);

        verify(partidaRepository).buscarJogosComPlacarConfirmado(10L, primeiraPagina);
    }

    @Test
    void deveNegarPlanoSemAcessoCompleto() {
        usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
        usuario.setStatusPagamento(StatusPagamento.EXPIRADO);

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(false);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> timeService.buscarScoutDoUsuarioAutenticado());

        assertEquals(403, erro.getStatusCode().value());
    }

    @Test
    void devePermitirTrialAtivo() {
        usuario.setPlanoAssinatura(PlanoAssinatura.TRIAL);
        usuario.setStatusPagamento(StatusPagamento.TRIAL);
        usuario.setDataExpiracao(LocalDateTime.now().plusDays(2));

        Time time = new Time();
        time.setNome("Trial FC");

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(time));

        assertEquals("Trial FC", timeService.buscarScoutDoUsuarioAutenticado().getNome());
    }

    @Test
    void deveRetornarNotFoundQuandoUsuarioNaoTemTime() {
        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> timeService.buscarScoutDoUsuarioAutenticado());

        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void deveRetornarRankingComTodosOsTimesDaConsulta() {
        Time primeiro = new Time();
        primeiro.setNome("Primeiro FC");
        Time segundo = new Time();
        segundo.setNome("Segundo FC");

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(assinaturaService.atualizarTrialExpirado(usuario)).thenReturn(usuario);
        when(assinaturaService.temAcessoCompleto(usuario)).thenReturn(true);
        when(timeRepository.buscarRankingGeral()).thenReturn(List.of(primeiro, segundo));

        var ranking = timeService.buscarRankingGeral();

        assertEquals(2, ranking.size());
        assertEquals("Primeiro FC", ranking.get(0).getNome());
        assertEquals("Segundo FC", ranking.get(1).getNome());
    }

    @Test
    void devePermitirRankingParaAdministradorSemPlano() {
        usuario.setPerfil(Perfil.ADMIN);
        usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
        usuario.setStatusPagamento(StatusPagamento.EXPIRADO);

        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));
        when(timeRepository.buscarRankingGeral()).thenReturn(List.of());

        assertEquals(0, timeService.buscarRankingGeral().size());
    }
}
