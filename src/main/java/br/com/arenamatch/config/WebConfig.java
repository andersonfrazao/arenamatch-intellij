package br.com.arenamatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${arenamatch.uploads.escudos-dir:/app/uploads/escudos}")
    private String diretorioEscudos;

    @Value("${arenamatch.pagina-publica-inicial-habilitada:false}")
    private boolean paginaPublicaInicialHabilitada;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mantem o login para WebViews antigos ate a ativacao explicita da apresentacao.
        registry.addViewController("/")
                .setViewName(paginaPublicaInicialHabilitada ? "redirect:/index.xhtml" : "redirect:/login.xhtml");
        
        // Define prioridade alta para garantir que esse redirecionamento aconteça antes de outros
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/escudos/**")
                .addResourceLocations("file:" + diretorioEscudos + "/");
    }
}
