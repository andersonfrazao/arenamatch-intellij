package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.AtletaRequestDTO;
import br.com.arenamatch.enums.SituacaoAtleta;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AtletaClient {
    private final RestClient restClient;
    public AtletaClient(RestClient restClient) { this.restClient = restClient; }
    public List<AtletaDTO> listar() {
        AtletaDTO[] itens = restClient.get().uri("/api/atletas").retrieve().body(AtletaDTO[].class);
        return itens == null ? List.of() : Arrays.asList(itens);
    }
    public AtletaDTO criar(String nome, String apelido) {
        return restClient.post().uri("/api/atletas").contentType(MediaType.APPLICATION_JSON)
                .body(new AtletaRequestDTO(nome, apelido)).retrieve().body(AtletaDTO.class);
    }
    public AtletaDTO atualizar(Long id, String nome, String apelido) {
        return restClient.put().uri("/api/atletas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .body(new AtletaRequestDTO(nome, apelido)).retrieve().body(AtletaDTO.class);
    }
    public void alterarSituacao(Long id, SituacaoAtleta situacao) {
        restClient.patch().uri("/api/atletas/{id}/situacao/{situacao}", id, situacao)
                .retrieve().toBodilessEntity();
    }
}
