package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.OpcoesPartidaExternaDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.jsf.client.PartidaExternaClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistroPartidaBeanTest {
    @Test
    void recebeFiltrosDaBuscaEExigeCategoriaQuandoFiltroEstavaEmTodas() {
        testarParametros(Map.of("data", "2026-10-01", "modalidade", "FUTSAL"), null);
        testarParametros(Map.of("categoria", "VETERANO_35", "modalidade", "FUTSAL"), Categoria.VETERANO_35);
    }

    private void testarParametros(Map<String, String> parametros, Categoria categoriaEsperada) {
        FacesContext contexto = mock(FacesContext.class);
        ExternalContext externo = mock(ExternalContext.class);
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getRequestParameterMap()).thenReturn(parametros);
        SessaoBean sessao = mock(SessaoBean.class);
        when(sessao.isLogado()).thenReturn(true);
        PartidaExternaClient client = mock(PartidaExternaClient.class);
        var opcoes = new OpcoesPartidaExternaDTO();
        opcoes.setNomeTime("Mocidade FC");
        opcoes.setModalidades(List.of(Modalidade.FUTSAL));
        opcoes.setAdversarios(List.of("União da Vila"));
        when(client.buscarOpcoes()).thenReturn(opcoes);
        RegistroPartidaBean bean = bean(client, sessao);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.init();
        }
        assertTrue(bean.isPronto());
        assertEquals(Modalidade.FUTSAL, bean.getDto().getModalidade());
        assertEquals(categoriaEsperada, bean.getDto().getCategoria());
        assertNotNull(bean.getDto().getChaveSolicitacao());
        if (parametros.containsKey("data")) assertEquals(LocalDate.of(2026, 10, 1), bean.getDto().getData());
        assertEquals(List.of("União da Vila"), bean.completarAdversario("união"));
    }

    @Test
    void erroDeValidacaoMantemDadosEChaveDoFormulario() {
        PartidaExternaClient client = mock(PartidaExternaClient.class);
        RegistroPartidaBean bean = bean(client, mock(SessaoBean.class));
        ReflectionTestUtils.setField(bean, "pronto", true);
        bean.getDto().setChaveSolicitacao(java.util.UUID.randomUUID());
        bean.getDto().setNomeAdversario("União da Vila");
        var chave = bean.getDto().getChaveSolicitacao();
        when(client.registrar(bean.getDto())).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Dados inválidos", null,
                "Informe um horário final posterior ao início.".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        FacesContext contexto = mock(FacesContext.class);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.salvar();
        }
        assertEquals(chave, bean.getDto().getChaveSolicitacao());
        assertEquals("União da Vila", bean.getDto().getNomeAdversario());
        verify(contexto).addMessage(isNull(), any(FacesMessage.class));
    }

    @Test
    void salvarRetroativoRedirecionaParaODiaRegistradoNaAgenda() throws Exception {
        PartidaExternaClient client = mock(PartidaExternaClient.class);
        RegistroPartidaBean bean = bean(client, mock(SessaoBean.class));
        ReflectionTestUtils.setField(bean, "pronto", true);
        EventoAgendaDTO evento = new EventoAgendaDTO();
        evento.setDataHora(LocalDate.of(2026, 1, 2).atTime(12, 0));
        when(client.registrar(bean.getDto())).thenReturn(evento);
        FacesContext contexto = mock(FacesContext.class);
        ExternalContext externo = mock(ExternalContext.class);
        Flash flash = mock(Flash.class);
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getFlash()).thenReturn(flash);
        when(externo.getRequestContextPath()).thenReturn("");
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.salvar();
        }
        verify(flash).setKeepMessages(true);
        verify(externo).redirect("/minha-agenda.xhtml?data=2026-01-02");
    }

    private RegistroPartidaBean bean(PartidaExternaClient client, SessaoBean sessao) {
        var bean = new RegistroPartidaBean();
        ReflectionTestUtils.setField(bean, "partidaExternaClient", client);
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        return bean;
    }
}
