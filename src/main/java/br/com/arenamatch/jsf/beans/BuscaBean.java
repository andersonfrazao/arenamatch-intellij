package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.BuscaClient;
import br.com.arenamatch.jsf.client.PartidaClient;
import br.com.arenamatch.service.BuscaTelaService;
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
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class BuscaBean implements Serializable {

    @Inject private BuscaClient buscaClient;
    @Inject private SessaoBean sessaoBean;
    @Inject private PartidaClient partidaClient;
    @Inject private AgendaClient agendaClient;
    @Inject private BuscaTelaService buscaTelaService;

    @Getter @Setter
    private FiltroBuscaDTO filtro = new FiltroBuscaDTO();

    @Getter
    private List<TimeResumoDTO> resultados;

    @Getter @Setter
    private TimeResumoDTO timeSelecionadoParaDesafio;

    @Getter @Setter
    private String mensagemDesafio;

    private Long idMeuTimeCache;

    @Getter @Setter
    private Categoria categoriaSelecionada;

    @PostConstruct
    public void init() {
        this.resultados = new ArrayList<>();
        aplicarLimiteRaioBasicoNaTela();
    }

    public void pesquisar() {
        aplicarLimiteRaioBasicoNaTela();

        try {
            buscaTelaService.validarFiltro(filtro);
        } catch (RuntimeException e) {
            this.resultados = new ArrayList<>();
            msgWarn(e.getMessage());
            return;
        }

        try {
            Long meuId = carregarIdMeuTime();
            if (meuId == null) {
                msgErro("Você precisa ter um time cadastrado para buscar adversários.");
                return;
            }

            carregarResultados(meuId);

            if (resultados.isEmpty()) {
                msgInfo("Nenhum time encontrado para esta data na sua região.");
            }
        } catch (RestClientResponseException e) {
            this.resultados = new ArrayList<>();
            msgWarn(e.getResponseBodyAsString());
        } catch (Exception e) {
            msgErro("Erro ao buscar os times");
        }
    }

    public void confirmarDesafio() {
        try {
            Long meuId = carregarIdMeuTime();
            if (meuId == null) {
                msgErro("Erro ao identificar seu time.");
                return;
            }

            DesafioDTO dto = new DesafioDTO();
            dto.setIdTimeDesafiante(meuId);
            dto.setIdTimeDesafiado(timeSelecionadoParaDesafio.getId());
            dto.setMensagem(mensagemDesafio);
            dto.setCategoria(timeSelecionadoParaDesafio.getCategoria());

            if (filtro.getDataJogo() != null) {
                dto.setDataHoraPartida(filtro.getDataJogo().atStartOfDay());
            } else {
                msgErro("A data da pesquisa foi perdida. Pesquise novamente.");
                return;
            }

            partidaClient.enviarDesafio(dto);
            msgInfo("Desafio enviado com sucesso para " + timeSelecionadoParaDesafio.getNome() + "!");
            carregarResultados(meuId);
            cancelarDesafio();
        } catch (RestClientResponseException e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getMessage());
        }
    }

    public boolean isPlanoBasico() {
        return buscaTelaService.isPlanoBasico(sessaoBean.getUsuarioLogado());
    }

    public Integer getRaioMaximoPlanoBasicoKm() {
        return buscaTelaService.getRaioMaximoPlanoBasicoKm();
    }

    public void informarBloqueioRaioBasico() {
        aplicarLimiteRaioBasicoNaTela();
        msgWarn("O ajuste de distancia esta bloqueado no plano BASICO. Para buscar acima de "
                + getRaioMaximoPlanoBasicoKm() + " km, mude para o plano PRO.");
    }

    public void prepararDesafio(TimeResumoDTO timeDesafiado) {
        this.timeSelecionadoParaDesafio = timeDesafiado;
        this.mensagemDesafio = "";
    }

    public void cancelarDesafio() {
        this.timeSelecionadoParaDesafio = null;
        this.mensagemDesafio = "";
    }

    public void cancelarConviteEnviado(Long idAdversario) {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            partidaClient.cancelarConvitePorAdversario(meuTimeId, idAdversario);
            msgInfo("Convite retirado.");
        } catch (Exception e) {
            msgErro("Erro ao cancelar convite.");
        }
    }

    public Categoria[] getCategoriasEnum() {
        return Categoria.values();
    }

    public Categoria[] getCategorias() {
        return Categoria.values();
    }

    public String getDataJogoParametro() {
        return filtro.getDataJogo() != null ? filtro.getDataJogo().toString() : null;
    }

    public String getDataParametro() {
        return getDataJogoParametro();
    }

    public void setDataParametro(String data) {
        if (data == null || data.isBlank()) {
            return;
        }
        try {
            filtro.setDataJogo(java.time.LocalDate.parse(data));
        } catch (java.time.format.DateTimeParseException e) {
            filtro.setDataJogo(null);
        }
    }

    private void carregarResultados(Long meuId) {
        this.resultados = buscaClient.filtrarTimes(filtro, meuId);
    }

    private void aplicarLimiteRaioBasicoNaTela() {
        buscaTelaService.aplicarLimiteRaioBasico(filtro, sessaoBean.getUsuarioLogado());
    }

    private Long carregarIdMeuTime() {
        if (this.idMeuTimeCache != null) {
            return this.idMeuTimeCache;
        }

        try {
            Long idUsuario = sessaoBean.getUsuarioLogado().getId();
            TimeResumoDTO meuTime = agendaClient.buscarMeuTime(idUsuario);
            if (meuTime != null) {
                this.idMeuTimeCache = meuTime.getId();
                return this.idMeuTimeCache;
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar time do usuário: " + e.getMessage());
        }
        return null;
    }

    private void msgWarn(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }
}
