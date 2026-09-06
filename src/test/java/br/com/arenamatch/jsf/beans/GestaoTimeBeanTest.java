package br.com.arenamatch.jsf.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.SituacaoAtleta;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GestaoTimeBeanTest {

    private GestaoTimeBean bean;
    private GestaoTimeBean.LinhaAtleta atleta;

    @BeforeEach
    void preparar() {
        bean = new GestaoTimeBean();
        atleta = new GestaoTimeBean.LinhaAtleta(
                new AtletaDTO(1L, "Rafael Santos", "Rafa", SituacaoAtleta.ATIVO));
        bean.setEscalacao(new ArrayList<>());
        bean.getEscalacao().add(atleta);
    }

    @Test
    void deveMontarOnzeSlotsNaFormacaoTresCincoDois() {
        bean.setFormacao("3-5-2");

        assertEquals(11, bean.getSlotsFormacao().size());
        assertEquals("Goleiro", bean.getSlotsFormacao().getFirst().rotulo());
    }

    @Test
    void devePosicionarAtletaSelecionadoComoTitular() {
        var slot = bean.getSlotsFormacao().get(1);

        bean.selecionarAtleta(atleta);
        bean.clicarSlot(slot);

        assertEquals(PapelParticipacao.TITULAR, atleta.getPapel());
        assertEquals(slot.id(), atleta.getSlotTatico());
        assertEquals(slot.x(), atleta.getCoordenadaX());
        assertNull(bean.getAtletaSelecionadoId());
    }

    @Test
    void devePreservarAtletaComoRelacionadoAoTrocarFormacao() {
        bean.selecionarAtleta(atleta);
        bean.clicarSlot(bean.getSlotsFormacao().get(1));

        bean.setFormacao("4-4-2");
        bean.alterarFormacao();

        assertEquals(PapelParticipacao.RELACIONADO, atleta.getPapel());
        assertNull(atleta.getSlotTatico());
        assertEquals(1, bean.getRelacionados().size());
    }

    @Test
    void deveCriarFormacaoPersonalizadaValida() {
        bean.setFormacao("PERSONALIZADA");
        bean.setFormacaoPersonalizada("2-3-1");

        assertEquals(7, bean.getSlotsFormacao().size());
    }

    @Test
    void deveMoverAtletaSelecionadoEntreDestinosNoFluxoPorToque() {
        bean.selecionarAtleta(atleta);
        bean.clicarSlot(bean.getSlotsFormacao().get(1));

        bean.selecionarAtleta(atleta);
        bean.moverSelecionadoParaReservas();
        assertEquals(PapelParticipacao.RESERVA, atleta.getPapel());

        bean.selecionarAtleta(atleta);
        bean.moverSelecionadoParaRelacionados();
        assertEquals(PapelParticipacao.RELACIONADO, atleta.getPapel());

        bean.selecionarAtleta(atleta);
        bean.moverSelecionadoParaDisponiveis();
        assertNull(atleta.getPapel());
        assertEquals(1, bean.getDisponiveis().size());
        assertNull(bean.getAtletaSelecionadoId());
    }
}
