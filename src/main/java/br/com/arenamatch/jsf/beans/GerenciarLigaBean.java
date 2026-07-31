package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.jsf.client.LigaClient;
import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.dto.ScoutLigaDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Named
@ViewScoped
public class GerenciarLigaBean implements Serializable {

    @Autowired
    private LigaClient ligaClient;

    @Autowired
    private SessaoBean sessaoBean;

    @Getter @Setter
    private Long ligaId;

    @Getter @Setter
    private LigaDetalheDTO ligaAtual;

    @Getter @Setter
    private String termoBusca;
    
    @Getter @Setter
    private TimeSimplesDTO timeSelecionadoParaConvite;
    
    @Getter @Setter
    private String mensagemConvite;
    
    @Getter @Setter
    private List<TimeSimplesDTO> resultadosBusca = new ArrayList<>();

    @Getter @Setter
    private List<PartidaDTO> jogosDaLiga = new ArrayList<>();

    @Getter @Setter
    private List<PublicacaoLigaDTO> publicacoesDaLiga = new ArrayList<>();

    @Getter @Setter
    private List<RankingLigaDTO> rankingDaLiga = new ArrayList<>();

    @Getter @Setter
    private ScoutLigaDTO scoutDaLiga;

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
    private List<BanimentoLigaDTO> banimentosAtivos = new ArrayList<>();

    @Getter @Setter
    private TimeSimplesDTO membroSelecionadoParaBanimento;

    @Getter @Setter
    private String motivoBanimento;

    // --- NOVA LISTA PARA SEGURAR OS IDS ---
    @Getter @Setter
    private List<Long> idsTimesComConvite = new ArrayList<>();

    public void carregarLiga() {
        if (!sessaoBean.isAcessoCompleto()) {
            ligaAtual = null;
            jogosDaLiga = new ArrayList<>();
            publicacoesDaLiga = new ArrayList<>();
            rankingDaLiga = new ArrayList<>();
            banimentosAtivos = new ArrayList<>();
            scoutDaLiga = null;
            return;
        }

        if (ligaId != null) {
            try {
                ligaAtual = ligaClient.buscarLigaPorId(ligaId);
                jogosDaLiga = ligaClient.listarJogosDaLiga(ligaId);
                publicacoesDaLiga = ligaClient.listarPublicacoesDaLiga(ligaId);
                rankingDaLiga = ligaClient.buscarRankingDaLiga(ligaId);
                banimentosAtivos = ligaClient.listarBanimentosAtivos(ligaId);
                carregarScoutDaLiga();
                log.info("Liga [{}] carregada com sucesso na tela de gestão.", ligaAtual.getNome());
            } catch (Exception e) {
                log.error("Erro fatal ao carregar a liga com ID: {}", ligaId, e);
                msgErro("Erro ao carregar os dados da liga.");
            }
        } else {
            log.warn("Tentativa de carregar a tela de gerenciar liga sem informar o ligaId na URL.");
        }
    }

    public void buscarTimesParaConvidar() {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        if (termoBusca == null || termoBusca.trim().length() < 3) {
            msgErro("Digite pelo menos 3 letras para buscar.");
            return;
        }
        try {
            log.info("Buscando times com o termo: '{}'", termoBusca);
            this.resultadosBusca = ligaClient.buscarTimesPorNome(termoBusca);
            
            // --- POPULA A LISTA DE IDS DE QUEM JÁ FOI CONVIDADO ---
            if (this.ligaAtual != null && this.ligaAtual.getId() != null) {
                this.idsTimesComConvite = ligaClient.buscarIdsTimesComConvitePendente(this.ligaAtual.getId());
            } else {
                this.idsTimesComConvite = new ArrayList<>();
            }

            if (this.resultadosBusca.isEmpty()) {
                msgInfo("Nenhum time encontrado com o nome: " + termoBusca);
            } else {
                msgInfo("Encontramos " + this.resultadosBusca.size() + " time(s)!");
            }
        } catch (Exception e) {
            log.error("Erro ao buscar times para convite com o termo '{}'", termoBusca, e);
            msgErro("Erro ao buscar times.");
        }
    }

    // --- MÉTODOS DE VALIDAÇÃO DE TELA ---
    private void carregarScoutDaLiga() {
        scoutDaLiga = null;
        if (sessaoBean.getUsuarioLogado() == null || sessaoBean.getUsuarioLogado().getIdTime() == null) {
            return;
        }

        scoutDaLiga = ligaClient.buscarScoutDaLiga(ligaId, sessaoBean.getUsuarioLogado().getIdTime());
    }

    public boolean jaEhMembro(Long idTimeBuscado) {
        if (ligaAtual != null && ligaAtual.getTimes() != null) {
            return ligaAtual.getTimes().stream()
                    .anyMatch(membro -> membro.getId().equals(idTimeBuscado));
        }
        return false;
    }

    public boolean jaFoiConvidado(Long idTimeBuscado) {
        if (idsTimesComConvite != null) {
            return idsTimesComConvite.contains(idTimeBuscado);
        }
        return false;
    }

    public boolean isAdminDaLiga() {
        if (ligaAtual == null
                || ligaAtual.getAdmin() == null
                || sessaoBean.getUsuarioLogado() == null
                || sessaoBean.getUsuarioLogado().getIdTime() == null) {
            return false;
        }

        return ligaAtual.getAdmin().getId().equals(sessaoBean.getUsuarioLogado().getIdTime());
    }

    public void removerMembro(TimeSimplesDTO membro) {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            if (!isAdminDaLiga()) {
                msgErro("Apenas o administrador da liga pode remover membros.");
                return;
            }

            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            log.info("Iniciando remoção do time '{}' (ID: {}) da liga ID {}", membro.getNome(), membro.getId(), ligaId);
            ligaClient.removerMembro(ligaId, membro.getId(), meuTimeId);
            msgInfo(membro.getNome() + " foi removido da liga.");
            carregarLiga(); 
        } catch (Exception e) {
            log.error("Erro ao remover o time ID {} da liga ID {}", membro.getId(), ligaId, e);
            msgErro("Erro ao remover time.");
        }
    }

    public void prepararBanimento(TimeSimplesDTO membro) {
        this.membroSelecionadoParaBanimento = membro;
        this.motivoBanimento = null;
    }

    public void cancelarBanimento() {
        this.membroSelecionadoParaBanimento = null;
        this.motivoBanimento = null;
    }

    public void banirMembro() {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            if (!isAdminDaLiga()) {
                msgErro("Apenas o administrador da liga pode banir membros.");
                return;
            }
            if (membroSelecionadoParaBanimento == null) {
                msgErro("Selecione um membro para banir.");
                return;
            }

            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            ligaClient.banirMembro(ligaId, membroSelecionadoParaBanimento.getId(), meuTimeId, motivoBanimento);
            msgInfo(membroSelecionadoParaBanimento.getNome() + " foi banido da liga.");
            cancelarBanimento();
            carregarLiga();
        } catch (Exception e) {
            log.error("Erro ao banir membro da liga {}", ligaId, e);
            msgErro("Erro ao banir membro.");
        }
    }

    public void reverterBanimento(BanimentoLigaDTO banimento) {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            if (!isAdminDaLiga()) {
                msgErro("Apenas o administrador da liga pode reverter banimentos.");
                return;
            }

            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            ligaClient.reverterBanimento(ligaId, banimento.getTimeBanido().getId(), meuTimeId);
            msgInfo("Banimento revertido.");
            carregarLiga();
        } catch (Exception e) {
            log.error("Erro ao reverter banimento da liga {}", ligaId, e);
            msgErro("Erro ao reverter banimento.");
        }
    }
    
    public void prepararConvite(TimeSimplesDTO time) {
        this.timeSelecionadoParaConvite = time;
        this.mensagemConvite = "Olá! Venha fazer parte da nossa liga: " + ligaAtual.getNome();
    }

    public void cancelarConvite() {
        this.timeSelecionadoParaConvite = null;
    }

    public void enviarConvite() { 
        try {
            if (ligaAtual == null || timeSelecionadoParaConvite == null) {
                msgErro("Erro interno: Dados do convite perdidos.");
                return;
            }

            log.info("Enviando convite com msg personalizada para '{}'", timeSelecionadoParaConvite.getNome());
            
            ligaClient.enviarConvite(ligaId, timeSelecionadoParaConvite.getId(), mensagemConvite);
            msgInfo("Convite enviado para " + timeSelecionadoParaConvite.getNome() + "!");
            
            // ADICIONA O TIME NA LISTA DE CONVIDADOS PARA A TELA ATUALIZAR SOZINHA
            this.idsTimesComConvite.add(timeSelecionadoParaConvite.getId());
            
            // Limpa a seleção e fecha o painel
            this.timeSelecionadoParaConvite = null;
            
        } catch (Exception e) {
            log.error("Erro ao enviar convite", e);
            msgErro("Erro ao enviar convite.");
        }
    }

    public void criarPublicacao() {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            if (ligaId == null || sessaoBean.getUsuarioLogado() == null || sessaoBean.getUsuarioLogado().getIdTime() == null) {
                msgErro("Nao foi possivel identificar o time logado.");
                return;
            }

            NovaPublicacaoLigaDTO dto = new NovaPublicacaoLigaDTO();
            dto.setIdLiga(ligaId);
            dto.setIdTimeAutor(sessaoBean.getUsuarioLogado().getIdTime());
            dto.setDataJogo(dataJogoPublicacao != null ? dataJogoPublicacao.atStartOfDay() : null);
            dto.setHoraInicio(horaInicioPublicacao);
            dto.setHoraFim(horaFimPublicacao);
            dto.setTipoProcura(tipoProcuraPublicacao);
            dto.setCategoria(categoriaPublicacao);
            dto.setRegiao(regiaoPublicacao);
            dto.setObservacao(observacaoPublicacao);

            ligaClient.criarPublicacao(ligaId, dto);
            msgInfo("Publicacao criada no mural da liga.");
            limparFormularioPublicacao();
            publicacoesDaLiga = ligaClient.listarPublicacoesDaLiga(ligaId);
        } catch (Exception e) {
            log.error("Erro ao criar publicacao no mural da liga {}", ligaId, e);
            msgErro("Erro ao criar publicacao.");
        }
    }

    public void cancelarPublicacao(PublicacaoLigaDTO publicacao) {
        if (!sessaoBean.isAcessoCompleto()) {
            msgErro("Ligas estao disponiveis para plano PRO ou periodo trial ativo.");
            return;
        }

        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            ligaClient.cancelarPublicacao(publicacao.getId(), meuTimeId);
            msgInfo("Publicacao cancelada.");
            publicacoesDaLiga = ligaClient.listarPublicacoesDaLiga(ligaId);
        } catch (Exception e) {
            log.error("Erro ao cancelar publicacao {}", publicacao != null ? publicacao.getId() : null, e);
            msgErro("Erro ao cancelar publicacao.");
        }
    }

    public boolean podeCancelarPublicacao(PublicacaoLigaDTO publicacao) {
        if (publicacao == null || sessaoBean.getUsuarioLogado() == null || ligaAtual == null) {
            return false;
        }

        Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
        return meuTimeId != null
                && (meuTimeId.equals(publicacao.getTimeAutor().getId())
                || (ligaAtual.getAdmin() != null && meuTimeId.equals(ligaAtual.getAdmin().getId())));
    }

    public List<TipoProcuraPublicacaoLiga> getTiposProcura() {
        return Arrays.asList(TipoProcuraPublicacaoLiga.values());
    }

    public List<Categoria> getCategorias() {
        return Arrays.asList(Categoria.values());
    }

    private void limparFormularioPublicacao() {
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
