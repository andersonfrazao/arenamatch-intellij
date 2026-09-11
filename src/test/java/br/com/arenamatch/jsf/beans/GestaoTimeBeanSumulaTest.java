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
    void deveMontarEventosComMinutosEGolContra() {
        GestaoTimeBean bean = new GestaoTimeBean();
        var linha = new GestaoTimeBean.LinhaAtleta(
                new AtletaDTO(10L, "Atleta", null, SituacaoAtleta.ATIVO));
        linha.setPapel(PapelParticipacao.TITULAR);
        linha.setGols(2);
        linha.setMinutosGols("12, 67");
        linha.setAmarelos(1);
        linha.setMinutosAmarelos("40");
        bean.setEscalacao(new ArrayList<>(List.of(linha)));
        bean.setGolsContra(1);
        bean.setMinutosGolsContra("74");

        var request = bean.montarRequest(true);

        assertEquals(List.of(12, 67), request.eventos().stream()
                .filter(e -> e.tipo() == TipoEventoSumula.GOL).map(e -> e.minuto()).toList());
        assertEquals(40, request.eventos().stream()
                .filter(e -> e.tipo() == TipoEventoSumula.CARTAO_AMARELO).findFirst().orElseThrow().minuto());
        assertEquals(74, request.eventos().stream()
                .filter(e -> e.tipo() == TipoEventoSumula.GOL_CONTRA).findFirst().orElseThrow().minuto());
    }

    @Test
    void deveConferirTotalDaSumulaComPlacarDoTime() {
        GestaoTimeBean bean = new GestaoTimeBean();
        var linha = new GestaoTimeBean.LinhaAtleta(
                new AtletaDTO(10L, "Atleta", null, SituacaoAtleta.ATIVO));
        linha.setGols(1);
        bean.setEscalacao(List.of(linha));
        bean.setGolsContra(1);
        bean.setDisponibilidade(new DisponibilidadeGestaoPartidaDTO(
                true, true, true, true, true, true, null,
                "Meu time", 2, 0, "Adversário", 2, "Disponível"));

        assertTrue(bean.isGolsConferem());
        assertEquals("2 de 2 gol(s) do placar atribuídos na súmula.", bean.getResumoConferenciaGols());
    }
}
