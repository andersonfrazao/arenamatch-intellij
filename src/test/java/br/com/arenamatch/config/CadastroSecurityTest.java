package br.com.arenamatch.config;

import br.com.arenamatch.controller.CadastroController;
import br.com.arenamatch.service.*;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CadastroSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach void configurar() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Configuracao.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
    }

    @AfterEach void fechar() { context.close(); }

    @Test void novoCadastroValidaTodasAsEtapasSemJwt() throws Exception {
        for (String rota : new String[]{"/validacoes/responsavel", "/validacoes/time", "/validacoes/finalizacao", "/disponibilidades"}) {
            mvc.perform(post("/api/cadastro" + rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/api/cadastro").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    @Test void consultasDeEnderecoEEnvioDeEscudoFuncionamSemJwt() throws Exception {
        mvc.perform(get("/api/cadastro/endereco/cep").param("cep", "01001000"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/cadastro/endereco/coordenadas").param("latitude", "-23.5").param("longitude", "-46.6"))
                .andExpect(status().isOk());
        mvc.perform(multipart("/api/cadastro/escudo")
                .file(new MockMultipartFile("arquivo", "escudo.png", "image/png", new byte[]{1,2,3})))
                .andExpect(status().isOk());
    }

    @Test void leituraEdicaoEDesativacaoContinuamProtegidas() throws Exception {
        mvc.perform(get("/api/cadastro/7")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/cadastro/7").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/cadastro/minha-conta/desativar")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/cadastro/validacoes/responsavel")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/cadastro/rota-nao-liberada")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/times/ranking")).andExpect(status().isUnauthorized());
    }

    @Configuration @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthenticationFilter.class, CadastroController.class})
    static class Configuracao {
        @Bean JwtService jwtService() { return mock(JwtService.class); }
        @Bean CadastroService cadastroService() { return mock(CadastroService.class); }
        @Bean CadastroFormularioService formularioService() { return mock(CadastroFormularioService.class); }
        @Bean CadastroEnderecoService enderecoService() { return mock(CadastroEnderecoService.class); }
        @Bean EscudoTimeService escudoService() { return mock(EscudoTimeService.class); }
    }
}
