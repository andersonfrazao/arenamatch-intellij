package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.*;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.jsf.client.CadastroClient;
import jakarta.faces.context.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CadastroModalidadesBeanTest {
    @Test void salvarPreservaModalidadeAntigaSemHorariosEAcrescentaANova() {
        verificarSalvamento(Modalidade.FUTSAL, Set.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL));
    }

    @Test void modalidadeSemHorarioNaListaNaoEAcrescentadaAoSalvar() {
        verificarSalvamento(Modalidade.FUTEBOL_CAMPO, Set.of(Modalidade.FUTEBOL_CAMPO));
    }

    private void verificarSalvamento(Modalidade modalidadeHorario, Set<Modalidade> esperadas) {
        CadastroDTO dto = new CadastroDTO(); dto.setModalidades(new ArrayList<>(List.of(Modalidade.FUTEBOL_CAMPO)));
        UsuarioDTO usuario = new UsuarioDTO(); usuario.setId(7L);
        CadastroClient client = mock(CadastroClient.class);
        SessaoBean sessao = mock(SessaoBean.class);
        when(sessao.isLogado()).thenReturn(true); when(sessao.getUsuarioLogado()).thenReturn(usuario);
        when(client.buscarDadosParaEdicao(7L)).thenReturn(dto);
        CadastroBean bean = new CadastroBean();
        ReflectionTestUtils.setField(bean,"cadastroClient",client);
        ReflectionTestUtils.setField(bean,"sessaoBean",sessao);
        bean.init();
        DisponibilidadeDTO horario = new DisponibilidadeDTO("Sábado","09:00","11:00"); horario.setModalidade(modalidadeHorario);
        bean.setAgenda(new ArrayList<>(List.of(horario)));
        // A seleção isolada não deve criar uma modalidade: apenas os horários adicionados contam.
        bean.setTempModalidade(Modalidade.FUTEBOL_SOCIETY);
        FacesContext context = mock(FacesContext.class);
        ExternalContext external = mock(ExternalContext.class);
        when(context.getExternalContext()).thenReturn(external); when(external.getFlash()).thenReturn(mock(Flash.class));
        try (var faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(context);
            assertEquals("/minha-agenda.xhtml?faces-redirect=true",bean.finalizar());
        }
        assertEquals(esperadas,new HashSet<>(dto.getModalidades()));
        assertEquals(List.of(horario),dto.getDisponibilidades());
        verify(client).validarFinalizacao(dto,bean.getAgenda(),false);
        verify(client).atualizarConta(7L,dto);
    }
}
