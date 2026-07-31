package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.ResultadoCandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import br.com.arenamatch.jsf.client.LigaClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Named
@ViewScoped
public class MuralLigaBean implements Serializable {

    @Autowired
    private LigaClient ligaClient;

    @Autowired
    private SessaoBean sessaoBean;

    @Getter @Setter
    private Long ligaId;

    @Getter @Setter
    private Long ligaSelecionadaPublicacao;

    @Getter @Setter
    private Long ligaSelecionadaCandidatura;

    @Getter @Setter
    private LocalDate dataJogoPublicacao;

    @Getter @Setter
    private String horaInicioPublicacao;

    @Getter @Setter
    private String horaFimPublicacao;

    @Getter @Setter
    private TipoProcuraPublicacaoLiga tipoProcuraPublicacao = TipoProcuraPublicacaoLiga.AMBOS;

    @Getter @Setter
    private Categoria categoriaPublicacao;

    @Getter @Setter
    private String regiaoPublicacao;

    @Getter @Setter
    private String observacaoPublicacao;

    @Getter @Setter
    private List<PublicacaoLigaDTO> publicacoes = new ArrayList<>();

    @Getter @Setter
    private List<LigaDetalheDTO> minhasLigas = new ArrayList<>();

    @Getter @Setter
    private List<LigaExplorarDTO> ligasEmDestaque = new ArrayList<>();

    @Getter @Setter
    private List<JogoRecenteLigaDTO> jogosRecentes = new ArrayList<>();

    public void carregarMural() {
        if (!sessaoBean.isAcessoCompleto()) {
            limparDados();
            return;
        }

        try {
            Long meuTimeId = getMeuTimeId();
            minhasLigas = ligaClient.buscarLigasDoTime(meuTimeId);
            publicacoes = ligaClient.listarPublicacoesGlobais(meuTimeId);
            ligasEmDestaque = ligaClient.listarLigasEmAlta(meuTimeId);
            jogosRecentes = ligaClient.listarJogosRecentesDoMural(10);

            if (ligaSelecionadaPublicacao == null && ligaId != null && participaDaLiga(ligaId)) {
                ligaSelecionadaPublicacao = ligaId;
            }
        } catch (Exception e) {
            log.error("Erro ao carregar mural global de ligas", e);
            msgErro("Erro ao carregar o mural.");
        }
    }

    public void criarPublicacao() {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Mural esta disponivel para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            NovaPublicacaoLigaDTO dto = new NovaPublicacaoLigaDTO();
            dto.setIdLiga(ligaSelecionadaPublicacao);
            dto.setIdTimeAutor(getMeuTimeId());
            dto.setDataJogo(dataJogoPublicacao != null ? dataJogoPublicacao.atStartOfDay() : null);
            dto.setHoraInicio(horaInicioPublicacao);
            dto.setHoraFim(horaFimPublicacao);
            dto.setTipoProcura(tipoProcuraPublicacao);
            dto.setCategoria(categoriaPublicacao);
            dto.setRegiao(regiaoPublicacao);
            dto.setObservacao(observacaoPublicacao);

            ligaClient.criarPublicacaoGlobal(dto);
            msgInfo("Anuncio criado no mural.");
            limparFormulario();
            carregarMural();
        } catch (Exception e) {
            log.error("Erro ao criar publicacao global no mural", e);
            msgErro("Erro ao criar anuncio.");
        }
    }

    public void candidatar(PublicacaoLigaDTO publicacao) {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Mural esta disponivel para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            ResultadoCandidaturaPublicacaoLigaDTO resultado = ligaClient.candidatarPublicacao(
                    publicacao.getId(),
                    getMeuTimeId(),
                    ligaSelecionadaCandidatura);
            msgInfo(resultado != null ? resultado.getMensagem() : "Candidatura enviada.");
            ligaSelecionadaCandidatura = null;
            carregarMural();
        } catch (Exception e) {
            log.error("Erro ao candidatar na publicacao {}", publicacao != null ? publicacao.getId() : null, e);
            msgErro("Erro ao enviar candidatura.");
        }
    }

    public void cancelarPublicacao(PublicacaoLigaDTO publicacao) {
        try {
            ligaClient.cancelarPublicacao(publicacao.getId(), getMeuTimeId());
            msgInfo("Anuncio cancelado.");
            carregarMural();
        } catch (Exception e) {
            log.error("Erro ao cancelar publicacao {}", publicacao != null ? publicacao.getId() : null, e);
            msgErro("Erro ao cancelar publicacao.");
        }
    }

    public boolean podeCancelarPublicacao(PublicacaoLigaDTO publicacao) {
        return publicacao != null
                && publicacao.getTimeAutor() != null
                && publicacao.getTimeAutor().getId().equals(getMeuTimeId());
    }

    public boolean podeCandidatar(PublicacaoLigaDTO publicacao) {
        return publicacao != null
                && publicacao.getTimeAutor() != null
                && !publicacao.getTimeAutor().getId().equals(getMeuTimeId());
    }

    public List<TipoProcuraPublicacaoLiga> getTiposProcura() {
        return Arrays.asList(TipoProcuraPublicacaoLiga.values());
    }

    public List<Categoria> getCategorias() {
        return Arrays.asList(Categoria.values());
    }

    public String iniciaisLiga(LigaExplorarDTO liga) {
        if (liga == null || liga.getNome() == null || liga.getNome().trim().isEmpty()) {
            return "LG";
        }

        String[] partes = liga.getNome().trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String segunda = partes.length > 1 ? partes[1].substring(0, 1) : "";
        return (primeira + segunda).toUpperCase();
    }

    public String resumoMovimentacao(LigaExplorarDTO liga) {
        if (liga == null) {
            return "Sem movimentacao";
        }

        return liga.getQtdTimes() + " times · "
                + liga.getQtdPublicacoesAbertas() + " anuncios · "
                + liga.getQtdJogos() + " jogos";
    }

    private boolean participaDaLiga(Long idLiga) {
        return minhasLigas.stream().anyMatch(liga -> liga.getId().equals(idLiga));
    }

    private Long getMeuTimeId() {
        if (sessaoBean.getUsuarioLogado() == null || sessaoBean.getUsuarioLogado().getIdTime() == null) {
            throw new RuntimeException("Nao foi possivel identificar o time logado.");
        }
        return sessaoBean.getUsuarioLogado().getIdTime();
    }

    private void limparDados() {
        publicacoes = new ArrayList<>();
        minhasLigas = new ArrayList<>();
        ligasEmDestaque = new ArrayList<>();
        jogosRecentes = new ArrayList<>();
    }

    private void limparFormulario() {
        ligaSelecionadaPublicacao = null;
        dataJogoPublicacao = null;
        horaInicioPublicacao = null;
        horaFimPublicacao = null;
        tipoProcuraPublicacao = TipoProcuraPublicacaoLiga.AMBOS;
        categoriaPublicacao = null;
        regiaoPublicacao = null;
        observacaoPublicacao = null;
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}
