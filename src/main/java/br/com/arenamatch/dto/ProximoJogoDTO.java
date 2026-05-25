package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProximoJogoDTO {

    private Long idPartida;
    private LocalDateTime dataHora;
    private String nomeAdversario;
    private String nomeTimeMandante;
    private String nomeTimeVisitante;
    private String posicaoMeuTime;
    private String cidade;
    private String endereco;
}
