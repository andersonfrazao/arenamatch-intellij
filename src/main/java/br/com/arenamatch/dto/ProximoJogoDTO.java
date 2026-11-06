package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;
import br.com.arenamatch.enums.Modalidade;

@Data
public class ProximoJogoDTO {

    private Long idPartida;
    private Modalidade modalidade;
    private LocalDateTime dataHora;
    private String nomeAdversario;
    private String nomeTimeMandante;
    private String nomeTimeVisitante;
    private String escudoTimeMandante;
    private String escudoTimeVisitante;
    private String posicaoMeuTime;
    private String cidade;
    private String endereco;
}
