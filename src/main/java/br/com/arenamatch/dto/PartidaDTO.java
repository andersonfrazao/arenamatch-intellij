package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDTO {

    private Long id;
    
    // Trocamos a Entidade Time pelo DTO
    private TimeResumoDTO mandante;
    private TimeResumoDTO visitante;
    
    private LocalDateTime dataHora;
    private StatusPartida status;
    private Integer golsMandante;
    private Integer golsVisitante;
    private StatusPlacar statusPlacar;

    // --- Controle de Cancelamento ---
    
    // Trocamos a Entidade Time pelo DTO
    private TimeResumoDTO solicitanteCancelamento;
    
    private String motivoCancelamento;
    private LocalDateTime dataSolicitacao;
}
