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

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Entrada direta do aplicativo WebView; apresentacao em /index.xhtml.
        registry.addViewController("/")
                .setViewName("redirect:/index.xhtml");
        
        // Define prioridade alta para garantir que esse redirecionamento aconteça antes de outros
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/escudos/**")
                .addResourceLocations("file:" + diretorioEscudos + "/");
    }
}
