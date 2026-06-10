package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.PaginaJogosRealizadosDTO;
import br.com.arenamatch.dto.TimeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class TimeClient {

    @Autowired
    private RestClient restClient;

    public List<TimeDTO> buscarRankingGeral() {
        TimeDTO[] times = restClient.get()
                .uri("/api/times/ranking")
                .retrieve()
                .body(TimeDTO[].class);
                
        return times != null ? Arrays.asList(times) : new ArrayList<>();
    }

    public TimeDTO buscarMeuScout() {
        return restClient.get()
                .uri("/api/times/scout")
                .retrieve()
                .body(TimeDTO.class);
    }

    public PaginaJogosRealizadosDTO buscarJogosRealizadosDoScout(int pagina) {
        PaginaJogosRealizadosDTO resultado = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/times/scout/jogos")
                        .queryParam("pagina", pagina)
                        .build())
                .retrieve()
                .body(PaginaJogosRealizadosDTO.class);

        return resultado != null
                ? resultado
                : new PaginaJogosRealizadosDTO(List.of(), false);
    }
}
