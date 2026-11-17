package br.com.arenamatch.jsf.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.LigaClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinhaAgendaBeanTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 2);

    @Mock private AgendaClient agendaClient;
    @Mock private LigaClient ligaClient;
    @InjectMocks private MinhaAgendaBean bean;

    @BeforeEach
    void configurar() {
        bean.setMeuTimeId(7L);
        when(agendaClient.buscarCalendario(anyLong(), any())).thenReturn(periodo(INICIO, 30));
        when(agendaClient.buscarDetalhesDia(anyLong(), any())).thenReturn(new ArrayList<>());
        when(agendaClient.buscarTodosMeusJogos(anyLong())).thenReturn(new ArrayList<>());
        when(ligaClient.buscarConvitesParaAgenda(anyLong())).thenReturn(new ArrayList<>());
    }

    @Test
    void recarregarMantemODiaAbertoQuandoEleContinuaNoPeriodo() {
        bean.setDiaSelecionado(dia(LocalDate.of(2026, 11, 15)));

        bean.carregarCalendario();

        assertEquals(LocalDate.of(2026, 11, 15), bean.getDiaSelecionado().getData());
    }

    @Test
    void recarregarAbreOPrimeiroDiaQuandoODiaAbertoSaiuDoPeriodo() {
        bean.setDiaSelecionado(dia(LocalDate.of(2026, 10, 20)));

        bean.carregarCalendario();

        assertEquals(INICIO, bean.getDiaSelecionado().getData());
    }

    @Test
    void primeiraCargaAbreOPrimeiroDia() {
        bean.carregarCalendario();

        assertEquals(INICIO, bean.getDiaSelecionado().getData());
    }

    private static List<ResumoAgendaDTO> periodo(LocalDate inicio, int dias) {
        List<ResumoAgendaDTO> lista = new ArrayList<>();
        for (int i = 0; i < dias; i++) {
            lista.add(dia(inicio.plusDays(i)));
        }
        return lista;
    }

    private static ResumoAgendaDTO dia(LocalDate data) {
        ResumoAgendaDTO dia = new ResumoAgendaDTO();
        dia.setData(data);
        return dia;
    }
}
