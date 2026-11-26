package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.HorarioAgendaTimeDTO;
import br.com.arenamatch.dto.OpcoesPartidaExternaDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.Modalidade;
import java.math.BigDecimal;
import br.com.arenamatch.jsf.client.PartidaExternaClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
        opcoes.setHorarios(List.of(
                horario(DayOfWeek.THURSDAY, Modalidade.FUTSAL, Categoria.ESPORTE, 20),
                horario(DayOfWeek.THURSDAY, Modalidade.FUTSAL, Categoria.VETERANO_35, 20)));
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

    @Test
    void mandantePreCarregaTaxaDoCadastroEAdversarioFicaVisitante() {
        var opcoes = opcoes(MandoPartida.MANDANTE);
        opcoes.setValorTaxa(new BigDecimal("120.00"));
        RegistroPartidaBean bean = iniciar(opcoes);
        assertTrue(bean.isMandante());
        assertEquals(MandoPartida.VISITANTE, bean.getMandoAdversario());
        assertEquals(new BigDecimal("120.00"), bean.getTaxaCadastro());
        assertNull(bean.getDto().getValorTaxa(), "a taxa do mandante vem do cadastro no backend");
        assertNull(bean.getDto().getNomeCampo());
        assertNull(bean.getDto().getEndereco());
    }

    @Test
    void visitanteVeAdversarioComoMandanteETaxaVazia() {
        RegistroPartidaBean bean = iniciar(opcoes(MandoPartida.VISITANTE));
        assertFalse(bean.isMandante());
        assertEquals(MandoPartida.MANDANTE, bean.getMandoAdversario());
        assertNull(bean.getDto().getValorTaxa());
        assertEquals("Valor que seu time vai pagar ao mandante. Deixe em branco se ainda não souber.",
                bean.getOrientacaoTaxa());
    }

    @Test
    void calendarioBloqueiaDiasForaDaAgendaEAvisaDataInvalida() {
        RegistroPartidaBean bean = iniciar(opcoes(MandoPartida.VISITANTE));
        assertEquals(List.of(0, 1, 2, 3, 4, 6), bean.getDiasDesabilitados());
        assertEquals("sexta", bean.getDiasDoTime());
        bean.getDto().setData(LocalDate.of(2026, 11, 28)); // sábado
        assertTrue(bean.isDataForaDaAgenda());
        bean.getDto().setData(LocalDate.of(2026, 11, 27)); // sexta
        assertFalse(bean.isDataForaDaAgenda());
    }

    @Test
    void mandanteVeHorarioDoCadastroDoDiaEscolhido() {
        var opcoes = opcoes(MandoPartida.MANDANTE);
        opcoes.setNomeCampo("Arena Mocidade");
        RegistroPartidaBean bean = iniciar(opcoes);
        assertEquals(Modalidade.FUTEBOL_CAMPO, bean.getDto().getModalidade());
        assertEquals(Categoria.ESPORTE, bean.getDto().getCategoria(), "única categoria da agenda");
        bean.getDto().setData(LocalDate.of(2026, 11, 27));
        assertEquals("20:00 às 22:00", bean.getHorarioDoDiaTexto());
        assertEquals("Arena Mocidade", bean.getNomeCampoCadastro());
        bean.getDto().setData(LocalDate.of(2026, 11, 28));
        assertNull(bean.getHorarioDoDiaTexto());
    }

    @Test
    void trocarModalidadeLimpaCategoriaSemHorarioEEscondeModalidadeSemAgenda() {
        var opcoes = opcoes(MandoPartida.VISITANTE);
        opcoes.setModalidades(List.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL, Modalidade.FUTEBOL_SOCIETY));
        opcoes.setHorarios(List.of(
                horario(DayOfWeek.FRIDAY, Modalidade.FUTEBOL_CAMPO, Categoria.ESPORTE, 20),
                horario(DayOfWeek.SUNDAY, Modalidade.FUTSAL, Categoria.MASTER, 9),
                horario(DayOfWeek.SUNDAY, Modalidade.FUTSAL, Categoria.VETERANO_35, 9)));
        RegistroPartidaBean bean = iniciar(opcoes);
        assertEquals(List.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL), bean.getModalidades());
        bean.getDto().setModalidade(Modalidade.FUTEBOL_CAMPO);
        bean.aoMudarModalidade();
        assertEquals(Categoria.ESPORTE, bean.getDto().getCategoria());
        bean.getDto().setModalidade(Modalidade.FUTSAL);
        bean.aoMudarModalidade();
        assertNull(bean.getDto().getCategoria());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), bean.getDiasDesabilitados());
        assertFalse(bean.isSemAgenda());
    }

    private static HorarioAgendaTimeDTO horario(DayOfWeek dia, Modalidade modalidade, Categoria categoria, int hora) {
        return new HorarioAgendaTimeDTO(dia, modalidade, categoria, LocalTime.of(hora, 0), LocalTime.of(hora + 2, 0));
    }

    private OpcoesPartidaExternaDTO opcoes(MandoPartida mando) {
        var opcoes = new OpcoesPartidaExternaDTO();
        opcoes.setNomeTime("Mocidade FC");
        opcoes.setModalidades(List.of(Modalidade.FUTEBOL_CAMPO));
        opcoes.setAdversarios(List.of());
        opcoes.setMandoTime(mando);
        opcoes.setHorarios(List.of(horario(DayOfWeek.FRIDAY, Modalidade.FUTEBOL_CAMPO, Categoria.ESPORTE, 20)));
        return opcoes;
    }

    private RegistroPartidaBean iniciar(OpcoesPartidaExternaDTO opcoes) {
        FacesContext contexto = mock(FacesContext.class);
        ExternalContext externo = mock(ExternalContext.class);
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getRequestParameterMap()).thenReturn(Map.of());
        SessaoBean sessao = mock(SessaoBean.class);
        when(sessao.isLogado()).thenReturn(true);
        PartidaExternaClient client = mock(PartidaExternaClient.class);
        when(client.buscarOpcoes()).thenReturn(opcoes);
        RegistroPartidaBean bean = bean(client, sessao);
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.init();
        }
        assertTrue(bean.isPronto());
        return bean;
    }

    private RegistroPartidaBean bean(PartidaExternaClient client, SessaoBean sessao) {
        var bean = new RegistroPartidaBean();
        ReflectionTestUtils.setField(bean, "partidaExternaClient", client);
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        return bean;
    }
}
