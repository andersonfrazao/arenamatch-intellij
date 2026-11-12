package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.OpcoesPartidaExternaDTO;
import br.com.arenamatch.dto.RegistroPartidaExternaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PartidaExternaClient {
    private final RestClient restClient;

    public PartidaExternaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OpcoesPartidaExternaDTO buscarOpcoes() {
        return restClient.get().uri("/api/partidas-externas/opcoes").retrieve().body(OpcoesPartidaExternaDTO.class);
    }

    public EventoAgendaDTO registrar(RegistroPartidaExternaDTO dto) {
        return restClient.post().uri("/api/partidas-externas").body(dto).retrieve().body(EventoAgendaDTO.class);
    }
}
