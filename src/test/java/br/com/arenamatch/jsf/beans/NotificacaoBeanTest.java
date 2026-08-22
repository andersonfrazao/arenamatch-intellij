package br.com.arenamatch.jsf.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.service.NotificacaoClassificacaoService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class NotificacaoBeanTest {

    @Mock private AgendaClient agendaClient;
    @Mock private NotificacaoClassificacaoService notificacaoClassificacaoService;
    @InjectMocks private NotificacaoBean bean;

    @Test
    void deveExibirMensagemDoServidorQuandoPlacarImpedeAceite() {
        NotificacaoDTO notificacao = new NotificacaoDTO();
        notificacao.setIdReferencia(30L);
        notificacao.setTipo("JOGO");
        String mensagem = "Voce precisa informar o placar do jogo anterior.";
        RestClientResponseException resposta = new RestClientResponseException(
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpHeaders.EMPTY,
                mensagem.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(notificacaoClassificacaoService.isJogo(notificacao)).thenReturn(true);
        doThrow(resposta).when(agendaClient).aceitarDesafio(30L);

        FacesContext facesContext = org.mockito.Mockito.mock(FacesContext.class);
        try (MockedStatic<FacesContext> facesContextStatic = mockStatic(FacesContext.class)) {
            facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.aceitarConvite(notificacao);
        }

        ArgumentCaptor<FacesMessage> mensagemCaptor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(org.mockito.ArgumentMatchers.isNull(), mensagemCaptor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, mensagemCaptor.getValue().getSeverity());
        assertEquals(mensagem, mensagemCaptor.getValue().getDetail());
    }
}
