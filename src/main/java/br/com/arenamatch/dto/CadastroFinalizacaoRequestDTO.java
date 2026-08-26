package br.com.arenamatch.dto;

import java.util.List;
import lombok.Data;

@Data
public class CadastroFinalizacaoRequestDTO {
    private CadastroDTO cadastro;
    private List<DisponibilidadeDTO> agenda;
    private boolean novoCadastro;
}
