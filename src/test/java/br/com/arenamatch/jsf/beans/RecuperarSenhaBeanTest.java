package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.AuthClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecuperarSenhaBeanTest {

    private final AuthClient cliente = mock(AuthClient.class);
    private final FacesContext contexto = mock(FacesContext.class);
    private final ExternalContext externo = mock(ExternalContext.class);
    private final Flash flash = mock(Flash.class);
    private final RecuperarSenhaBean bean = new RecuperarSenhaBean();
    private MockedStatic<FacesContext> faces;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(bean, "authClient", cliente);
        bean.setEmail("recuperacao@example.test");
        bean.setCodigo("12345");
        bean.setNovaSenha("SenhaFicticia77!");
        bean.setConfirmarSenha("SenhaFicticia77!");
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getFlash()).thenReturn(flash);
        faces = mockStatic(FacesContext.class);
        faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
    }

    @AfterEach
    void encerrar() {
        faces.close();
    }

    @Test
    void avancaParaCodigoAposEnvioEPermiteAlterarEmail() {
        bean.enviarCodigo();
        assertTrue(bean.isEtapa2());
        verify(cliente).solicitarCodigoRecuperacao("recuperacao@example.test");
        bean.voltarEtapa1();
        assertFalse(bean.isEtapa2());
        assertNull(bean.getCodigo());
        assertNull(bean.getNovaSenha());
        assertNull(bean.getConfirmarSenha());
        assertEquals("recuperacao@example.test", bean.getEmail());
    }

    @Test
    void informaCodigoInvalidoOuExpiradoEMantemEtapaDeRedefinicao() {
        bean.enviarCodigo();
        clearInvocations(contexto);
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Codigo expirado", null, null, null))
                .when(cliente).redefinirSenha("recuperacao@example.test", "12345", "SenhaFicticia77!");
        bean.alterarSenha();
        assertTrue(bean.isEtapa2());
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto).addMessage(isNull(), mensagem.capture());
        assertEquals("O código digitado é inválido ou expirou.", mensagem.getValue().getDetail());
        verifyNoInteractions(externo);
    }

    @Test
    void validaConfirmacaoAntesDeSolicitarRedefinicao() {
        bean.setConfirmarSenha("OutraSenhaFicticia77!");
        bean.alterarSenha();
        verifyNoInteractions(cliente);
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto).addMessage(isNull(), mensagem.capture());
        assertEquals("As senhas não conferem.", mensagem.getValue().getDetail());
    }

    @Test
    void retornaAoLoginComMensagemDeSucessoDepoisDaRedefinicao() throws Exception {
        bean.alterarSenha();
        verify(cliente).redefinirSenha("recuperacao@example.test", "12345", "SenhaFicticia77!");
        verify(flash).setKeepMessages(true);
        verify(externo).redirect("login.xhtml");
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto).addMessage(isNull(), mensagem.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, mensagem.getValue().getSeverity());
    }
}
