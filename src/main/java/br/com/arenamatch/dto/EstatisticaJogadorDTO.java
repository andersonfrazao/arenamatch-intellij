package br.com.arenamatch.dto;

import br.com.arenamatch.enums.SituacaoAtleta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticaJogadorDTO {
    private Long atletaId;
    private String nome;
    private String apelido;
    private SituacaoAtleta situacao;
    private long partidas;
    private long gols;
    private long cartoesAmarelos;
    private long cartoesVermelhos;
    private Long minutosJogados;
    private long partidasSemMinutos;

    public String getNomeExibicao() {
        return apelido == null || apelido.isBlank() ? nome : apelido;
    }
}
