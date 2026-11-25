package br.com.arenamatch.config;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceHandlerWrapper;
import jakarta.faces.application.ResourceWrapper;
import java.util.Set;

/**
 * Acrescenta {@code v=<versão da implantação>} às URLs dos recursos do próprio app
 * (bibliotecas css, js e images). O Mojarra serve esses recursos com cache de 7 dias e
 * sem versão na URL; sem isto, depois de uma implantação o navegador continuaria com o
 * CSS antigo sobre o HTML novo. A versão muda a cada subida da aplicação.
 */
public class VersaoRecursoResourceHandler extends ResourceHandlerWrapper {

    static final String VERSAO = Long.toString(System.currentTimeMillis(), 36);
    private static final Set<String> BIBLIOTECAS_DO_APP = Set.of("css", "js", "images");

    public VersaoRecursoResourceHandler(ResourceHandler wrapped) {
        super(wrapped);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return versionar(super.createResource(resourceName, libraryName), libraryName);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        return versionar(super.createResource(resourceName, libraryName, contentType), libraryName);
    }

    static Resource versionar(Resource recurso, String biblioteca) {
        if (recurso == null || biblioteca == null || !BIBLIOTECAS_DO_APP.contains(biblioteca)) {
            return recurso;
        }
        return new ResourceWrapper(recurso) {
            @Override
            public String getRequestPath() {
                String caminho = super.getRequestPath();
                return caminho + (caminho.contains("?") ? "&" : "?") + "v=" + VERSAO;
            }
        };
    }
}
