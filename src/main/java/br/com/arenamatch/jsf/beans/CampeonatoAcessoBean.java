package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AcessoCampeonatoDTO;
import br.com.arenamatch.jsf.client.CampeonatoAcessoClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class CampeonatoAcessoBean {
    @Inject private SessaoBean sessaoBean;
    @Inject private CampeonatoAcessoClient client;
    private boolean consultado;
    private boolean erro;
    private AcessoCampeonatoDTO acesso;

    private void consultar() {
        if (consultado) return;
        consultado = true;
        if (!sessaoBean.isLogado()) return;
        try {
            acesso = client.consultar();
            erro = acesso == null;
        } catch (Exception e) {
            erro = true;
        }
    }

    public boolean isPodeOrganizar() {
        consultar();
        return acesso != null && acesso.podeOrganizar();
    }

    public boolean isErro() { consultar(); return erro; }

    public String getMensagem() {
        consultar();
        if (!sessaoBean.isLogado()) return "Entre na sua conta para acessar campeonatos.";
        if (erro) return "Não foi possível verificar seu acesso. Atualize a página para tentar novamente.";
        return acesso.mensagem();
    }
}
