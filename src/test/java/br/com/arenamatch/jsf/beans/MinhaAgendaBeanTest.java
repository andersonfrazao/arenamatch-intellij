package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.LigaClient;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinhaAgendaBeanTest {
    private final MinhaAgendaBean bean = new MinhaAgendaBean();
    private final AgendaClient agenda = mock(AgendaClient.class);
    private final LigaClient ligas = mock(LigaClient.class);
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(bean, "agendaClient", agenda);
        ReflectionTestUtils.setField(bean, "ligaClient", ligas);
        bean.setMeuTimeId(7L);
        when(ligas.buscarConvitesParaAgenda(7L)).thenReturn(List.of());
        when(agenda.buscarTodosMeusJogos(7L)).thenReturn(List.of());
    }

    private ResumoAgendaDTO dia(LocalDate data) {
        var dia = new ResumoAgendaDTO();
        dia.setData(data);
        return dia;
    }

    @Test
    void atualizarCalendarioPreservaDiaSelecionadoESeuRascunho() {
        var selecionado = dia(hoje.plusDays(4));
        bean.setDiaSelecionado(selecionado);
        bean.setGolsMandanteInformado(3);
        var novoResumo = dia(selecionado.getData());
        when(agenda.buscarCalendario(7L, hoje)).thenReturn(List.of(dia(hoje), novoResumo));
        bean.carregarCalendario();
        assertSame(novoResumo, bean.getDiaSelecionado());
        assertEquals(3, bean.getGolsMandanteInformado());
        verify(agenda).buscarDetalhesDia(7L, selecionado.getData());
    }

    @Test
    void novoPeriodoSelecionaPrimeiroDiaELimpaRascunhos() {
        bean.setDiaSelecionado(dia(hoje));
        bean.setGolsMandanteInformado(3);
        bean.setEventoEmCancelamento(new EventoAgendaDTO());
        when(agenda.buscarCalendario(7L, hoje.plusDays(15))).thenReturn(List.of(dia(hoje.plusDays(15))));
        bean.avancarPeriodo();
        assertEquals(hoje.plusDays(15), bean.getDiaSelecionado().getData());
        assertNull(bean.getGolsMandanteInformado());
        assertNull(bean.getEventoEmCancelamento());
    }

    @Test
    void hojeSelecionaDataAtualMesmoSeDiaAnteriorCouberNoPeriodo() {
        bean.setDiaSelecionado(dia(hoje.plusDays(4)));
        when(agenda.buscarCalendario(7L, hoje)).thenReturn(List.of(dia(hoje), dia(hoje.plusDays(4))));
        bean.voltarParaHoje();
        assertEquals(hoje, bean.getDiaSelecionado().getData());
    }

    @Test
    void calendarioVazioLimpaSelecaoEDetalhesAntigos() {
        bean.setDiaSelecionado(dia(hoje));
        ReflectionTestUtils.setField(bean, "eventosDoDia", List.of(new EventoAgendaDTO()));
        when(agenda.buscarCalendario(7L, hoje)).thenReturn(List.of());
        bean.carregarCalendario();
        assertNull(bean.getDiaSelecionado());
        assertTrue(bean.getEventosDoDia().isEmpty());
    }
}
