package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.PaginaJogosRealizadosDTO;
import br.com.arenamatch.dto.AdversarioDetalhesDTO;
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

    public List<TimeDTO> buscarRankingGeral(br.com.arenamatch.enums.Modalidade modalidade) {
        TimeDTO[] times = restClient.get()
                .uri(b -> b.path("/api/times/ranking").queryParam("modalidade", modalidade).build())
                .retrieve()
                .body(TimeDTO[].class);
                
        return times != null ? Arrays.asList(times) : new ArrayList<>();
    }

    public TimeDTO buscarMeuScout(br.com.arenamatch.enums.Modalidade modalidade) {
        return restClient.get()
                .uri(b -> b.path("/api/times/scout").queryParam("modalidade", modalidade).build())
                .retrieve()
                .body(TimeDTO.class);
    }

    public PaginaJogosRealizadosDTO buscarJogosRealizadosDoScout(int pagina, br.com.arenamatch.enums.Modalidade modalidade) {
        PaginaJogosRealizadosDTO resultado = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/times/scout/jogos")
                        .queryParam("pagina", pagina)
                        .queryParam("modalidade", modalidade)
                        .build())
                .retrieve()
                .body(PaginaJogosRealizadosDTO.class);

        return resultado != null
                ? resultado
                : new PaginaJogosRealizadosDTO(List.of(), false);
    }

    public AdversarioDetalhesDTO buscarDetalhesAdversario(Long idAdversario, br.com.arenamatch.enums.Modalidade modalidade) {
        return restClient.get()
                .uri(b -> b.path("/api/times/{id}/adversario-detalhes").queryParam("modalidade", modalidade).build(idAdversario))
                .retrieve()
                .body(AdversarioDetalhesDTO.class);
    }
}
