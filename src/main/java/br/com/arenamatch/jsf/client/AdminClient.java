package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.AdminParametroSistemaDTO;
import br.com.arenamatch.dto.AdminParametroSistemaEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioResumoDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdminClient {

    private final RestClient restClient;

    public AdminClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<AdminUsuarioResumoDTO> buscarUsuarios(String termo) {
        AdminUsuarioResumoDTO[] usuarios = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/usuarios")
                        .queryParam("termo", termo)
                        .build())
                .retrieve()
                .body(AdminUsuarioResumoDTO[].class);

        return usuarios != null ? Arrays.asList(usuarios) : new ArrayList<>();
    }

    public AdminUsuarioEdicaoDTO buscarUsuario(Long id) {
        return restClient.get()
                .uri("/api/admin/usuarios/{id}", id)
                .retrieve()
                .body(AdminUsuarioEdicaoDTO.class);
    }

    public AdminUsuarioEdicaoDTO atualizarUsuario(AdminUsuarioEdicaoDTO usuario) {
        return restClient.put()
                .uri("/api/admin/usuarios/{id}", usuario.getId())
                .body(usuario)
                .retrieve()
                .body(AdminUsuarioEdicaoDTO.class);
    }

    public List<AdminParametroSistemaDTO> listarParametros() {
        AdminParametroSistemaDTO[] parametros = restClient.get()
                .uri("/api/admin/parametros")
                .retrieve()
                .body(AdminParametroSistemaDTO[].class);

        return parametros != null ? Arrays.asList(parametros) : new ArrayList<>();
    }

    public AdminParametroSistemaDTO atualizarParametro(AdminParametroSistemaDTO parametro) {
        AdminParametroSistemaEdicaoDTO dto = new AdminParametroSistemaEdicaoDTO();
        dto.setChave(parametro.getChave());
        dto.setValor(parametro.getValor());

        return restClient.put()
                .uri("/api/admin/parametros/{chave}", parametro.getChave())
                .body(dto)
                .retrieve()
                .body(AdminParametroSistemaDTO.class);
    }
}
