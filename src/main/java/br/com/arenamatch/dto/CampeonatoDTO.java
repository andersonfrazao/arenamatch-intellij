package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusCampeonato;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CampeonatoDTO extends CampeonatoRequestDTO {
    private Long id;
    private StatusCampeonato status;
    private Integer anoReferencia;
    private boolean tabelaPublicada;
}
