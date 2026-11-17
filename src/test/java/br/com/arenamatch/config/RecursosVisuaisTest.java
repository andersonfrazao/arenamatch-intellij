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
            assertTrue(consultar(documento, "string(//*[local-name()='body']/@styleClass)").contains("am-ui"), pagina);
            assertEquals("0", consultar(documento,
                    "count(//*[local-name()='link'][contains(@href,'primeflex@') or contains(@href,'font-awesome/')])"), pagina);
            String viewport = consultar(documento, "string(//*[local-name()='meta'][@name='viewport']/@content)");
            assertTrue(viewport.startsWith("width=device-width, initial-scale=1.0"), pagina);
            assertTrue(!viewport.contains("user-scalable") && !viewport.contains("maximum-scale"), pagina);
        }
    }

    @Test
    void atualizacaoDasNotificacoesPreservaOConteudoEmEdicao() throws Exception {
        Document documento = lerPagina("WEB-INF/template/layout.xhtml");

        assertEquals("0", consultar(documento,
                "count(//*[@id='painelNotificacoesGlobais']//*[local-name()='insert'][@name='conteudo'])"));
        assertEquals("1", consultar(documento,
                "count(//*[local-name()='main']//*[local-name()='insert'][@name='conteudo'])"));
        assertEquals("1", consultar(documento,
                "count(//*[local-name()='button'][@id='amBellBtn'][@type='button'][@aria-controls='amNotifPanel'])"));
        assertEquals("1", consultar(documento,
                "count(//*[local-name()='nav'][@id='amBottomNav'][@aria-label])"));
        assertEquals("0", consultar(documento,
                "count(//*[local-name()='form']//*[local-name()='form'])"));
        assertEquals("0", consultar(documento,
                "count(//*[local-name()='commandButton'][not(ancestor::*[local-name()='form'])])"));
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

    @Test
    void paginasDeAcessoCompartilhamRecursosEFormulariosAcessiveis() throws Exception {
        for (String pagina : List.of("login.xhtml", "ativar-conta.xhtml", "recuperar-senha.xhtml")) {
            Document documento = lerPagina(pagina);
            assertTrue(consultar(documento, "string(//*[local-name()='body']/@styleClass)").contains("am-access"), pagina);
            assertEquals("0", consultar(documento, "count(//*[local-name()='style'])"), pagina);
            assertEquals("1", consultar(documento,
                    "count(//*[local-name()='link'][contains(@href,'css:acesso.css')])"), pagina);
            assertEquals("1", consultar(documento,
                    "count(//*[local-name()='script'][contains(@src,'js:acesso.js')])"), pagina);
            assertEquals("1", consultar(documento, "count(//*[local-name()='main'])"), pagina);
            assertEquals("1", consultar(documento, "count(//*[local-name()='defaultCommand'])"), pagina);
            assertEquals("0", consultar(documento,
                    "count(//*[local-name()='inputText' or local-name()='password' or local-name()='inputMask'][not(@id)])"), pagina);
            assertEquals("0", consultar(documento,
                    "count(//*[local-name()='commandButton'][not(ancestor::*[local-name()='form'])])"), pagina);
            assertEquals("1", consultar(documento,
                    "count(//*[local-name()='inputMask'][@*[local-name()='inputmode']='numeric'][@*[local-name()='autocomplete']='one-time-code'])"), pagina);
        }
        Document ativacao = lerPagina("ativar-conta.xhtml");
        assertEquals("@this email", consultar(ativacao,
                "string(//*[local-name()='commandLink'][@id='reenviar']/@process)"));
    }

    @Test
    void cadastroMantemComponentesProcessamentoEAcoesDeHorarioAcessiveis() throws Exception {
        Document cadastro = lerPagina("cadastro.xhtml");
        Document layout = lerPagina("WEB-INF/template/layout.xhtml");
        assertEquals("1", consultar(layout,
                "count(//*[local-name()='head']/*[local-name()='insert'][@name='estilosPagina'])"));
        assertEquals("1", consultar(cadastro,
                "count(//*[local-name()='define'][@name='estilosPagina']/*[local-name()='link'][contains(@href,'css:cadastro.css')])"));
        assertEquals("0", consultar(cadastro, "count(//*[local-name()='style'] | //@style)"));
        assertEquals("3", consultar(cadastro, "count(//*[local-name()='ol'][@aria-label='Etapas do cadastro']/*[local-name()='li'])"));
        assertEquals("1", consultar(cadastro,
                "count(//*[local-name()='wizard'][@widgetVar='wiz'][@showStepStatus='false'])"));
        assertEquals("@this", consultar(cadastro,
                "string(//*[local-name()='commandButton'][@id='excluirHorario']/@process)"));
        assertEquals("Excluir", consultar(cadastro,
                "string(//*[local-name()='commandButton'][@id='excluirHorario']/@value)"));
        assertEquals("0", consultar(cadastro,
                "count(//*[local-name()='selectOneMenu'][@id='modalidadeHorario' or @id='categoriaHorario'][@required='true'])"));
        assertEquals("1", consultar(cadastro,
                "count(//*[local-name()='div'][@id='boxTextosTermo'][@tabindex='0'][@onscroll])"));
        assertEquals("0", consultar(cadastro,
                "count(//*[local-name()='form']//*[local-name()='form'])"));
    }

    @Test
    void buscaMantemContextoDoConviteEPaginacaoAcessivel() throws Exception {
        Document busca = lerPagina("busca-time.xhtml");
        assertEquals("0", consultar(busca, "count(//*[local-name()='style'] | //@style)"));
        assertEquals("1", consultar(busca,
                "count(//*[local-name()='define'][@name='estilosPagina']/*[local-name()='link'][contains(@href,'css:busca.css')])"));
        assertEquals("1", consultar(busca,
                "count(//*[local-name()='dataView'][@paginatorAlwaysVisible='false'][@pageLinks='1'])"));
        assertEquals("1", consultar(busca,
                "count(//*[local-name()='datePicker'][@autoDetectDisplay='false'][@touchUI='false'][@showOnFocus='false'][@appendTo='@(body)'])"));
        assertEquals("@this mensagemDesafio", consultar(busca,
                "string(//*[local-name()='commandButton'][@id='enviarConvite']/@process)"));
        assertEquals("#{buscaBean.cancelarConviteEnviado(time.idPartidaPendente)}", consultar(busca,
                "string(//*[local-name()='commandButton'][@id='cancelarConvite']/@action)"));
        assertEquals("1", consultar(busca,
                "count(//*[local-name()='outputLabel'][@for='mensagemDesafio'])"));
        assertEquals("1", consultar(busca,
                "count(//*[local-name()='inputText'][@id='txtRaio']/@*[local-name()='oninput'])"));
        Document antiga = lerPagina("busca.xhtml");
        assertTrue(consultar(antiga, "string(//*[local-name()='viewAction']/@action)")
                .contains("/busca-time?faces-redirect=true"));
        assertEquals("0", consultar(antiga, "count(//*[local-name()='form'])"));
    }

    @Test
    void agendaUsaCssEscopadoEProcessaSomenteOCartaoDoPlacar() throws Exception {
        Document agenda = lerPagina("minha-agenda.xhtml");
        assertEquals("0", consultar(agenda, "count(//*[local-name()='style'] | //@style | //@inputStyle)"));
        assertEquals("1", consultar(agenda,
                "count(//*[local-name()='define'][@name='estilosPagina']/*[local-name()='link'][contains(@href,'css:agenda.css')])"));
        assertEquals("1", consultar(agenda,
                "count(//*[local-name()='button'][@id='todosMeusJogosBtn'][@aria-controls='todosMeusJogosPanel'])"));
        assertEquals("1", consultar(agenda,
                "count(//*[local-name()='dialog'][@widgetVar='flyerDialog'][not(@width)][@draggable='false'])"));
        assertEquals("4", consultar(agenda,
                "count(//*[local-name()='inputNumber'][@inputStyleClass='placar-input'])"));
        assertEquals("2", consultar(agenda,
                "count(//*[local-name()='commandButton'][contains(@action,'salvarPlacarInline')][@process='@this @parent'])"));
        assertEquals("1", consultar(agenda,
                "count(//*[local-name()='remoteCommand'][@name='atualizarCalendarioAjax'])"));
        assertEquals("0", consultar(agenda,
                "count(//*[local-name()='messages'][@id='msgs']/*[local-name()='autoUpdate'])"));
    }

    private String consultar(Document documento, String expressao) throws Exception {
        return XPathFactory.newInstance().newXPath().evaluate(expressao, documento);
    }
}
