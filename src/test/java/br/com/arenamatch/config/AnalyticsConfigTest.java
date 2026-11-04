package br.com.arenamatch.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsConfigTest {
    private static final String ID = "G-VPZK0B0PSH";

    @Test void desabilitadoNaoIncluiAnalyticsMesmoEmPaginasPublicas() {
        AnalyticsConfig config = new AnalyticsConfig(false, "");
        assertFalse(config.habilitadoPara("/index.xhtml"));
        assertFalse(config.habilitadoPara("/cadastro.xhtml"));
    }

    @Test void habilitadoIncluiSomenteAsPaginasPublicasPrevistas() {
        AnalyticsConfig config = new AnalyticsConfig(true, " " + ID + " ");
        assertEquals(ID, config.getMeasurementId());
        for (String pagina : new String[]{"/index.xhtml", "/como-funciona.xhtml", "/privacidade.xhtml", "/login.xhtml", "/cadastro.xhtml"}) {
            assertTrue(config.habilitadoPara(pagina), pagina);
        }
        for (String pagina : new String[]{"/minha-agenda.xhtml", "/chat.xhtml", "/admin.xhtml", "/recuperar-senha.xhtml", "/ativar-conta.xhtml", "/interno/index.xhtml"}) {
            assertFalse(config.habilitadoPara(pagina), pagina);
        }
        assertFalse(config.habilitadoPara(null));
    }

    @Test void configuracaoHabilitadaComIdInvalidoFalhaAntesDeServirAPagina() {
        for (String id : new String[]{"", "UA-1234", "G-ABC<script>", "G-AB C"}) {
            assertThrows(IllegalArgumentException.class, () -> new AnalyticsConfig(true, id));
        }
        assertThrows(IllegalArgumentException.class, () -> new AnalyticsConfig(true, null));
    }
}
