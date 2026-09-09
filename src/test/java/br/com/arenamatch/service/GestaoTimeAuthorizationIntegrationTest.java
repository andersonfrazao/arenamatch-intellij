package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.AtletaRequestDTO;
import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.GestaoPartidaRepository;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class GestaoTimeAuthorizationIntegrationTest {

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void downgradeDeveBloquearElencoSemAlterarDados() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TimeRepository timeRepository = mock(TimeRepository.class);
        AtletaRepository atletaRepository = mock(AtletaRepository.class);
        Usuario usuario = usuario(PlanoAssinatura.BASICO, StatusPagamento.EXPIRADO);
        Time time = time(10L, usuario);
        prepararContexto(usuarioRepository, timeRepository, usuario, time);
        AtletaService atletaService = new AtletaService(atletaRepository,
                new GestaoTimeAuthorizationService(usuarioRepository, timeRepository));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> atletaService.criar(new AtletaRequestDTO("Ana", null)));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(atletaRepository);
    }

    @Test
    void idDeAtletaDeOutroTimeNaoDeveRevelarSuaExistencia() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TimeRepository timeRepository = mock(TimeRepository.class);
        AtletaRepository atletaRepository = mock(AtletaRepository.class);
        Usuario usuario = usuario(PlanoAssinatura.PRO, StatusPagamento.PAGO);
        Time meuTime = time(10L, usuario);
        Time outroTime = time(20L, new Usuario());
        Atleta atletaAlheio = new Atleta();
        atletaAlheio.setId(99L);
        atletaAlheio.setTime(outroTime);
        prepararContexto(usuarioRepository, timeRepository, usuario, meuTime);
        when(atletaRepository.findById(99L)).thenReturn(Optional.of(atletaAlheio));
        AtletaService atletaService = new AtletaService(atletaRepository,
                new GestaoTimeAuthorizationService(usuarioRepository, timeRepository));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> atletaService.atualizar(99L, new AtletaRequestDTO("Atleta", null)));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        assertEquals("Atleta nao encontrado.", erro.getReason());
    }

    @Test
    void idDePartidaDeOutroTimeNaoDeveExporDadosDaGestao() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TimeRepository timeRepository = mock(TimeRepository.class);
        PartidaRepository partidaRepository = mock(PartidaRepository.class);
        GestaoPartidaRepository gestaoRepository = mock(GestaoPartidaRepository.class);
        AtletaRepository atletaRepository = mock(AtletaRepository.class);
        GestaoPartidaValidator validator = mock(GestaoPartidaValidator.class);
        Usuario usuario = usuario(PlanoAssinatura.PRO, StatusPagamento.PAGO);
        Time meuTime = time(10L, usuario);
        Partida partidaAlheia = new Partida();
        partidaAlheia.setId(77L);
        prepararContexto(usuarioRepository, timeRepository, usuario, meuTime);
        when(partidaRepository.findById(77L)).thenReturn(Optional.of(partidaAlheia));
        when(validator.pertenceAPartida(partidaAlheia, meuTime)).thenReturn(false);
        GestaoTimeAuthorizationService authorizationService =
                new GestaoTimeAuthorizationService(usuarioRepository, timeRepository);
        GestaoPartidaService gestaoService = new GestaoPartidaService(
                gestaoRepository, partidaRepository, atletaRepository, validator, authorizationService);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> gestaoService.consultarDisponibilidade(77L));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        assertEquals("Jogo nao encontrado.", erro.getReason());
        verifyNoInteractions(gestaoRepository);
    }

    private void prepararContexto(UsuarioRepository usuarioRepository, TimeRepository timeRepository,
                                  Usuario usuario, Time time) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), null));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(time));
    }

    private Usuario usuario(PlanoAssinatura plano, StatusPagamento pagamento) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("representante@arena.test");
        usuario.setPlanoAssinatura(plano);
        usuario.setStatusPagamento(pagamento);
        return usuario;
    }

    private Time time(Long id, Usuario responsavel) {
        Time time = new Time();
        time.setId(id);
        time.setResponsavel(responsavel);
        return time;
    }
}
