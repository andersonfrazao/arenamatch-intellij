package br.com.arenamatch.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.StatusGestaoPartida;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class GestaoPartidaMappingTest {

    @Test
    void deveMapearTabelaEControleOtimista() throws Exception {
        Table table = GestaoPartida.class.getAnnotation(Table.class);
        Field versao = GestaoPartida.class.getDeclaredField("versao");

        assertNotNull(table);
        assertEquals("gestao_partida", table.name());
        assertNotNull(versao.getAnnotation(Version.class));
    }

    @Test
    void deveIniciarComoRascunhoNaEtapaEscalacao() {
        GestaoPartida gestao = new GestaoPartida();

        assertEquals(StatusGestaoPartida.RASCUNHO, gestao.getStatus());
        assertEquals(EtapaGestaoPartida.ESCALACAO, gestao.getEtapa());
    }
}
