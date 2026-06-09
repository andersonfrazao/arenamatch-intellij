package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AdminParametroSistemaDTO;
import br.com.arenamatch.dto.AdminUsuarioEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioResumoDTO;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.jsf.client.AdminClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    @Getter @Setter
    private String termoBusca;

    @Getter
    private List<AdminUsuarioResumoDTO> usuarios = new ArrayList<>();

    @Getter @Setter
    private AdminUsuarioEdicaoDTO usuarioSelecionado;

    @Getter
    private List<AdminParametroSistemaDTO> parametros = new ArrayList<>();

    @Inject
    private AdminClient adminClient;

    @Inject
    private SessaoBean sessaoBean;

    @PostConstruct
    public void init() {
        if (!sessaoBean.isAdmin()) {
            redirecionarParaAgenda();
            return;
        }

        carregarParametros();
    }

    public void buscarUsuarios() {
        if (!sessaoBean.isAdmin()) {
            redirecionarParaAgenda();
            return;
        }

        if (termoBusca == null || termoBusca.trim().length() < 3) {
            usuarios = new ArrayList<>();
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Busca", "Digite pelo menos 3 caracteres.");
            return;
        }

        try {
            usuarios = adminClient.buscarUsuarios(termoBusca.trim());
            usuarioSelecionado = null;
        } catch (Exception e) {
            adicionarMensagemErro(e, "Falha ao buscar usuarios.");
        }
    }

    public void selecionarUsuario(AdminUsuarioResumoDTO usuario) {
        try {
            usuarioSelecionado = adminClient.buscarUsuario(usuario.getId());
        } catch (Exception e) {
            adicionarMensagemErro(e, "Falha ao carregar usuario.");
        }
    }

    public void salvarUsuario() {
        try {
            usuarioSelecionado = adminClient.atualizarUsuario(usuarioSelecionado);
            if (termoBusca != null && termoBusca.trim().length() >= 3) {
                usuarios = adminClient.buscarUsuarios(termoBusca.trim());
            }
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuario atualizado.");
        } catch (Exception e) {
            adicionarMensagemErro(e, "Falha ao salvar usuario.");
        }
    }

    public void carregarParametros() {
        try {
            parametros = adminClient.listarParametros();
        } catch (Exception e) {
            parametros = new ArrayList<>();
            adicionarMensagemErro(e, "Falha ao carregar parametros.");
        }
    }

    public void salvarParametro(AdminParametroSistemaDTO parametro) {
        try {
            AdminParametroSistemaDTO atualizado = adminClient.atualizarParametro(parametro);
            parametro.setValor(atualizado.getValor());
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Parametro atualizado.");
        } catch (Exception e) {
            adicionarMensagemErro(e, "Falha ao salvar parametro.");
        }
    }

    public StatusUsuario[] getStatusUsuarioOptions() {
        return StatusUsuario.values();
    }

    public Perfil[] getPerfilOptions() {
        return Perfil.values();
    }

    public PlanoAssinatura[] getPlanoAssinaturaOptions() {
        return PlanoAssinatura.values();
    }

    public StatusPagamento[] getStatusPagamentoOptions() {
        return StatusPagamento.values();
    }

    private void adicionarMensagemErro(Exception e, String mensagemPadrao) {
        String mensagem = mensagemPadrao;
        if (e instanceof RestClientResponseException restException
                && restException.getResponseBodyAsString() != null
                && !restException.getResponseBodyAsString().trim().isEmpty()) {
            mensagem = restException.getResponseBodyAsString();
        }

        adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", mensagem);
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumo, detalhe));
    }

    private void redirecionarParaAgenda() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("minha-agenda.xhtml");
        } catch (IOException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Acesso negado.");
        }
    }
}
