package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.DetalheEstatisticaJogadorDTO;
import br.com.arenamatch.dto.HistoricoEstatisticaJogadorDTO;
import br.com.arenamatch.jsf.client.EstatisticasGestaoTimeClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
@Getter
@Setter
public class EstatisticaJogadorBean implements Serializable {
    @Inject private EstatisticasGestaoTimeClient client;
    @Inject private SessaoBean sessaoBean;

    private Long atleta;
    private LocalDate inicio;
    private LocalDate fim;
    private String modo = "ano";
    private Integer ano;
    private DetalheEstatisticaJogadorDTO detalhe;
    private final List<HistoricoEstatisticaJogadorDTO> historico = new ArrayList<>();
    private int pagina;
    private boolean temMais;
    private boolean inicializado;

    public void inicializar() {
        if (inicializado || atleta == null || !sessaoBean.isAssinantePro()) return;
        inicializado = true;
        if (inicio == null) inicio = LocalDate.now().withDayOfYear(1);
        if (fim == null) fim = LocalDate.now();
        carregar(false);
    }

    public void carregarMais() {
        if (!temMais) return;
        pagina++;
        carregar(true);
    }

    public String getNomeExibicao() {
        if (detalhe == null) return "Jogador";
        return detalhe.getApelido() == null || detalhe.getApelido().isBlank()
                ? detalhe.getNome() : detalhe.getApelido();
    }

    private void carregar(boolean acumular) {
        try {
            DetalheEstatisticaJogadorDTO resposta = client.detalharJogador(atleta, inicio, fim, pagina);
            if (!acumular) historico.clear();
            if (resposta != null && resposta.getHistorico() != null) historico.addAll(resposta.getHistorico());
            detalhe = resposta;
            temMais = resposta != null && resposta.isTemMais();
        } catch (Exception e) {
            if (pagina > 0) pagina--;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Não foi possível carregar o histórico do jogador.", null));
        }
    }
}
