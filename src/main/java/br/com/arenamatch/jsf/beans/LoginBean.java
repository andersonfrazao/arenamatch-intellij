package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.AuthClient;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class LoginBean implements Serializable {

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String senha;

    @Getter @Setter
    private String codigoAdmin;

    @Getter
    private String desafioAdmin;

    @Getter
    private String emailAdminMascarado;

    @Inject
    private AuthClient authClient; // Injeção do Feign Client

    @Inject
    private SessaoBean sessaoBean;

    public String logar() {
        String emailNormalizado = email != null ? email.trim() : null;
        email = emailNormalizado;

        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(emailNormalizado);
            loginDTO.setSenha(senha);

            // Chama a API via Client
            LoginResponseDTO loginResponse = authClient.login(loginDTO);

            if (loginResponse != null && loginResponse.isRequerCodigoAdmin()) {
                desafioAdmin = loginResponse.getDesafioAdmin();
                emailAdminMascarado = loginResponse.getEmailMascarado();
                senha = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Codigo enviado",
                                "Informe o codigo enviado para " + emailAdminMascarado + "."));
                return null;
            }

            if (loginResponse != null && loginResponse.getUsuario() != null) {
                sessaoBean.setUsuarioLogado(loginResponse.getUsuario());
                sessaoBean.setTokenJwt(loginResponse.getToken());
                return "/minha-agenda?faces-redirect=true"; // Navegação
            }
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Usuario ou senha invalidos.";
            }
            if (mensagem.toLowerCase().contains("pendente de ativacao")) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().put("emailAtivacao", emailNormalizado);
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Ativacao pendente", mensagem));
                return "/ativar-conta.xhtml?faces-redirect=true";
            }
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário ou senha inválidos."));
        }
        return null; // Fica na mesma tela
    }

    public String confirmarCodigoAdmin() {
        try {
            LoginResponseDTO response = authClient.confirmarAcessoAdmin(desafioAdmin, codigoAdmin);
            if (response == null || response.getUsuario() == null || response.getToken() == null) {
                adicionarErro("Nao foi possivel concluir o acesso.");
                return null;
            }

            sessaoBean.setUsuarioLogado(response.getUsuario());
            sessaoBean.setTokenJwt(response.getToken());
            limparDesafioAdmin();
            return "/admin.xhtml?faces-redirect=true";
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            adicionarErro(mensagem == null || mensagem.isBlank()
                    ? "Codigo invalido ou expirado."
                    : mensagem);
            return null;
        } catch (Exception e) {
            adicionarErro("Nao foi possivel confirmar o codigo.");
            return null;
        }
    }

    public void reenviarCodigoAdmin() {
        try {
            authClient.reenviarCodigoAcessoAdmin(desafioAdmin);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Codigo reenviado",
                            "Enviamos um novo codigo para " + emailAdminMascarado + "."));
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            adicionarErro(mensagem == null || mensagem.isBlank()
                    ? "Nao foi possivel reenviar o codigo."
                    : mensagem);
        }
    }

    public void voltarParaLogin() {
        limparDesafioAdmin();
    }

    public boolean isAguardandoCodigoAdmin() {
        return desafioAdmin != null && !desafioAdmin.isBlank();
    }

    private void limparDesafioAdmin() {
        desafioAdmin = null;
        emailAdminMascarado = null;
        codigoAdmin = null;
    }

    private void adicionarErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
    }
}
