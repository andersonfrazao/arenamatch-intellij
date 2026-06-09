package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.util.List;

import br.com.arenamatch.jsf.client.TimeClient;
import br.com.arenamatch.dto.TimeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

@Named
@ViewScoped
public class RankingBean implements Serializable {
    @Inject private TimeClient timeClient;
    @Inject private SessaoBean sessaoBean;
    @Getter private List<TimeDTO> ranking;
    @Getter private TimeDTO scout;
    @Getter private String abaAtiva = "ranking";

    @PostConstruct
    public void init() {
        if (sessaoBean.isAcessoCompleto()) {
            carregarRanking();
        } else {
            this.ranking = List.of();
        }
    }

    public void exibirRanking() {
        abaAtiva = "ranking";
        if (sessaoBean.isAcessoCompleto() && ranking.isEmpty()) {
            carregarRanking();
        }
    }

    public void exibirScout() {
        abaAtiva = "scout";
        if (sessaoBean.isAcessoCompleto() && sessaoBean.isPossuiTime() && scout == null) {
            carregarScout();
        }
    }

    public boolean isRankingAtivo() {
        return "ranking".equals(abaAtiva);
    }

    public boolean isScoutAtivo() {
        return "scout".equals(abaAtiva);
    }

    public List<TimeDTO> getScoutTabela() {
        return scout == null ? List.of() : List.of(scout);
    }

    private void carregarScout() {
        this.scout = timeClient.buscarMeuScout();
    }

    private void carregarRanking() {
        this.ranking = timeClient.buscarRankingGeral();
    }
}
