package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.GestaoPartidaDTO;
import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.StatusGestaoPartida;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GestaoTimeBeanHistoricoTest {

    @Test
    void deveReabrirParticipacaoDeAtletaInativado() {
        GestaoTimeBean bean = new GestaoTimeBean();
        bean.setAtletas(List.of(new AtletaDTO(7L, "Atleta histórico", "Veterano", SituacaoAtleta.INATIVO)));
        bean.setEscalacao(new ArrayList<>());
        var participacao = new GestaoPartidaDTO.ParticipacaoDTO(
                1L, 7L, "Atleta histórico", PapelParticipacao.RESERVA,
                15, "Meio", null, null, null, 0);
        var dto = new GestaoPartidaDTO(1L, 2L, 3L, StatusGestaoPartida.PUBLICADO,
                EtapaGestaoPartida.PUBLICACAO, "4-4-2", null, 1L,
                null, null, List.of(participacao), List.of());

        bean.aplicar(dto);

        assertEquals(1, bean.getEscalacao().size());
        assertEquals(SituacaoAtleta.INATIVO, bean.getEscalacao().getFirst().getAtleta().getSituacao());
        assertEquals(PapelParticipacao.RESERVA, bean.getEscalacao().getFirst().getPapel());
    }
}
