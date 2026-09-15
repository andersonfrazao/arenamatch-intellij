package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.DisponibilidadeGestaoPartidaDTO;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.TipoEventoSumula;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestaoTimeBeanSumulaTest {

    @Test
    void deveMontarEventosSemMinutosNemGolContra() {
        GestaoTimeBean bean = new GestaoTimeBean();
        var linha = new GestaoTimeBean.LinhaAtleta(
                new AtletaDTO(10L, "Atleta", null, SituacaoAtleta.ATIVO));
        linha.setPapel(PapelParticipacao.TITULAR);
        linha.setGols(2);
        linha.setAmarelos(1);
        bean.setEscalacao(new ArrayList<>(List.of(linha)));

        var request = bean.montarRequest(true);

        assertEquals(2, request.eventos().stream()
                .filter(e -> e.tipo() == TipoEventoSumula.GOL).count());
        assertTrue(request.eventos().stream().allMatch(e -> e.minuto() == null));
        assertTrue(request.eventos().stream().noneMatch(e -> e.tipo() == TipoEventoSumula.GOL_CONTRA));
    }

    @Test
    void deveConferirTotalDaSumulaComPlacarDoTime() {
        GestaoTimeBean bean = new GestaoTimeBean();
        var linha = new GestaoTimeBean.LinhaAtleta(
                new AtletaDTO(10L, "Atleta", null, SituacaoAtleta.ATIVO));
        linha.setGols(1);
        bean.setEscalacao(List.of(linha));
        bean.setDisponibilidade(new DisponibilidadeGestaoPartidaDTO(
                true, true, true, true, true, true, null,
                "Meu time", 2, 0, "Adversário", 2, "Disponível"));

        assertTrue(bean.isGolsConferem());
        assertEquals("1 de 2 gol(s) atribuído(s) aos jogadores. A diferença pode ficar sem autor individual.",
                bean.getResumoConferenciaGols());
    }
}
