package br.com.arenamatch.dto;

import br.com.arenamatch.enums.SituacaoAtleta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtletaDTO {
    private Long id;
    private String nome;
    private String apelido;
    private SituacaoAtleta situacao;
}
