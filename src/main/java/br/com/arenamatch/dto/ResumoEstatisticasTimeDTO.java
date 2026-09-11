package br.com.arenamatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResumoEstatisticasTimeDTO {
    private long jogos;
    private long vitorias;
    private long empates;
    private long derrotas;
    private long pontos;
    private long golsPro;
    private long golsContra;
    private long saldoGols;
    private double aproveitamento;
    private double mediaGolsPro;
    private double mediaGolsContra;
}
