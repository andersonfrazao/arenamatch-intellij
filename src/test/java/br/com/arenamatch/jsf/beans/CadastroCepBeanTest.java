package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.jsf.client.CadastroClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CadastroCepBeanTest {
    @Test void exibeCepNaoEncontradoQuandoApiRetornaErroDeNegocio() {
        verificarMensagem(new HttpClientErrorException(HttpStatus.BAD_REQUEST), "CEP não encontrado.");
        verificarMensagem(new HttpClientErrorException(HttpStatus.NOT_FOUND), "CEP não encontrado.");
    }

    @Test void informaFalhaTemporariaQuandoConsultaEstaIndisponivel() {
        verificarMensagem(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR), "Não foi possível consultar o CEP agora.");
        verificarMensagem(new ResourceAccessException("Falha de conexão"), "Não foi possível consultar o CEP agora.");
    }

    @Test void exibeMensagemSeConsultaRetornaEnderecoVazioOuComErro() {
        CadastroClient client = mock(CadastroClient.class);
        CadastroBean bean = cadastro(client);
        EnderecoDTO invalido = new EnderecoDTO(); invalido.setErro(true);
        when(client.buscarEnderecoPorCep("99999999")).thenReturn(null, invalido);
        FacesContext context = mock(FacesContext.class);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(context);
            assertDoesNotThrow(bean::buscarCep);
            assertDoesNotThrow(bean::buscarCep);
        }
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(context,times(2)).addMessage(isNull(),mensagem.capture());
        assertTrue(mensagem.getAllValues().stream().allMatch(m -> m.getDetail().startsWith("CEP não encontrado.")));
    }

    @Test void preencheEnderecoQuandoCepFoiEncontrado() {
        CadastroClient client = mock(CadastroClient.class);
        CadastroBean bean = cadastro(client);
        bean.getDto().setLatitude(-23.5); bean.getDto().setLongitude(-46.6);
        EnderecoDTO endereco = new EnderecoDTO(); endereco.setCep("99999-999");
        endereco.setLogradouro("Rua de teste"); endereco.setLocalidade("São Paulo"); endereco.setUf("SP");
        when(client.buscarEnderecoPorCep("99999999")).thenReturn(endereco);
        bean.buscarCep();
        assertEquals("Rua de teste",bean.getDto().getLogradouro());
        assertEquals("São Paulo",bean.getDto().getCidade());
        assertEquals("SP",bean.getDto().getUf());
        assertNull(bean.getDto().getLatitude()); assertNull(bean.getDto().getLongitude());
    }

    private void verificarMensagem(RuntimeException erro, String texto) {
        CadastroClient client = mock(CadastroClient.class);
        CadastroBean bean = cadastro(client);
        when(client.buscarEnderecoPorCep("99999999")).thenThrow(erro);
        FacesContext context = mock(FacesContext.class);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(context);
            assertDoesNotThrow(bean::buscarCep);
        }
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(context).addMessage(isNull(),mensagem.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR,mensagem.getValue().getSeverity());
        assertTrue(mensagem.getValue().getDetail().startsWith(texto));
    }

    private CadastroBean cadastro(CadastroClient client) {
        CadastroBean bean = new CadastroBean(); bean.getDto().setCep("99999-999");
        ReflectionTestUtils.setField(bean,"cadastroClient",client); return bean;
    }
}
