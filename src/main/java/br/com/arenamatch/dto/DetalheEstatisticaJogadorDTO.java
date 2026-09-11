package br.com.arenamatch.dto;

import br.com.arenamatch.enums.SituacaoAtleta;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetalheEstatisticaJogadorDTO {
    private Long atletaId;
    private String nome;
    private String apelido;
    private SituacaoAtleta situacao;
    private long partidas;
    private long gols;
    private long cartoesAmarelos;
    private long cartoesVermelhos;
    private List<HistoricoEstatisticaJogadorDTO> historico = new ArrayList<>();
    private int pagina;
    private boolean temMais;
}
