package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Modalidade;
import java.util.List;
import lombok.Data;

@Data
public class OpcoesPartidaExternaDTO {
    private String nomeTime;
    private List<Modalidade> modalidades;
    private List<String> adversarios;
}
