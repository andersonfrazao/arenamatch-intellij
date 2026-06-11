package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConfrontoJogoDTO {
    private LocalDateTime dataHora;
    private String nomeMandante;
    private String escudoMandante;
    private Integer golsMandante;
    private Integer golsVisitante;
    private String nomeVisitante;
    private String escudoVisitante;
}
