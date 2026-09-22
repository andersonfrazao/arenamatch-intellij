package br.com.arenamatch.service;

import br.com.arenamatch.controller.CampeonatoAcessoController;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CampeonatoAuthorizationServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final CampeonatoAuthorizationService service = new CampeonatoAuthorizationService(repository);

    @AfterEach void limpar() { SecurityContextHolder.clearContext(); }

    @Test void apenasPremiumPagoPodeOrganizarMasPremiumIncluiPro() {
        for (PlanoAssinatura plano : PlanoAssinatura.values()) {
            for (StatusPagamento pagamento : StatusPagamento.values()) {
                assertEquals(plano == PlanoAssinatura.PREMIUM && pagamento == StatusPagamento.PAGO,
                        CampeonatoAuthorizationService.possuiPremiumPago(plano, pagamento));
                Usuario usuario = usuario(plano, pagamento);
                boolean pro = (plano == PlanoAssinatura.PRO || plano == PlanoAssinatura.PREMIUM)
                        && pagamento == StatusPagamento.PAGO;
                assertEquals(pro, new AssinaturaService(repository).temAcessoCompleto(usuario));
                assertEquals(pro, new GestaoTimeAuthorizationService(repository, null).possuiProPago(usuario));
            }
        }
        assertFalse(CampeonatoAuthorizationService.possuiPremiumPago(null, StatusPagamento.PAGO));
        assertFalse(CampeonatoAuthorizationService.possuiPremiumPago(PlanoAssinatura.PREMIUM, null));
    }

    @Test void exigeAutenticacaoMesmoConsultandoDiretamente() {
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                service::exigirPremium).getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test void premiumNaoConcedePropriedadeDeOutroOrganizador() {
        Usuario usuario = autenticar(PlanoAssinatura.PREMIUM, StatusPagamento.PAGO);
        assertSame(usuario, service.exigirOrganizador(10L));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.exigirOrganizador(20L)).getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.exigirOrganizador(null));
        verify(repository, never()).save(any());
    }

    @Test void apiRevalidaDowngradeComMesmoTokenSemModificarCadastro() throws Exception {
        Usuario usuario = autenticar(PlanoAssinatura.PREMIUM, StatusPagamento.PAGO);
        var mvc = MockMvcBuilders.standaloneSetup(new CampeonatoAcessoController(service)).build();
        mvc.perform(get("/api/campeonatos/administracao/acesso"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.podeOrganizar").value(true));
        usuario.setPlanoAssinatura(PlanoAssinatura.PRO);
        mvc.perform(get("/api/campeonatos/administracao/acesso")).andExpect(status().isForbidden());
        mvc.perform(get("/api/campeonatos/acesso"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.podeOrganizar").value(false));
        usuario.setPlanoAssinatura(PlanoAssinatura.PREMIUM);
        usuario.setStatusPagamento(StatusPagamento.ATRASADO);
        mvc.perform(get("/api/campeonatos/administracao/acesso")).andExpect(status().isForbidden());
        usuario.setStatusPagamento(StatusPagamento.PAGO);
        mvc.perform(get("/api/campeonatos/administracao/acesso")).andExpect(status().isOk());
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test void perfilAdminNaoSubstituiPremiumETrialNaoOrganiza() {
        Usuario usuario = autenticar(PlanoAssinatura.TRIAL, StatusPagamento.TRIAL);
        usuario.setPerfil(Perfil.ADMIN);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                service::exigirPremium).getStatusCode());
    }

    private Usuario autenticar(PlanoAssinatura plano, StatusPagamento pagamento) {
        Usuario usuario = usuario(plano, pagamento);
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, java.util.List.of()));
        return usuario;
    }

    private Usuario usuario(PlanoAssinatura plano, StatusPagamento pagamento) {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("organizador@arena.test");
        usuario.setPlanoAssinatura(plano);
        usuario.setStatusPagamento(pagamento);
        return usuario;
    }
}
