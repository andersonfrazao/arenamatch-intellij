package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.CampeonatoDTO;
import br.com.arenamatch.dto.CampeonatoRequestDTO;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CampeonatoClient {
    private final RestClient rest;
    public CampeonatoClient(RestClient rest) { this.rest = rest; }
    public List<CampeonatoDTO> listar() {
        CampeonatoDTO[] itens = rest.get().uri("/api/campeonatos/administracao").retrieve().body(CampeonatoDTO[].class);
        return itens == null ? List.of() : Arrays.asList(itens);
    }
    public CampeonatoDTO consultar(Long id) {
        return rest.get().uri("/api/campeonatos/administracao/{id}", id).retrieve().body(CampeonatoDTO.class);
    }
    public CampeonatoDTO salvar(Long id, CampeonatoRequestDTO request) {
        return id == null
                ? rest.post().uri("/api/campeonatos/administracao").body(request).retrieve().body(CampeonatoDTO.class)
                : rest.put().uri("/api/campeonatos/administracao/{id}", id).body(request).retrieve().body(CampeonatoDTO.class);
    }
    public void cancelar(CampeonatoDTO dto) {
        rest.patch().uri("/api/campeonatos/administracao/{id}/cancelar-rascunho?versao={versao}", dto.getId(), dto.getVersao())
                .retrieve().toBodilessEntity();
    }
}
