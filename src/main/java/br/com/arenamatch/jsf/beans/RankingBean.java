package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.jsf.client.TimeClient;
import br.com.arenamatch.dto.JogoRealizadoDTO;
import br.com.arenamatch.dto.TimeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Named
@ViewScoped
@Slf4j
public class RankingBean implements Serializable {
    @Getter @lombok.Setter private br.com.arenamatch.enums.Modalidade modalidade = br.com.arenamatch.enums.Modalidade.FUTEBOL_CAMPO;

    public br.com.arenamatch.enums.Modalidade[] getModalidades() { return br.com.arenamatch.enums.Modalidade.values(); }

    public void alterarModalidade() {
        ranking = List.of();
        scout = null;
        jogosRealizados = new ArrayList<>();
        temMaisJogos = false;
        falhaJogos = false;
        falhaScout = false;
        falhaRanking = false;
        if (sessaoBean.isAcessoCompleto()) {
            carregarRanking();
            if (isScoutAtivo() && sessaoBean.isPossuiTime()) carregarScout();
        }
    }
    @Inject private TimeClient timeClient;
    @Inject private SessaoBean sessaoBean;
    @Getter private List<TimeDTO> ranking = List.of();
    @Getter private TimeDTO scout;
    @Getter private List<JogoRealizadoDTO> jogosRealizados = new ArrayList<>();
    @Getter private boolean temMaisJogos;
    @Getter private boolean falhaRanking;
    @Getter private boolean falhaScout;
    @Getter private boolean falhaJogos;
    private int paginaJogos;
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
        try {
            scout = timeClient.buscarMeuScout(modalidade);
            falhaScout = false;
            paginaJogos = -1;
            jogosRealizados = new ArrayList<>();
            carregarPaginaDeJogos(0);
        } catch (Exception e) {
            falhaScout = true;
            log.error("Erro ao carregar scout da modalidade {}", modalidade, e);
            msgErro("Não foi possível carregar o desempenho do seu time. Tente novamente.");
        }
    }

    public void carregarMaisJogos() {
        if (!temMaisJogos && !falhaJogos) {
            return;
        }

        carregarPaginaDeJogos(paginaJogos + 1);
    }

    private void carregarPaginaDeJogos(int pagina) {
        try {
            var resultado = timeClient.buscarJogosRealizadosDoScout(pagina, modalidade);
            jogosRealizados.addAll(resultado.getJogos());
            paginaJogos = pagina;
            temMaisJogos = resultado.isTemMais();
            falhaJogos = false;
        } catch (Exception e) {
            falhaJogos = true;
            log.error("Erro ao carregar página {} do scout da modalidade {}", pagina, modalidade, e);
            msgErro("Não foi possível carregar os jogos. Tente novamente para continuar o histórico.");
        }
    }

    private void carregarRanking() {
        try {
            ranking = timeClient.buscarRankingGeral(modalidade);
            falhaRanking = false;
        } catch (Exception e) {
            ranking = List.of();
            falhaRanking = true;
            log.error("Erro ao carregar ranking da modalidade {}", modalidade, e);
            msgErro("Não foi possível carregar o ranking. Tente novamente.");
        }
    }

    private void msgErro(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Desempenho", texto));
    }
}
