package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class GestaoTimeAuthorizationServiceTest {

    private UsuarioRepository usuarioRepository;
    private TimeRepository timeRepository;
    private GestaoTimeAuthorizationService service;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        timeRepository = mock(TimeRepository.class);
        service = new GestaoTimeAuthorizationService(usuarioRepository, timeRepository);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveLiberarSomenteProPagoParaEntidadeEDto() {
        Usuario usuario = usuario(PlanoAssinatura.PRO, StatusPagamento.PAGO);
        UsuarioDTO dto = new UsuarioDTO();
        dto.setPlanoAssinatura(PlanoAssinatura.PRO);
        dto.setStatusPagamento(StatusPagamento.PAGO);

        assertTrue(service.possuiProPago(usuario));
        assertTrue(service.possuiProPago(dto));
        assertFalse(service.possuiProPago(PlanoAssinatura.BASICO, StatusPagamento.PAGO));
        assertFalse(service.possuiProPago(PlanoAssinatura.TRIAL, StatusPagamento.TRIAL));
        assertFalse(service.possuiProPago(PlanoAssinatura.PRO, StatusPagamento.PENDENTE));
        assertFalse(service.possuiProPago(PlanoAssinatura.PRO, StatusPagamento.ATRASADO));
        assertFalse(service.possuiProPago(PlanoAssinatura.PRO, StatusPagamento.CANCELADO));
        assertFalse(service.possuiProPago(PlanoAssinatura.PRO, StatusPagamento.EXPIRADO));
        assertFalse(service.possuiProPago((Usuario) null));
    }

    @Test
    void deveDiferenciarUpgradeDeRegularizacao() {
        assertEquals(GestaoTimeAuthorizationService.MENSAGEM_UPGRADE,
                service.mensagemBloqueio(PlanoAssinatura.BASICO, StatusPagamento.PAGO));
        assertEquals(GestaoTimeAuthorizationService.MENSAGEM_REGULARIZACAO,
                service.mensagemBloqueio(PlanoAssinatura.PRO, StatusPagamento.ATRASADO));
        assertTrue(service.precisaRegularizar(PlanoAssinatura.PRO, StatusPagamento.PENDENTE));
        assertFalse(service.precisaRegularizar(PlanoAssinatura.BASICO, StatusPagamento.EXPIRADO));
    }

    @Test
    void deveRejeitarUsuarioNaoAutenticado() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, service::consultarContexto);
        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
    }

    @Test
    void deveRejeitarUsuarioQueNaoExiste() {
        autenticar("desconhecido@arena.test");
        when(usuarioRepository.findByEmail("desconhecido@arena.test")).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, service::consultarContexto);
        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
    }

    @Test
    void deveRejeitarUsuarioSemTime() {
        Usuario usuario = usuario(PlanoAssinatura.PRO, StatusPagamento.PAGO);
        autenticar(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, service::consultarContexto);
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }

    @Test
    void deveBloquearPlanoSemAcessoEPreservarContextoParaConsultaDeDisponibilidade() {
        Usuario usuario = usuario(PlanoAssinatura.BASICO, StatusPagamento.PAGO);
        Time time = time(10L, usuario);
        prepararContexto(usuario, time);

        assertFalse(service.consultarContexto().acessoPro());
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, service::exigirAcessoPro);
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        assertEquals(GestaoTimeAuthorizationService.MENSAGEM_UPGRADE, erro.getReason());
    }

    @Test
    void deveRetornarUsuarioETimeParaProPago() {
        Usuario usuario = usuario(PlanoAssinatura.PRO, StatusPagamento.PAGO);
        Time time = time(10L, usuario);
        prepararContexto(usuario, time);

        var contexto = service.exigirAcessoPro();

        assertSame(usuario, contexto.usuario());
        assertSame(time, contexto.time());
        assertTrue(contexto.acessoPro());
    }

    @Test
    void deveOcultarRecursoDeOutroTimeComoNaoEncontrado() {
        Time time = new Time();
        time.setId(10L);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.exigirRecursoDoTime(20L, time, "Atleta"));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        assertEquals("Atleta nao encontrado.", erro.getReason());
    }

    private void prepararContexto(Usuario usuario, Time time) {
        autenticar(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(timeRepository.findByResponsavel(usuario)).thenReturn(Optional.of(time));
    }

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
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
