package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.AcessoCampeonatoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CampeonatoAcessoClient {
    private final RestClient restClient;

    public CampeonatoAcessoClient(RestClient restClient) { this.restClient = restClient; }

    public AcessoCampeonatoDTO consultar() {
        return restClient.get().uri("/api/campeonatos/acesso")
                .retrieve().body(AcessoCampeonatoDTO.class);
    }
}
