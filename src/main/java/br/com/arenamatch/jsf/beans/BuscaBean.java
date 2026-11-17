package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.BuscaClient;
import br.com.arenamatch.jsf.client.PartidaClient;
import br.com.arenamatch.jsf.client.ParametroSistemaClient;
import br.com.arenamatch.enums.PlanoAssinatura;
import jakarta.annotation.PostConstruct;
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
import org.springframework.beans.BeanUtils;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class BuscaBean implements Serializable {

    @Inject private BuscaClient buscaClient;
    @Inject private SessaoBean sessaoBean;
    @Inject private PartidaClient partidaClient;
    @Inject private AgendaClient agendaClient;
    @Inject private ParametroSistemaClient parametroSistemaClient;
    private Integer raioMaximoPlanoBasicoKm;

    @Getter @Setter
    private FiltroBuscaDTO filtro = new FiltroBuscaDTO();

    @Getter
    private List<TimeResumoDTO> resultados;

    @Getter private boolean pesquisaRealizada;
    @Getter @Setter private int primeiroResultado;
    private FiltroBuscaDTO filtroResultados;
    @Getter private LocalDate dataDesafio;

    @Getter @Setter
    private TimeResumoDTO timeSelecionadoParaDesafio;

    @Getter @Setter
    private String mensagemDesafio;

    private Long idMeuTimeCache;
    @Getter private java.util.List<br.com.arenamatch.enums.Modalidade> modalidades = java.util.List.of();

    @Getter @Setter
    private Categoria categoriaSelecionada;

    @PostConstruct
    public void init() {
        this.resultados = new ArrayList<>();
        carregarIdMeuTime();
        if (modalidades.size() == 1) filtro.setModalidade(modalidades.get(0));
        aplicarLimiteRaioBasicoNaTela();
    }

    public void pesquisar() {
        aplicarLimiteRaioBasicoNaTela();
        cancelarDesafio();
        resultados = new ArrayList<>();
        pesquisaRealizada = false;
        primeiroResultado = 0;
        filtroResultados = null;

        if (filtro.getModalidade() == null) {
            resultados = new ArrayList<>();
            msgWarn("Selecione a modalidade para realizar a busca.");
            return;
        }
        if (filtro.getDataJogo() == null) {
            this.resultados = new ArrayList<>();
            msgWarn("Selecione uma data para realizar a busca.");
            return;
        }

        try {
            Long meuId = carregarIdMeuTime();
            if (meuId == null) {
                msgErro("Você precisa ter um time cadastrado para buscar adversários.");
                return;
            }

            FiltroBuscaDTO parametros = new FiltroBuscaDTO();
            BeanUtils.copyProperties(filtro, parametros);
            resultados = buscaClient.filtrarTimes(parametros, meuId);
            filtroResultados = parametros;
            pesquisaRealizada = true;

            if (resultados.isEmpty()) {
                msgInfo("Nenhum time encontrado para esta data na sua região.");
            }
        } catch (RestClientResponseException e) {
            this.resultados = new ArrayList<>();
            msgWarn(e.getResponseBodyAsString());
        } catch (Exception e) {
            this.resultados = new ArrayList<>();
            msgErro("Erro ao buscar os times");
        }
    }

    public void confirmarDesafio() {
        if (timeSelecionadoParaDesafio == null || dataDesafio == null) {
            msgWarn("Pesquise novamente e selecione o adversário para enviar o convite.");
            return;
        }
        Long meuId = carregarIdMeuTime();
        try {
            if (meuId == null) {
                msgErro("Erro ao identificar seu time.");
                return;
            }

            DesafioDTO dto = new DesafioDTO();
            dto.setIdTimeDesafiante(meuId);
            dto.setIdTimeDesafiado(timeSelecionadoParaDesafio.getId());
            dto.setMensagem(mensagemDesafio);
            dto.setCategoria(timeSelecionadoParaDesafio.getCategoria());
            dto.setModalidade(timeSelecionadoParaDesafio.getModalidade());

            dto.setDataHoraPartida(dataDesafio.atStartOfDay());

            partidaClient.enviarDesafio(dto);
            msgInfo("Desafio enviado com sucesso para " + timeSelecionadoParaDesafio.getNome() + "!");
        } catch (RestClientResponseException e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getResponseBodyAsString());
            return;
        } catch (Exception e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getMessage());
            return;
        }
        cancelarDesafio();
        atualizarResultadosAposConvite(meuId);
    }

    public boolean isPlanoBasico() {
        return sessaoBean.getUsuarioLogado() != null
                && sessaoBean.getUsuarioLogado().getPlanoAssinatura() == PlanoAssinatura.BASICO;
    }

    public Integer getRaioMaximoPlanoBasicoKm() {
        if (raioMaximoPlanoBasicoKm == null) {
            raioMaximoPlanoBasicoKm = parametroSistemaClient.buscarRaioMaximoBuscaBasico();
        }
        return raioMaximoPlanoBasicoKm;
    }

    public void informarBloqueioRaioBasico() {
        aplicarLimiteRaioBasicoNaTela();
        msgWarn("O ajuste de distancia esta bloqueado no plano BASICO. Para buscar acima de "
                + getRaioMaximoPlanoBasicoKm() + " km, mude para o plano PRO.");
    }

    public void prepararDesafio(TimeResumoDTO timeDesafiado) {
        this.timeSelecionadoParaDesafio = timeDesafiado;
        this.dataDesafio = getDataResultados();
        this.mensagemDesafio = "";
    }

    public void alterarModalidade() {
        resultados = new ArrayList<>();
        pesquisaRealizada = false;
        primeiroResultado = 0;
        filtroResultados = null;
        cancelarDesafio();
    }

    public void cancelarDesafio() {
        this.timeSelecionadoParaDesafio = null;
        this.dataDesafio = null;
        this.mensagemDesafio = "";
    }

    public void cancelarConviteEnviado(Long idPartida) {
        try {
            partidaClient.cancelarConvitePorId(idPartida);
            msgInfo("Convite retirado.");
        } catch (Exception e) {
            msgErro("Erro ao cancelar convite.");
            return;
        }
        atualizarResultadosAposConvite(carregarIdMeuTime());
    }

    public String getMensagemResultadosVazios() {
        return pesquisaRealizada
                ? "Nenhum adversário encontrado. Tente outra data ou ajuste os filtros."
                : "Escolha a data e a modalidade e clique em Buscar para encontrar adversários.";
    }

    public Categoria[] getCategoriasEnum() {
        return Categoria.values();
    }

    public Categoria[] getCategorias() {
        return Categoria.values();
    }

    public String getDataJogoParametro() {
        LocalDate data = getDataResultados();
        return data != null ? data.toString() : null;
    }

    public LocalDate getDataResultados() {
        return filtroResultados != null ? filtroResultados.getDataJogo() : null;
    }

    public String getDataParametro() {
        return filtro.getDataJogo() != null ? filtro.getDataJogo().toString() : null;
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

    private void atualizarResultadosAposConvite(Long meuId) {
        try {
            resultados = buscaClient.filtrarTimes(filtroResultados, meuId);
            if (primeiroResultado >= resultados.size()) primeiroResultado = 0;
        } catch (Exception e) {
            resultados = new ArrayList<>();
            pesquisaRealizada = false;
            msgWarn("A ação no convite foi concluída. Pesquise novamente para atualizar os adversários.");
        }
    }

    private void aplicarLimiteRaioBasicoNaTela() {
        if (isPlanoBasico()
                && (filtro.getRaioKm() == null || filtro.getRaioKm() > getRaioMaximoPlanoBasicoKm())) {
            filtro.setRaioKm(getRaioMaximoPlanoBasicoKm());
        }
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
                this.modalidades = meuTime.getModalidades();
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
