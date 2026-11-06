package br.com.arenamatch.service;

import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.*;
import br.com.arenamatch.jsf.beans.SessaoBean;
import br.com.arenamatch.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanoPremiumCompatibilidadeTest {
    @Test void reconhecePremiumNoRetornoDoLogin() throws Exception {
        UsuarioDTO dto = new ObjectMapper().readValue("{\"planoAssinatura\":\"PREMIUM\",\"statusPagamento\":\"PAGO\"}", UsuarioDTO.class);
        assertEquals(PlanoAssinatura.PREMIUM, dto.getPlanoAssinatura());
    }

    @Test void premiumPagoIncluiProSemAlterarPlanoPersistido() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        AssinaturaService service = new AssinaturaService(repo);
        Usuario usuario = new Usuario(); usuario.setPlanoAssinatura(PlanoAssinatura.PREMIUM); usuario.setStatusPagamento(StatusPagamento.PAGO);
        assertSame(usuario, service.atualizarTrialExpirado(usuario));
        assertTrue(service.temAcessoCompleto(usuario));
        assertEquals(PlanoAssinatura.PREMIUM, usuario.getPlanoAssinatura());
        verifyNoInteractions(repo);
    }

    @Test void premiumSemPagamentoNaoLiberaProNemNaSessao() {
        AssinaturaService service = new AssinaturaService(mock(UsuarioRepository.class));
        for (StatusPagamento status : StatusPagamento.values()) {
            if (status == StatusPagamento.PAGO) continue;
            Usuario usuario = new Usuario(); usuario.setPlanoAssinatura(PlanoAssinatura.PREMIUM); usuario.setStatusPagamento(status);
            assertFalse(service.temAcessoCompleto(usuario), status.name());
            assertFalse(sessao(status).isAcessoCompleto(), status.name());
            assertFalse(sessao(status).isPlanoPagoAtivo(), status.name());
        }
    }

    @Test void sessaoExibePremiumEPermiteRecursosProQuandoPago() {
        SessaoBean sessao = sessao(StatusPagamento.PAGO);
        assertEquals("Premium", sessao.getPlanoAssinaturaLabel());
        assertEquals("pro", sessao.getPlanoCssClass());
        assertEquals("Recursos PREMIUM ativos", sessao.getResumoPlano());
        assertTrue(sessao.isAcessoCompleto());
        assertTrue(sessao.isPlanoPagoAtivo());
    }

    private SessaoBean sessao(StatusPagamento status) {
        UsuarioDTO dto = new UsuarioDTO(); dto.setPlanoAssinatura(PlanoAssinatura.PREMIUM); dto.setStatusPagamento(status); dto.setPerfil(Perfil.REPRESENTANTE);
        SessaoBean bean = new SessaoBean(); ReflectionTestUtils.setField(bean, "usuarioLogado", dto); return bean;
    }
}
