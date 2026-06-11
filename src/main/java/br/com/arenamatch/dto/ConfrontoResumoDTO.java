package br.com.arenamatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfrontoResumoDTO {
    private Long jogos;
    private Long vitorias;
    private Long empates;
    private Long derrotas;
    private Long golsMeuTime;
    private Long golsAdversario;
}
