package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class RankingLigaDTO {
    private Long idTime;
    private String nomeTime;
    private Integer pontos = 0;
    private Integer jogos = 0;
    private Integer vitorias = 0;
    private Integer empates = 0;
    private Integer derrotas = 0;
    private Integer golsPro = 0;
    private Integer golsContra = 0;
    private Integer saldoGols = 0;
    private Double aproveitamento = 0.0;
}
