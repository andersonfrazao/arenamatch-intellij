package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.PainelEstatisticasJogadoresDTO;
import br.com.arenamatch.dto.ResumoEstatisticasTimeDTO;
import br.com.arenamatch.jsf.client.EstatisticasGestaoTimeClient;
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
    private String modo = "ano";
    private boolean periodoPersonalizadoAberto;
    private Integer ano = LocalDate.now().getYear();
    private List<Integer> anosDisponiveis = List.of(LocalDate.now().getYear());
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
        try {
            anosDisponiveis = client.listarAnos();
        } catch (Exception e) {
            anosDisponiveis = List.of(LocalDate.now().getYear());
        }
        if (anosDisponiveis == null || anosDisponiveis.isEmpty()) {
            anosDisponiveis = List.of(LocalDate.now().getYear());
        }
        if (ano == null || !anosDisponiveis.contains(ano)) ano = LocalDate.now().getYear();
        if (!"personalizado".equals(modo) || inicio == null || fim == null) aplicarAnoSemConsultar();
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

    public void selecionarAno() {
        modo = "ano";
        periodoPersonalizadoAberto = false;
        aplicarAnoSemConsultar();
        consultar();
    }

    public void alternarPeriodoPersonalizado() {
        periodoPersonalizadoAberto = !periodoPersonalizadoAberto;
        if (periodoPersonalizadoAberto && (inicio == null || fim == null)) aplicarAnoSemConsultar();
    }

    public void aplicarPeriodoPersonalizado() {
        modo = "personalizado";
        periodoPersonalizadoAberto = false;
        consultar();
    }

    public void cancelarPeriodoPersonalizado() {
        modo = "ano";
        periodoPersonalizadoAberto = false;
        aplicarAnoSemConsultar();
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

    public List<CardTime> getCardsTime() {
        if (resumoTime == null) return List.of();
        return List.of(
                new CardTime("fa-futbol", Long.toString(resumoTime.getJogos()), "Jogos", true),
                new CardTime("fa-percent", decimal(resumoTime.getAproveitamento()) + "%", "Aproveitamento", true),
                new CardTime("fa-trophy", Long.toString(resumoTime.getVitorias()), "Vitórias", false),
                new CardTime("fa-equals", Long.toString(resumoTime.getEmpates()), "Empates", false),
                new CardTime("fa-times", Long.toString(resumoTime.getDerrotas()), "Derrotas", false),
                new CardTime("fa-arrow-up", Long.toString(resumoTime.getGolsPro()),
                        "Gols pró · média " + decimal(resumoTime.getMediaGolsPro()), false),
                new CardTime("fa-arrow-down", Long.toString(resumoTime.getGolsContra()),
                        "Gols contra · média " + decimal(resumoTime.getMediaGolsContra()), false),
                new CardTime("fa-balance-scale", Long.toString(resumoTime.getSaldoGols()), "Saldo de gols", false));
    }

    private String decimal(double valor) {
        return new DecimalFormat("0.##").format(valor);
    }

    private void aplicarAnoSemConsultar() {
        int anoSelecionado = ano == null ? LocalDate.now().getYear() : ano;
        inicio = LocalDate.of(anoSelecionado, 1, 1);
        fim = LocalDate.of(anoSelecionado, 12, 31);
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
