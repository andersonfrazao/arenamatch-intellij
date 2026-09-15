package br.com.arenamatch.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoEstatisticaJogadorDTO {
    private Long partidaId;
    private LocalDateTime dataHora;
    private String adversario;
    private Integer golsMeuTime;
    private Integer golsAdversario;
    private Integer numeroCamisa;
    private String posicao;
    private String papel;
    private long gols;
    private long cartoesAmarelos;
    private long cartoesVermelhos;
    private Integer minutosJogados;
}
