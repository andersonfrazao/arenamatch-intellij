package br.com.arenamatch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.faces.application.Resource;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VersaoRecursoResourceHandlerTest {

    @Test
    void acrescentaAVersaoNosRecursosDoApp() {
        Resource versionado = VersaoRecursoResourceHandler.versionar(recurso("/jakarta.faces.resource/styles.css.xhtml?ln=css"), "css");

        assertEquals("/jakarta.faces.resource/styles.css.xhtml?ln=css&v=" + VersaoRecursoResourceHandler.VERSAO,
                versionado.getRequestPath());
    }

    @Test
    void naoMexeNosRecursosDoPrimeFaces() {
        Resource original = recurso("/jakarta.faces.resource/theme.css.xhtml?ln=primefaces-arya&v=14.0.5");

        assertSame(original, VersaoRecursoResourceHandler.versionar(original, "primefaces-arya"));
    }

    @Test
    void ignoraRecursoInexistenteOuSemBiblioteca() {
        Resource original = recurso("/x");

        assertNull(VersaoRecursoResourceHandler.versionar(null, "css"));
        assertSame(original, VersaoRecursoResourceHandler.versionar(original, null));
    }

    private static Resource recurso(String caminho) {
        return new Resource() {
            @Override public InputStream getInputStream() { return null; }
            @Override public Map<String, String> getResponseHeaders() { return Map.of(); }
            @Override public String getRequestPath() { return caminho; }
            @Override public URL getURL() { return null; }
            @Override public boolean userAgentNeedsUpdate(jakarta.faces.context.FacesContext context) { return false; }
        };
    }
}
