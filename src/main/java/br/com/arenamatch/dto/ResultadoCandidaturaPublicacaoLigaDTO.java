package br.com.arenamatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCandidaturaPublicacaoLigaDTO {
    private String acao;
    private String mensagem;
    private Long idLiga;
}
