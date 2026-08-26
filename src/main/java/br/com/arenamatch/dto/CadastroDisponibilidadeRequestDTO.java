package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import java.util.List;
import lombok.Data;

@Data
public class CadastroDisponibilidadeRequestDTO {
    private CadastroDTO cadastro;
    private List<DisponibilidadeDTO> agenda;
    private Categoria categoria;
    private String dia;
    private String inicio;
    private String fim;
}
