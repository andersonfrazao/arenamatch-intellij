package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AcessoCampeonatoDTO;
import br.com.arenamatch.jsf.client.CampeonatoAcessoClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CampeonatoAcessoBeanTest {
    @Test void consultaUmaVezPorRequisicaoEFalhaFechada() {
        SessaoBean sessao = mock(SessaoBean.class);
        CampeonatoAcessoClient client = mock(CampeonatoAcessoClient.class);
        when(sessao.isLogado()).thenReturn(true);
        when(client.consultar()).thenThrow(new IllegalStateException("indisponivel"));
        CampeonatoAcessoBean bean = bean(sessao, client);
        assertFalse(bean.isPodeOrganizar());
        assertTrue(bean.isErro());
        assertTrue(bean.getMensagem().contains("Não foi possível"));
        verify(client, times(1)).consultar();
    }

    @Test void usaPermissaoAtualDaApiMesmoComSessaoPremiumAntiga() {
        SessaoBean sessao = mock(SessaoBean.class);
        CampeonatoAcessoClient client = mock(CampeonatoAcessoClient.class);
        when(sessao.isLogado()).thenReturn(true);
        when(client.consultar()).thenReturn(new AcessoCampeonatoDTO(false, "Regularize o pagamento"));
        CampeonatoAcessoBean bean = bean(sessao, client);
        assertFalse(bean.isPodeOrganizar());
        assertEquals("Regularize o pagamento", bean.getMensagem());
    }

    @Test void anonimoNaoConsultaApi() {
        CampeonatoAcessoClient client = mock(CampeonatoAcessoClient.class);
        CampeonatoAcessoBean bean = bean(mock(SessaoBean.class), client);
        assertFalse(bean.isPodeOrganizar());
        assertTrue(bean.getMensagem().contains("Entre"));
        verifyNoInteractions(client);
    }

    private CampeonatoAcessoBean bean(SessaoBean sessao, CampeonatoAcessoClient client) {
        CampeonatoAcessoBean bean = new CampeonatoAcessoBean();
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        ReflectionTestUtils.setField(bean, "client", client);
        return bean;
    }
}
