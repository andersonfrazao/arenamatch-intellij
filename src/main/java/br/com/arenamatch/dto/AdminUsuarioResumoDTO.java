package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.enums.StatusUsuario;
import lombok.Data;

@Data
public class AdminUsuarioResumoDTO {
    private Long id;
    private String nome;
    private String email;
    private Perfil perfil;
    private StatusUsuario statusUsuario;
    private PlanoAssinatura planoAssinatura;
    private StatusAssinatura statusAssinatura;
    private StatusPagamento statusPagamento;
    private String dataExpiracao;
}
