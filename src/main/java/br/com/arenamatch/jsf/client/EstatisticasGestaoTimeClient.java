package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.DetalheEstatisticaJogadorDTO;
import br.com.arenamatch.dto.PainelEstatisticasJogadoresDTO;
import br.com.arenamatch.dto.ResumoEstatisticasTimeDTO;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EstatisticasGestaoTimeClient {
    private final RestClient restClient;

    public EstatisticasGestaoTimeClient(RestClient restClient) { this.restClient = restClient; }

    public ResumoEstatisticasTimeDTO resumirTime(LocalDate inicio, LocalDate fim) {
        return restClient.get().uri(uri -> uri.path("/api/gestao-time/estatisticas/time")
                        .queryParam("inicio", inicio).queryParam("fim", fim).build())
                .retrieve().body(ResumoEstatisticasTimeDTO.class);
    }

    public List<Integer> listarAnos() {
        Integer[] anos = restClient.get().uri("/api/gestao-time/estatisticas/anos")
                .retrieve().body(Integer[].class);
        return anos == null ? List.of() : Arrays.asList(anos);
    }

    public PainelEstatisticasJogadoresDTO resumirJogadores(
            LocalDate inicio, LocalDate fim, String busca, String ordenacao) {
        return restClient.get().uri(uri -> uri.path("/api/gestao-time/estatisticas/jogadores")
                        .queryParam("inicio", inicio).queryParam("fim", fim)
                        .queryParam("busca", busca == null ? "" : busca)
                        .queryParam("ordenacao", ordenacao).build())
                .retrieve().body(PainelEstatisticasJogadoresDTO.class);
    }

    public DetalheEstatisticaJogadorDTO detalharJogador(
            Long atletaId, LocalDate inicio, LocalDate fim, int pagina) {
        return restClient.get().uri(uri -> uri.path("/api/gestao-time/estatisticas/jogadores/{id}")
                        .queryParam("inicio", inicio).queryParam("fim", fim)
                        .queryParam("pagina", pagina).build(atletaId))
                .retrieve().body(DetalheEstatisticaJogadorDTO.class);
    }
}
