package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;
import br.com.arenamatch.enums.Modalidade;

@Data
public class JogoRealizadoDTO {
    private Modalidade modalidade;

    private LocalDateTime dataHora;
    private String nomeMeuTime;
    private Integer golsMeuTime;
    private Integer golsAdversario;
    private String nomeAdversario;
}
