package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.enums.SituacaoAtleta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GestaoTimeBeanFiltroTest {

    @Test
    void deveFiltrarPorNomeOuApelidoSemDiferenciarMaiusculas() {
        GestaoTimeBean bean = beanComAtletas();

        bean.setFiltroAtleta("  fraZão ");

        assertEquals(List.of(1L), bean.getAtletasFiltrados().stream().map(AtletaDTO::getId).toList());
    }

    @Test
    void deveCombinarBuscaComSituacao() {
        GestaoTimeBean bean = beanComAtletas();
        bean.setFiltroAtleta("andre");
        bean.setFiltroSituacao(SituacaoAtleta.INATIVO);

        assertEquals(List.of(3L), bean.getAtletasFiltrados().stream().map(AtletaDTO::getId).toList());
    }

    private GestaoTimeBean beanComAtletas() {
        GestaoTimeBean bean = new GestaoTimeBean();
        bean.setAtletas(List.of(
                new AtletaDTO(1L, "Anderson", "Frazão", SituacaoAtleta.ATIVO),
                new AtletaDTO(2L, "André Moraes", null, SituacaoAtleta.ATIVO),
                new AtletaDTO(3L, "André Vitória", "Vitória", SituacaoAtleta.INATIVO)));
        return bean;
    }
}
