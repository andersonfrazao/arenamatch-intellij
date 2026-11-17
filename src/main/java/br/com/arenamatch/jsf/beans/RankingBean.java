package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.jsf.client.TimeClient;
import br.com.arenamatch.dto.JogoRealizadoDTO;
import br.com.arenamatch.dto.TimeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

@Named
@ViewScoped
public class RankingBean implements Serializable {
    @Getter @lombok.Setter private br.com.arenamatch.enums.Modalidade modalidade = br.com.arenamatch.enums.Modalidade.FUTEBOL_CAMPO;

    public br.com.arenamatch.enums.Modalidade[] getModalidades() { return br.com.arenamatch.enums.Modalidade.values(); }

    public void alterarModalidade() {
        erroCarga = false;
        scout = null; jogosRealizados = new ArrayList<>(); temMaisJogos = false;
        if (sessaoBean.isAcessoCompleto()) {
            carregarRanking();
            if (isScoutAtivo() && sessaoBean.isPossuiTime()) carregarScout();
        }
    }
    @Inject private TimeClient timeClient;
    @Inject private SessaoBean sessaoBean;
    @Getter private List<TimeDTO> ranking;
    @Getter private TimeDTO scout;
    @Getter private List<JogoRealizadoDTO> jogosRealizados = new ArrayList<>();
    @Getter private boolean temMaisJogos;
    private int paginaJogos;
    @Getter private String abaAtiva = "ranking";
    // Falha ao consultar o backend: a tela mostra um aviso com "Tentar novamente" em vez de quebrar.
    @Getter private boolean erroCarga;

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
        erroCarga = false;
        if (sessaoBean.isAcessoCompleto() && ranking.isEmpty()) {
            carregarRanking();
        }
    }

    public void exibirScout() {
        abaAtiva = "scout";
        erroCarga = false;
        if (sessaoBean.isAcessoCompleto() && sessaoBean.isPossuiTime() && scout == null) {
            carregarScout();
        }
    }

    public void tentarNovamente() {
        alterarModalidade();
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
        this.paginaJogos = 0;
        this.jogosRealizados = new ArrayList<>();
        this.temMaisJogos = false;
        try {
            this.scout = timeClient.buscarMeuScout(modalidade);
        } catch (Exception e) {
            this.scout = null;
            erroCarga = true;
            return;
        }
        carregarPaginaDeJogos();
    }

    public void carregarMaisJogos() {
        if (!temMaisJogos) {
            return;
        }

        erroCarga = false;
        paginaJogos++;
        if (!carregarPaginaDeJogos()) {
            paginaJogos--;
        }
    }

    private boolean carregarPaginaDeJogos() {
        try {
            var resultado = timeClient.buscarJogosRealizadosDoScout(paginaJogos, modalidade);
            this.jogosRealizados.addAll(resultado.getJogos());
            this.temMaisJogos = resultado.isTemMais();
            return true;
        } catch (Exception e) {
            erroCarga = true;
            return false;
        }
    }

    private void carregarRanking() {
        try {
            this.ranking = timeClient.buscarRankingGeral(modalidade);
        } catch (Exception e) {
            this.ranking = List.of();
            erroCarga = true;
        }
    }
}
