package br.com.arenamatch.dto;

import java.util.List;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class AdversarioDetalhesDTO {
    private Long id;
    private String nome;
    private String escudo;
    private Categoria categoria;
    private String cidade;
    private String uf;
    private String regiao;
    private boolean mandoCampo;
    private Double distanciaKm;
    private Integer posicaoRanking;
    private Integer partidasJogadas;
    private Integer vitorias;
    private Integer empates;
    private Integer derrotas;
    private Integer golsPro;
    private Integer golsContra;
    private ConfrontoResumoDTO confronto = new ConfrontoResumoDTO(0L, 0L, 0L, 0L, 0L, 0L);
    private List<JogoRealizadoDTO> resultadosRecentes = List.of();

    public Integer getSaldoGols() {
        return (golsPro != null ? golsPro : 0) - (golsContra != null ? golsContra : 0);
    }
}
