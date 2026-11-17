package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.CadastroClient;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CadastroAtivacaoBeanTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void encaminhaCadastroConcluidoConformeAtivacaoConfigurada(boolean ativacaoHabilitada) {
        CadastroClient cliente = mock(CadastroClient.class);
        CadastroBean bean = new CadastroBean();
        ReflectionTestUtils.setField(bean, "cadastroClient", cliente);
        ReflectionTestUtils.setField(bean, "sessaoBean", mock(SessaoBean.class));
        ReflectionTestUtils.setField(bean, "ativacaoEmailHabilitada", ativacaoHabilitada);
        bean.getDto().setEmail("cadastro@example.test");

        FacesContext contexto = mock(FacesContext.class);
        ExternalContext externo = mock(ExternalContext.class);
        Flash flash = mock(Flash.class);
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getFlash()).thenReturn(flash);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            String pagina = ativacaoHabilitada ? "/ativar-conta.xhtml" : "/login.xhtml";
            assertEquals(pagina + "?faces-redirect=true", bean.finalizar());
        }
        verify(cliente).salvarTime(bean.getDto());
        verify(flash).setKeepMessages(true);
        verify(flash, times(ativacaoHabilitada ? 1 : 0)).put("emailAtivacao", "cadastro@example.test");
    }
}
