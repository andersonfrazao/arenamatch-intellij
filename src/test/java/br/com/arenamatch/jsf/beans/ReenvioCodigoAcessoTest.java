package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.AuthClient;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ReenvioCodigoAcessoTest {

    @Test
    void limpaCodigoDeAtivacaoDepoisDoReenvio() {
        AuthClient cliente = mock(AuthClient.class);
        AtivacaoContaBean bean = new AtivacaoContaBean();
        ReflectionTestUtils.setField(bean, "authClient", cliente);
        bean.setEmail("ativacao@example.test");
        bean.setCodigo("12345");
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(mock(FacesContext.class));
            bean.reenviarCodigo();
        }
        verify(cliente).reenviarCodigoAtivacao("ativacao@example.test");
        assertNull(bean.getCodigo());
    }

    @Test
    void limpaCodigoAdministrativoDepoisDoReenvio() {
        AuthClient cliente = mock(AuthClient.class);
        LoginBean bean = new LoginBean();
        ReflectionTestUtils.setField(bean, "authClient", cliente);
        ReflectionTestUtils.setField(bean, "desafioAdmin", "desafio-ficticio");
        bean.setCodigoAdmin("12345");
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(mock(FacesContext.class));
            bean.reenviarCodigoAdmin();
        }
        verify(cliente).reenviarCodigoAcessoAdmin("desafio-ficticio");
        assertNull(bean.getCodigoAdmin());
    }
}
