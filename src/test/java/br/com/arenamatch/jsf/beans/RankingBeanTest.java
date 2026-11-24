package br.com.arenamatch.jsf.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.TimeClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingBeanTest {

    @Mock private TimeClient timeClient;
    @Mock private SessaoBean sessaoBean;
    @Mock private AgendaClient agendaClient;
    @InjectMocks private RankingBean bean;

    @BeforeEach
    void configurar() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(5L);
        when(sessaoBean.isAcessoCompleto()).thenReturn(true);
        when(sessaoBean.isPossuiTime()).thenReturn(true);
        when(sessaoBean.getUsuarioLogado()).thenReturn(usuario);
        when(timeClient.buscarRankingGeral(any())).thenReturn(List.of());
    }

    @Test
    void abreNaModalidadeDoTimeQuandoEleNaoJogaAPadrao() {
        when(agendaClient.buscarMeuTime(5L)).thenReturn(time(Modalidade.FUTEBOL_SOCIETY));

        bean.init();

        assertEquals(Modalidade.FUTEBOL_SOCIETY, bean.getModalidade());
        verify(timeClient).buscarRankingGeral(Modalidade.FUTEBOL_SOCIETY);
    }

    @Test
    void mantemAPadraoQuandoOTimeTambemJogaNela() {
        when(agendaClient.buscarMeuTime(5L)).thenReturn(time(Modalidade.FUTEBOL_SOCIETY, Modalidade.FUTEBOL_CAMPO));

        bean.init();

        assertEquals(Modalidade.FUTEBOL_CAMPO, bean.getModalidade());
    }

    @Test
    void mantemAPadraoQuandoNaoConsegueBuscarOTime() {
        when(agendaClient.buscarMeuTime(5L)).thenThrow(new IllegalStateException("fora do ar"));

        bean.init();

        assertEquals(Modalidade.FUTEBOL_CAMPO, bean.getModalidade());
        verify(timeClient).buscarRankingGeral(Modalidade.FUTEBOL_CAMPO);
    }

    private static TimeResumoDTO time(Modalidade... modalidades) {
        TimeResumoDTO time = new TimeResumoDTO();
        time.setModalidades(List.of(modalidades));
        return time;
    }
}
