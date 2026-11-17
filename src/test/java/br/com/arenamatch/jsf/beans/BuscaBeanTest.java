package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.BuscaClient;
import br.com.arenamatch.jsf.client.ParametroSistemaClient;
import br.com.arenamatch.jsf.client.PartidaClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.time.LocalDate;
import java.util.List;
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

class BuscaBeanTest {
    private final BuscaBean bean = new BuscaBean();
    private final BuscaClient busca = mock(BuscaClient.class);
    private final AgendaClient agenda = mock(AgendaClient.class);
    private final PartidaClient partidas = mock(PartidaClient.class);
    private final ParametroSistemaClient parametros = mock(ParametroSistemaClient.class);
    private final SessaoBean sessao = mock(SessaoBean.class);
    private final FacesContext contexto = mock(FacesContext.class);
    private final UsuarioDTO usuario = new UsuarioDTO();
    private final TimeResumoDTO meuTime = new TimeResumoDTO();
    private final TimeResumoDTO adversario = new TimeResumoDTO();
    private MockedStatic<FacesContext> faces;

    @BeforeEach
    void preparar() {
        usuario.setId(1L);
        usuario.setIdTime(7L);
        usuario.setPlanoAssinatura(PlanoAssinatura.PRO);
        meuTime.setId(7L);
        meuTime.setModalidades(List.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL));
        adversario.setId(8L);
        adversario.setNome("Adversário de testes");
        adversario.setModalidade(Modalidade.FUTEBOL_CAMPO);
        adversario.setCategoria(Categoria.values()[0]);
        when(sessao.getUsuarioLogado()).thenReturn(usuario);
        when(agenda.buscarMeuTime(1L)).thenReturn(meuTime);
        when(busca.filtrarTimes(any(), eq(7L))).thenReturn(List.of(adversario));
        ReflectionTestUtils.setField(bean, "buscaClient", busca);
        ReflectionTestUtils.setField(bean, "agendaClient", agenda);
        ReflectionTestUtils.setField(bean, "partidaClient", partidas);
        ReflectionTestUtils.setField(bean, "parametroSistemaClient", parametros);
        ReflectionTestUtils.setField(bean, "sessaoBean", sessao);
        faces = mockStatic(FacesContext.class);
        faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
        bean.init();
        bean.getFiltro().setModalidade(Modalidade.FUTEBOL_CAMPO);
        bean.getFiltro().setDataJogo(LocalDate.now().plusDays(7));
    }

    @AfterEach
    void encerrar() {
        faces.close();
    }

    @Test
    void validaModalidadeEDataAntesDeConsultar() {
        bean.getFiltro().setModalidade(null);
        bean.pesquisar();
        bean.getFiltro().setModalidade(Modalidade.FUTSAL);
        bean.getFiltro().setDataJogo(null);
        bean.pesquisar();
        verifyNoInteractions(busca);
        assertFalse(bean.isPesquisaRealizada());
        assertTrue(bean.getResultados().isEmpty());
    }

    @Test
    void distingueBuscaInicialEVaziaEReiniciaPaginacao() {
        assertTrue(bean.getMensagemResultadosVazios().contains("Escolha"));
        when(busca.filtrarTimes(any(), eq(7L))).thenReturn(List.of());
        bean.setPrimeiroResultado(12);
        bean.pesquisar();
        assertEquals(0, bean.getPrimeiroResultado());
        assertTrue(bean.isPesquisaRealizada());
        assertTrue(bean.getMensagemResultadosVazios().contains("Nenhum adversário"));
    }

    @Test
    void alteraModalidadeELimpaResultadosRevisaoEPaginacao() {
        bean.pesquisar();
        bean.prepararDesafio(adversario);
        bean.setPrimeiroResultado(12);
        bean.alterarModalidade();
        assertTrue(bean.getResultados().isEmpty());
        assertFalse(bean.isPesquisaRealizada());
        assertNull(bean.getTimeSelecionadoParaDesafio());
        assertNull(bean.getDataDesafio());
        assertNull(bean.getDataResultados());
        assertEquals(0, bean.getPrimeiroResultado());
    }

    @Test
    void confirmaComDataModalidadeECategoriaDoResultadoRevisado() {
        bean.pesquisar();
        LocalDate dataPesquisada = bean.getDataResultados();
        bean.prepararDesafio(adversario);
        bean.setMensagemDesafio("Vamos combinar pelo chat.");
        bean.getFiltro().setDataJogo(dataPesquisada.plusDays(7));
        bean.getFiltro().setModalidade(Modalidade.FUTSAL);
        assertEquals(dataPesquisada.toString(), bean.getDataJogoParametro());
        bean.confirmarDesafio();
        var enviado = ArgumentCaptor.forClass(DesafioDTO.class);
        verify(partidas).enviarDesafio(enviado.capture());
        assertEquals(dataPesquisada.atStartOfDay(), enviado.getValue().getDataHoraPartida());
        assertEquals(Modalidade.FUTEBOL_CAMPO, enviado.getValue().getModalidade());
        assertEquals(adversario.getCategoria(), enviado.getValue().getCategoria());
        assertEquals("Vamos combinar pelo chat.", enviado.getValue().getMensagem());
        assertNull(bean.getTimeSelecionadoParaDesafio());
        var consultas = ArgumentCaptor.forClass(FiltroBuscaDTO.class);
        verify(busca, times(2)).filtrarTimes(consultas.capture(), eq(7L));
        assertTrue(consultas.getAllValues().stream().allMatch(f ->
                f.getDataJogo().equals(dataPesquisada) && f.getModalidade() == Modalidade.FUTEBOL_CAMPO));
    }

    @Test
    void falhaAoAtualizarResultadosAposEnvioInformaQueOConviteFoiConcluido() {
        when(busca.filtrarTimes(any(), eq(7L))).thenReturn(List.of(adversario))
                .thenThrow(new RuntimeException("consulta indisponível"));
        bean.pesquisar();
        bean.prepararDesafio(adversario);
        clearInvocations(contexto);
        bean.confirmarDesafio();
        verify(partidas, times(1)).enviarDesafio(any());
        assertNull(bean.getTimeSelecionadoParaDesafio());
        var mensagens = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto, times(2)).addMessage(isNull(), mensagens.capture());
        assertTrue(mensagens.getAllValues().stream().noneMatch(m -> m.getSeverity() == FacesMessage.SEVERITY_ERROR));
        assertTrue(mensagens.getAllValues().get(1).getDetail().contains("ação no convite foi concluída"));
    }

    @Test
    void erroNoEnvioMantemRevisaoParaCorrecao() {
        bean.pesquisar();
        bean.prepararDesafio(adversario);
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "regra de negócio", null, null, null))
                .when(partidas).enviarDesafio(any());
        bean.confirmarDesafio();
        assertSame(adversario, bean.getTimeSelecionadoParaDesafio());
        verify(busca, times(1)).filtrarTimes(any(), eq(7L));
    }

    @Test
    void cancelaSomentePartidaDoCartaoEAtualizaResultados() {
        bean.pesquisar();
        bean.cancelarConviteEnviado(91L);
        verify(partidas).cancelarConvitePorId(91L);
        verify(partidas, never()).cancelarConvitePorAdversario(any(), any());
        verify(busca, times(2)).filtrarTimes(any(), eq(7L));
    }

    @Test
    void novaPesquisaComErroDescartaResultadosERevisaoAnteriores() {
        bean.pesquisar();
        bean.prepararDesafio(adversario);
        when(busca.filtrarTimes(any(), eq(7L))).thenThrow(new RuntimeException("falha simulada"));
        bean.pesquisar();
        assertTrue(bean.getResultados().isEmpty());
        assertNull(bean.getTimeSelecionadoParaDesafio());
        assertFalse(bean.isPesquisaRealizada());
    }

    @Test
    void aplicaLimiteBasicoMesmoComRaioMaiorSolicitado() {
        usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
        when(parametros.buscarRaioMaximoBuscaBasico()).thenReturn(10);
        bean.getFiltro().setRaioKm(100);
        bean.pesquisar();
        assertEquals(10, bean.getFiltro().getRaioKm());
    }
}
