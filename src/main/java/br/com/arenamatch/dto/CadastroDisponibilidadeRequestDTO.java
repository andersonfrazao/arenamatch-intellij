package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import java.util.List;
import lombok.Data;
import br.com.arenamatch.enums.Modalidade;

@Data
public class CadastroDisponibilidadeRequestDTO {
    private CadastroDTO cadastro;
    private List<DisponibilidadeDTO> agenda;
    private Categoria categoria;
    private Modalidade modalidade;
    private String dia;
    private String inicio;
    private String fim;
}
