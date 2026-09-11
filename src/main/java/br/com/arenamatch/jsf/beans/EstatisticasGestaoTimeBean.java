package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.PainelEstatisticasJogadoresDTO;
import br.com.arenamatch.dto.ResumoEstatisticasTimeDTO;
import br.com.arenamatch.jsf.client.EstatisticasGestaoTimeClient;
import br.com.arenamatch.service.EstatisticasGestaoTimeService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.text.DecimalFormat;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
@Getter
@Setter
public class EstatisticasGestaoTimeBean implements Serializable {

    @Inject private EstatisticasGestaoTimeClient client;
    @Inject private SessaoBean sessaoBean;

    private String visao = "time";
    private String periodo = "ano";
    private LocalDate inicio = LocalDate.now().withDayOfYear(1);
    private LocalDate fim = LocalDate.now();
    private String busca = "";
    private String ordenacao = "nome";
    private ResumoEstatisticasTimeDTO resumoTime;
    private PainelEstatisticasJogadoresDTO painelJogadores;
    private boolean inicializado;

    public void inicializar() {
        if (inicializado || !sessaoBean.isAssinantePro()) return;
        inicializado = true;
        aplicarPeriodoSemConsultar(periodo);
        consultar();
    }

    public void exibirTime() {
        visao = "time";
        consultar();
    }

    public void exibirJogadores() {
        visao = "jogadores";
        consultar();
    }

    public void aplicarPeriodo(String novoPeriodo) {
        periodo = novoPeriodo;
        aplicarPeriodoSemConsultar(novoPeriodo);
        consultar();
    }

    public void consultar() {
        if (!sessaoBean.isAssinantePro()) return;
        try {
            if ("jogadores".equals(visao)) {
                painelJogadores = client.resumirJogadores(inicio, fim, busca, ordenacao);
            } else {
                resumoTime = client.resumirTime(inicio, fim);
            }
        } catch (Exception e) {
            mensagemErro(mensagem(e));
        }
    }

    public String getPeriodoLabel() {
        return switch (periodo == null ? "" : periodo) {
            case "30" -> "Últimos 30 dias";
            case "90" -> "Últimos 90 dias";
            case "todos" -> "Todo o histórico";
            case "personalizado" -> "Período personalizado";
            default -> "Ano atual";
        };
    }

    public List<CardTime> getCardsTime() {
        if (resumoTime == null) return List.of();
        return List.of(
                new CardTime("fa-futbol", Long.toString(resumoTime.getJogos()), "Jogos", true),
                new CardTime("fa-percent", decimal(resumoTime.getAproveitamento()) + "%", "Aproveitamento", true),
                new CardTime("fa-trophy", Long.toString(resumoTime.getVitorias()), "Vitórias", false),
                new CardTime("fa-equals", Long.toString(resumoTime.getEmpates()), "Empates", false),
                new CardTime("fa-times", Long.toString(resumoTime.getDerrotas()), "Derrotas", false),
                new CardTime("fa-star", Long.toString(resumoTime.getPontos()), "Pontos", false),
                new CardTime("fa-arrow-up", Long.toString(resumoTime.getGolsPro()),
                        "Gols pró · média " + decimal(resumoTime.getMediaGolsPro()), false),
                new CardTime("fa-arrow-down", Long.toString(resumoTime.getGolsContra()),
                        "Gols contra · média " + decimal(resumoTime.getMediaGolsContra()), false),
                new CardTime("fa-balance-scale", Long.toString(resumoTime.getSaldoGols()), "Saldo de gols", false));
    }

    private String decimal(double valor) {
        return new DecimalFormat("0.##").format(valor);
    }

    private void aplicarPeriodoSemConsultar(String valor) {
        LocalDate hoje = LocalDate.now();
        switch (valor == null ? "ano" : valor) {
            case "30" -> { inicio = hoje.minusDays(29); fim = hoje; }
            case "90" -> { inicio = hoje.minusDays(89); fim = hoje; }
            case "todos" -> {
                inicio = EstatisticasGestaoTimeService.inicioTodoHistorico();
                fim = EstatisticasGestaoTimeService.fimTodoHistorico();
            }
            case "personalizado" -> { /* datas informadas pelo usuário */ }
            default -> { periodo = "ano"; inicio = hoje.withDayOfYear(1); fim = hoje; }
        }
    }

    private String mensagem(Exception e) {
        if (e instanceof RestClientResponseException re && re.getResponseBodyAsString() != null
                && !re.getResponseBodyAsString().isBlank()) return re.getResponseBodyAsString();
        return "Não foi possível carregar as estatísticas.";
    }

    private void mensagemErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null));
    }

    @Getter
    public static class CardTime {
        private final String icone;
        private final String valor;
        private final String rotulo;
        private final boolean destaque;

        public CardTime(String icone, String valor, String rotulo, boolean destaque) {
            this.icone = icone;
            this.valor = valor;
            this.rotulo = rotulo;
            this.destaque = destaque;
        }
    }
}
