package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.DisponibilidadeGestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GestaoPartidaClient {
    private final RestClient restClient;
    public GestaoPartidaClient(RestClient restClient) { this.restClient = restClient; }
    public DisponibilidadeGestaoPartidaDTO disponibilidade(Long partidaId) {
        return restClient.get().uri("/api/gestao-partidas/{id}/disponibilidade", partidaId)
                .retrieve().body(DisponibilidadeGestaoPartidaDTO.class);
    }
    public GestaoPartidaDTO buscar(Long partidaId) {
        return restClient.get().uri("/api/gestao-partidas/{id}", partidaId).retrieve().body(GestaoPartidaDTO.class);
    }
    public GestaoPartidaDTO salvar(Long partidaId, GestaoPartidaRequestDTO dto) {
        return restClient.put().uri("/api/gestao-partidas/{id}/rascunho", partidaId)
                .contentType(MediaType.APPLICATION_JSON).body(dto).retrieve().body(GestaoPartidaDTO.class);
    }
    public GestaoPartidaDTO publicar(Long partidaId, GestaoPartidaRequestDTO dto) {
        return restClient.post().uri("/api/gestao-partidas/{id}/publicar", partidaId)
                .contentType(MediaType.APPLICATION_JSON).body(dto).retrieve().body(GestaoPartidaDTO.class);
    }
}
