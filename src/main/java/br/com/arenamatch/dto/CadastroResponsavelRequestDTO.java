package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class CadastroResponsavelRequestDTO {
    private CadastroDTO cadastro;
    private String confirmarSenha;
    private boolean novoCadastro;
}
