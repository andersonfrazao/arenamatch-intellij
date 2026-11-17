package br.com.arenamatch.config;

import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecursosVisuaisTest {

    @Test
    void fragmentoCompartilhaAsMesmasDependenciasVisuais() throws Exception {
        Document documento = lerPagina("WEB-INF/template/recursos-visuais.xhtml");

        assertEquals("1", consultar(documento,
                "count(//*[local-name()='link'][@href='https://unpkg.com/primeflex@3.3.1/primeflex.min.css'])"));
        assertEquals("1", consultar(documento,
                "count(//*[local-name()='link'][@href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'])"));
        assertEquals("1", consultar(documento,
                "count(//*[local-name()='link'][contains(@href,'css:base.css') and contains(@href,'rev=')])"));
    }

    @Test
    void paginasPrimeFacesIncluemABaseUmaVezEAtivamSeuEscopo() throws Exception {
        for (String pagina : List.of("WEB-INF/template/layout.xhtml", "login.xhtml", "ativar-conta.xhtml",
                "recuperar-senha.xhtml", "privacidade.xhtml")) {
            Document documento = lerPagina(pagina);

            assertEquals("1", consultar(documento,
                    "count(//*[local-name()='include'][@src='/WEB-INF/template/recursos-visuais.xhtml'])"), pagina);
            assertEquals("am-ui", consultar(documento, "string(//*[local-name()='body']/@styleClass)"), pagina);
            assertEquals("0", consultar(documento,
                    "count(//*[local-name()='link'][contains(@href,'primeflex@') or contains(@href,'font-awesome/')])"), pagina);
            assertEquals("width=device-width, initial-scale=1.0", consultar(documento,
                    "string(//*[local-name()='meta'][@name='viewport']/@content)"), pagina);
        }
    }

    @Test
    void paginasInstitucionaisCarregamATipografiaEPaletaCompartilhadas() throws Exception {
        for (String pagina : List.of("index.xhtml", "como-funciona.xhtml", "encontrar-times-futebol-amador.xhtml")) {
            Document documento = lerPagina(pagina);

            assertEquals("1", consultar(documento,
                    "count(//*[local-name()='link'][contains(@href,'css:base.css') and contains(@href,'rev=')])"), pagina);
        }
        assertTrue(new ClassPathResource("META-INF/resources/css/layout.css").exists());
    }

    private Document lerPagina(String pagina) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (var entrada = new ClassPathResource("META-INF/resources/" + pagina).getInputStream()) {
            return fabrica.newDocumentBuilder().parse(entrada);
        }
    }

    private String consultar(Document documento, String expressao) throws Exception {
        return XPathFactory.newInstance().newXPath().evaluate(expressao, documento);
    }
}
