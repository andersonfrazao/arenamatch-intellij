package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.PaginaHistoricoGestaoPartidaDTO;
import br.com.arenamatch.dto.ResumoHistoricoGestaoPartidaDTO;
import br.com.arenamatch.enums.StatusGestaoPartida;
import br.com.arenamatch.jsf.client.GestaoPartidaClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
@Getter
public class PartidasGestaoBean implements Serializable {

    @Inject private GestaoPartidaClient gestaoPartidaClient;
    @Inject private SessaoBean sessaoBean;

    private final List<ResumoHistoricoGestaoPartidaDTO> partidas = new ArrayList<>();
    private int pagina;
    private boolean temMais;

    @PostConstruct
    public void init() {
        if (sessaoBean.isAssinantePro()) carregarPagina();
    }

    public void carregarMais() {
        if (!temMais) return;
        pagina++;
        carregarPagina();
    }

    public String getRotuloStatus(ResumoHistoricoGestaoPartidaDTO partida) {
        StatusGestaoPartida status = partida.statusGestao();
        if (status == null) return "Não iniciada";
        return switch (status) {
            case RASCUNHO -> "Rascunho";
            case PENDENTE_CONCLUSAO -> "Pendente de conclusão";
            case PUBLICADO -> "Súmula finalizada";
        };
    }

    public String getClasseStatus(ResumoHistoricoGestaoPartidaDTO partida) {
        StatusGestaoPartida status = partida.statusGestao();
        if (status == null) return "not-started";
        return switch (status) {
            case RASCUNHO -> "draft";
            case PENDENTE_CONCLUSAO -> "pending";
            case PUBLICADO -> "published";
        };
    }

    private void carregarPagina() {
        try {
            PaginaHistoricoGestaoPartidaDTO resultado = gestaoPartidaClient.buscarHistorico(pagina);
            if (resultado == null) {
                temMais = false;
                return;
            }
            partidas.addAll(resultado.partidas() == null ? List.of() : resultado.partidas());
            temMais = resultado.temMais();
        } catch (Exception e) {
            if (pagina > 0) pagina--;
            mensagemErro(mensagem(e));
        }
    }

    private String mensagem(Exception e) {
        if (e instanceof RestClientResponseException re && re.getResponseBodyAsString() != null
                && !re.getResponseBodyAsString().isBlank()) return re.getResponseBodyAsString();
        return "Não foi possível carregar as partidas realizadas.";
    }

    private void mensagemErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null));
    }
}
