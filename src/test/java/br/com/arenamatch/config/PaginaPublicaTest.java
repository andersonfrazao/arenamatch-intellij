package br.com.arenamatch.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaginaPublicaTest {
    private final FiltroAutenticacao filtro = new FiltroAutenticacao();

    @Test void visitanteAcessaPaginaInstitucionalSemCriarSessao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/arena/index.xhtml");
        request.setContextPath("/arena");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filtro.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        assertNull(request.getSession(false));
        assertNull(response.getRedirectedUrl());
    }

    @Test void paginasInternasContinuamProtegidas() throws Exception {
        for (String pagina : new String[]{"/minha-agenda.xhtml", "/fale-conosco.xhtml", "/interno/index.xhtml"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", pagina);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);
            filtro.doFilter(request, response, chain);
            verifyNoInteractions(chain);
            assertEquals("/login.xhtml", response.getRedirectedUrl());
        }
    }

    @Test void loginCadastroEPrivacidadeContinuamPublicos() throws Exception {
        for (String pagina : new String[]{"/login.xhtml", "/cadastro.xhtml", "/privacidade.xhtml"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", pagina);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);
            filtro.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
            assertNull(response.getRedirectedUrl());
        }
    }

    @Test void raizRedirecionaVisitanteParaLogin() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(PublicWebConfiguration.class);
            context.refresh();
            MockMvcBuilders.webAppContextSetup(context).addFilters(filtro).build()
                    .perform(get("/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login.xhtml"));
        }
    }

    @Configuration @EnableWebMvc @Import(WebConfig.class)
    static class PublicWebConfiguration {}
}
