package br.com.arenamatch.jsf.beans;

import java.io.Serializable;

import br.com.arenamatch.jsf.client.EmailClient;
import br.com.arenamatch.dto.UsuarioDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class FaleConoscoBean implements Serializable {

    @Getter @Setter
    private String assunto;

    @Getter @Setter
    private String mensagem;

    @Getter @Setter
    private boolean exclusaoConfirmada;
    
    @Inject
    private SessaoBean sessaoBean; 
    
    @Inject
    private EmailClient emailClient;

    public void onAssuntoChange() {
        if (!isAssuntoExclusao()) {
            exclusaoConfirmada = false;
        }

        PrimeFaces.current().ajax().addCallbackParam("exibirConfirmacaoExclusao", isAguardandoConfirmacaoExclusao());
    }

    public void confirmarExclusao() {
        exclusaoConfirmada = true;
    }

    public void cancelarExclusao() {
        assunto = "";
        exclusaoConfirmada = false;
    }

    public boolean isAssuntoExclusao() {
        return "EXCLUSÃO".equals(assunto) || "EXCLUSÃƒO".equals(assunto);
    }

    public boolean isAguardandoConfirmacaoExclusao() {
        return isAssuntoExclusao() && !exclusaoConfirmada;
    }

    public void enviar() {
        try {
            if (isAguardandoConfirmacaoExclusao()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Confirmacao necessaria", "Confirme a orientacao sobre exclusao definitiva para continuar."));
                return;
            }

            UsuarioDTO usuario = sessaoBean.getUsuarioLogado();

            String textoEmail = "NOVA MENSAGEM DO ARENA MATCH\n"
                              + "----------------------------------------\n"
                              + "Remetente: " + usuario.getNome() + "\n"
                              + "E-mail de contato: " + usuario.getEmail() + "\n"
                              + "ID do Time: " + usuario.getIdTime() + "\n"
                              + "Assunto: " + assunto + "\n"
                              + "----------------------------------------\n\n"
                              + "Mensagem:\n" + mensagem;

             emailClient.enviarEmailSuporte("arenamatch.app@gmail.com", "[Arena Match] " + assunto, textoEmail, usuario.getEmail());

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Mensagem enviada com sucesso! Nossa equipe avaliara em breve."));
            
            this.assunto = "";
            this.mensagem = "";
            this.exclusaoConfirmada = false;
            
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao enviar a mensagem. Tente novamente."));
        }
    }
}
