package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JogoRecenteLigaDTO implements Serializable {
    private Long idPartida;
    private Long idLiga;
    private String nomeLiga;
    private TimeResumoDTO mandante;
    private TimeResumoDTO visitante;
    private LocalDateTime dataHora;
    private StatusPartida status;
    private Integer golsMandante;
    private Integer golsVisitante;
    private StatusPlacar statusPlacar;
}
