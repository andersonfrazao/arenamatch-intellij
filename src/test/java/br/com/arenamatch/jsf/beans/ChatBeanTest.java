package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.jsf.client.ChatClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatBeanTest {
    private final ChatBean bean = new ChatBean();
    private final ChatClient cliente = mock(ChatClient.class);
    private final SessaoBean sessao = mock(SessaoBean.class);
    private final FacesContext faces = mock(FacesContext.class);
    private final PrimeFaces prime = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
    private MockedStatic<FacesContext> contexto;
    private MockedStatic<PrimeFaces> primeAtual;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(bean, "chatClient", cliente);
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        var usuario = new UsuarioDTO();
        usuario.setIdTime(1L);
        when(sessao.getUsuarioLogado()).thenReturn(usuario);
        contexto = mockStatic(FacesContext.class);
        contexto.when(FacesContext::getCurrentInstance).thenReturn(faces);
        primeAtual = mockStatic(PrimeFaces.class);
        primeAtual.when(PrimeFaces::current).thenReturn(prime);
    }

    @AfterEach
    void encerrar() {
        primeAtual.close();
        contexto.close();
    }

    private ConversaInboxDTO conversa(boolean encerrada) {
        var conversa = new ConversaInboxDTO();
        conversa.setTipo("JOGO");
        conversa.setIdPartida(6L);
        conversa.setStatusPartida("AGENDADO");
        conversa.setEncerrada(encerrada);
        return conversa;
    }

    @Test
    void selecionarConversaLimpaRascunhoServidorEMarcaHistoricoComoLido() {
        var conversa = conversa(false);
        when(cliente.buscarHistorico(6L, 1L)).thenReturn(List.of());
        when(cliente.listarInbox(1L)).thenReturn(List.of(conversa));
        bean.setNovaMensagem("Rascunho de outra conversa");
        bean.selecionarConversa(conversa);
        assertSame(conversa, bean.getConversaSelecionada());
        assertEquals("", bean.getNovaMensagem());
        verify(cliente).marcarComoLidas(6L, 1L);
        verify(sessao).atualizarNotificacoesChat();
        verify(prime, never()).executeScript(anyString());
    }

    @Test
    void atualizarHistoricoPreservaRascunhoECompositorDaConversaAberta() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("Texto ainda em edição");
        when(cliente.listarInbox(1L)).thenReturn(List.of(conversa(false)));
        when(cliente.buscarHistorico(6L, 1L)).thenReturn(List.of());
        bean.chegouMensagemPeloWebSocket();
        assertEquals("Texto ainda em edição", bean.getNovaMensagem());
        verify(cliente).marcarComoLidas(6L, 1L);
        verify(prime.ajax(), never()).update("formChat:painelCompositor");
        verify(prime.ajax()).addCallbackParam("chatAtualizado", true);
    }

    @Test
    void encerramentoAtualizaCompositorEContextoDaPartida() {
        bean.setConversaSelecionada(conversa(false));
        when(cliente.listarInbox(1L)).thenReturn(List.of(conversa(true)));
        when(cliente.buscarHistorico(6L, 1L)).thenReturn(List.of());
        bean.chegouMensagemPeloWebSocket();
        assertTrue(bean.getConversaSelecionada().isEncerrada());
        assertEquals("Jogo finalizado", bean.getTituloContextoConversa());
        verify(prime.ajax()).update("formChat:painelCompositor");
    }

    @Test
    void conversaRemovidaRetornaParaInboxDuranteAtualizacao() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("Rascunho");
        when(cliente.listarInbox(1L)).thenReturn(List.of());
        bean.chegouMensagemPeloWebSocket();
        assertNull(bean.getConversaSelecionada());
        assertEquals("", bean.getNovaMensagem());
        assertTrue(bean.getMensagens().isEmpty());
        verify(prime.ajax()).update("formChat:painelChat");
    }

    @Test
    void falhaNoEnvioPreservaTextoEApresentaMensagem() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("Confirmamos o horário?");
        doThrow(new RuntimeException("Falha de teste")).when(cliente).enviarMensagem(6L, 1L, "Confirmamos o horário?");
        bean.enviarMensagem();
        assertEquals("Confirmamos o horário?", bean.getNovaMensagem());
        verify(prime.ajax()).addCallbackParam("mensagemEnviada", false);
        verify(prime.ajax(), never()).addCallbackParam("mensagemEnviada", true);
        verify(faces).addMessage(isNull(), argThat(msg -> msg.getSeverity() == FacesMessage.SEVERITY_ERROR));
    }

    @Test
    void falhaAposEnvioConfirmadoLimpaTextoEInformaSucessoParcial() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("Mensagem de teste");
        when(cliente.buscarHistorico(6L, 1L)).thenThrow(new RuntimeException("Falha ao atualizar"));
        bean.enviarMensagem();
        verify(cliente).enviarMensagem(6L, 1L, "Mensagem de teste");
        assertEquals("", bean.getNovaMensagem());
        verify(prime.ajax()).addCallbackParam("mensagemEnviada", true);
        verify(faces).addMessage(isNull(), argThat(msg -> msg.getSeverity() == FacesMessage.SEVERITY_WARN
                && msg.getDetail().startsWith("Mensagem enviada.")));
    }

    @Test
    void mensagemVaziaSolicitaPreenchimento() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("  ");
        bean.enviarMensagem();
        verify(cliente, never()).enviarMensagem(anyLong(), anyLong(), anyString());
        verify(faces).addMessage(isNull(), argThat(msg -> msg.getDetail().contains("Escreva uma mensagem")));
    }

    @Test
    void falhaDeAtualizacaoPodeSerRepetidaComRascunhoPreservado() {
        bean.setConversaSelecionada(conversa(false));
        bean.setNovaMensagem("Rascunho preservado");
        when(cliente.listarInbox(1L)).thenThrow(new RuntimeException("Indisponível"));
        bean.chegouMensagemPeloWebSocket();
        assertEquals("Rascunho preservado", bean.getNovaMensagem());
        verify(prime.ajax()).addCallbackParam("chatAtualizado", false);
    }

    @Test
    void falhaDaInboxPermiteDistinguirErroDeListaVazia() {
        when(cliente.listarInbox(1L)).thenThrow(new RuntimeException("Indisponível")).thenReturn(List.of());
        bean.carregarInbox();
        assertTrue(bean.isFalhaInbox());
        bean.carregarInbox();
        assertFalse(bean.isFalhaInbox());
        assertTrue(bean.getInbox().isEmpty());
    }
}
