package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JogoRealizadoDTO {

    private LocalDateTime dataHora;
    private String nomeMeuTime;
    private Integer golsMeuTime;
    private Integer golsAdversario;
    private String nomeAdversario;
}
