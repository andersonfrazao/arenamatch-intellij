package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.JogoRealizadoDTO;
import br.com.arenamatch.dto.PaginaJogosRealizadosDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.jsf.client.TimeClient;
import jakarta.faces.context.FacesContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RankingBeanTest {
    private final RankingBean bean = new RankingBean();
    private final TimeClient cliente = mock(TimeClient.class);
    private final SessaoBean sessao = mock(SessaoBean.class);
    private MockedStatic<FacesContext> contexto;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(bean, "timeClient", cliente);
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        when(sessao.isAcessoCompleto()).thenReturn(true);
        when(sessao.isPossuiTime()).thenReturn(true);
        contexto = mockStatic(FacesContext.class);
        contexto.when(FacesContext::getCurrentInstance).thenReturn(mock(FacesContext.class));
    }

    @AfterEach
    void fechar() { contexto.close(); }

    @Test
    void falhaAoCarregarMaisRepetePaginaSemPularJogos() {
        var primeiro = new JogoRealizadoDTO();
        var segundo = new JogoRealizadoDTO();
        when(cliente.buscarMeuScout(Modalidade.FUTEBOL_CAMPO)).thenReturn(new TimeDTO());
        when(cliente.buscarJogosRealizadosDoScout(0, Modalidade.FUTEBOL_CAMPO))
                .thenReturn(new PaginaJogosRealizadosDTO(List.of(primeiro), true));
        when(cliente.buscarJogosRealizadosDoScout(1, Modalidade.FUTEBOL_CAMPO))
                .thenThrow(new RuntimeException("Falha de teste"))
                .thenReturn(new PaginaJogosRealizadosDTO(List.of(segundo), false));
        bean.exibirScout();
        bean.carregarMaisJogos();
        assertTrue(bean.isFalhaJogos());
        assertEquals(List.of(primeiro), bean.getJogosRealizados());
        bean.carregarMaisJogos();
        assertFalse(bean.isFalhaJogos());
        assertFalse(bean.isTemMaisJogos());
        assertEquals(List.of(primeiro, segundo), bean.getJogosRealizados());
        verify(cliente, times(2)).buscarJogosRealizadosDoScout(1, Modalidade.FUTEBOL_CAMPO);
        verify(cliente, never()).buscarJogosRealizadosDoScout(2, Modalidade.FUTEBOL_CAMPO);
    }

    @Test
    void falhaNaPrimeiraPaginaPermiteRepetirMesmoComHistoricoVazio() {
        when(cliente.buscarMeuScout(Modalidade.FUTEBOL_CAMPO)).thenReturn(new TimeDTO());
        when(cliente.buscarJogosRealizadosDoScout(0, Modalidade.FUTEBOL_CAMPO))
                .thenThrow(new RuntimeException("Falha de teste"))
                .thenReturn(new PaginaJogosRealizadosDTO(List.of(new JogoRealizadoDTO()), false));
        bean.exibirScout();
        assertTrue(bean.isFalhaJogos());
        bean.carregarMaisJogos();
        assertEquals(1, bean.getJogosRealizados().size());
        assertFalse(bean.isFalhaJogos());
        verify(cliente, times(2)).buscarJogosRealizadosDoScout(0, Modalidade.FUTEBOL_CAMPO);
    }

    @Test
    void trocarModalidadeReiniciaHistoricoECarregaEstatisticasCorrespondentes() {
        var jogoCampo = new JogoRealizadoDTO();
        var jogoFutsal = new JogoRealizadoDTO();
        when(cliente.buscarMeuScout(Modalidade.FUTEBOL_CAMPO)).thenReturn(new TimeDTO());
        when(cliente.buscarMeuScout(Modalidade.FUTSAL)).thenReturn(new TimeDTO());
        when(cliente.buscarJogosRealizadosDoScout(0, Modalidade.FUTEBOL_CAMPO))
                .thenReturn(new PaginaJogosRealizadosDTO(List.of(jogoCampo), true));
        when(cliente.buscarJogosRealizadosDoScout(0, Modalidade.FUTSAL))
                .thenReturn(new PaginaJogosRealizadosDTO(List.of(jogoFutsal), false));
        when(cliente.buscarRankingGeral(Modalidade.FUTSAL)).thenReturn(List.of());
        bean.exibirScout();
        bean.setModalidade(Modalidade.FUTSAL);
        bean.alterarModalidade();
        assertEquals(List.of(jogoFutsal), bean.getJogosRealizados());
        assertFalse(bean.isTemMaisJogos());
        verify(cliente).buscarMeuScout(Modalidade.FUTSAL);
    }

    @Test
    void modalidadeComFalhaLimpaRankingAnteriorEPermiteNovaTentativa() {
        when(cliente.buscarRankingGeral(Modalidade.FUTEBOL_CAMPO)).thenReturn(List.of(new TimeDTO()));
        when(cliente.buscarRankingGeral(Modalidade.FUTSAL))
                .thenThrow(new RuntimeException("Falha de teste")).thenReturn(List.of(new TimeDTO()));
        bean.init();
        bean.setModalidade(Modalidade.FUTSAL);
        bean.alterarModalidade();
        assertTrue(bean.getRanking().isEmpty());
        assertTrue(bean.isFalhaRanking());
        bean.exibirRanking();
        assertFalse(bean.isFalhaRanking());
        assertEquals(1, bean.getRanking().size());
    }

    @Test
    void acessoBasicoNavegaAbasSemConsultarDadosRestritos() {
        when(sessao.isAcessoCompleto()).thenReturn(false);
        bean.init();
        bean.exibirScout();
        bean.setModalidade(Modalidade.FUTSAL);
        bean.alterarModalidade();
        bean.exibirRanking();
        assertTrue(bean.getRanking().isEmpty());
        assertNull(bean.getScout());
        verifyNoInteractions(cliente);
    }
}
