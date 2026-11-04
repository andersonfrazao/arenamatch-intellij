package br.com.arenamatch.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaginaPublicaTest {
    private final FiltroAutenticacao filtro = new FiltroAutenticacao();

    @Test void visitanteAcessaPaginaInstitucionalSemCriarSessao() throws Exception {
        for (String pagina : new String[]{"/arena/index.xhtml", "/arena/como-funciona.xhtml"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", pagina);
            request.setContextPath("/arena");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);
            filtro.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
            assertNull(request.getSession(false));
            assertNull(response.getRedirectedUrl());
        }
    }

    @Test void paginasInternasContinuamProtegidas() throws Exception {
        for (String pagina : new String[]{"/minha-agenda.xhtml", "/fale-conosco.xhtml", "/interno/index.xhtml", "/interno/como-funciona.xhtml", "/interno/robots.txt", "/interno/sitemap.xml"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", pagina);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);
            filtro.doFilter(request, response, chain);
            verifyNoInteractions(chain);
            assertEquals("/login.xhtml", response.getRedirectedUrl());
        }
    }

    @Test void recursosDeBuscaSaoPublicosSemCriarSessao() throws Exception {
        for (String contexto : new String[]{"", "/arena"}) {
            for (String recurso : new String[]{"/robots.txt", "/sitemap.xml"}) {
                MockHttpServletRequest request = new MockHttpServletRequest("GET", contexto + recurso);
                request.setContextPath(contexto);
                MockHttpServletResponse response = new MockHttpServletResponse();
                FilterChain chain = mock(FilterChain.class);
                filtro.doFilter(request, response, chain);
                verify(chain).doFilter(request, response);
                assertNull(request.getSession(false));
                assertNull(response.getRedirectedUrl());
            }
        }
    }

    @Test void recursosDeBuscaRetornamArquivosEmVezDeLogin() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(PublicWebConfiguration.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filtro).build();

            mvc.perform(get("/robots.txt"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                    .andExpect(content().string(containsString("User-agent: *")))
                    .andExpect(content().string(containsString("Sitemap: https://www.arenamatch.app.br/sitemap.xml")));

            mvc.perform(get("/sitemap.xml"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                    .andExpect(xpath("count(/s:urlset/s:url)", Map.of("s", "http://www.sitemaps.org/schemas/sitemap/0.9")).number(3.0))
                    .andExpect(content().string(containsString("https://www.arenamatch.app.br/index.xhtml")))
                    .andExpect(content().string(containsString("https://www.arenamatch.app.br/como-funciona.xhtml")))
                    .andExpect(content().string(containsString("https://www.arenamatch.app.br/privacidade.xhtml")));
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
        verificarEntrada(null, "/login.xhtml");
    }

    @Test void configuracaoDesabilitadaPreservaLogin() throws Exception {
        verificarEntrada(false, "/login.xhtml");
    }

    @Test void configuracaoHabilitadaDirecionaParaApresentacao() throws Exception {
        verificarEntrada(true, "/index.xhtml");
    }

    private void verificarEntrada(Boolean habilitada, String destino) throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            if (habilitada != null) {
                context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("teste",
                        Map.of("arenamatch.pagina-publica-inicial-habilitada", habilitada.toString())));
            }
            context.register(PublicWebConfiguration.class);
            context.refresh();
            MockMvcBuilders.webAppContextSetup(context).addFilters(filtro).build()
                    .perform(get("/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(destino));
        }
    }

    @Configuration @EnableWebMvc @Import(WebConfig.class)
    static class PublicWebConfiguration implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Reproduz os recursos estaticos do Spring Boot neste contexto MVC isolado.
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/META-INF/resources/");
        }
    }
}
