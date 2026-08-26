package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.dto.ResumoNotificacoesDTO;
import br.com.arenamatch.jsf.client.AgendaClient;
import br.com.arenamatch.jsf.client.LigaClient;
import br.com.arenamatch.jsf.client.NotificacaoClient;
import br.com.arenamatch.jsf.client.PartidaClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientResponseException;

@Named
@SessionScoped
@Slf4j
public class NotificacaoBean implements Serializable {

    @Inject private SessaoBean sessaoBean;
    @Inject private NotificacaoClient notificacaoClient;
    @Inject private LigaClient ligaClient;
    @Inject private AgendaClient agendaClient;
    @Inject private PartidaClient partidaClient;

    @Getter @Setter
    private List<NotificacaoDTO> listaNotificacoes = new ArrayList<>();

    @Getter
    private int totalConvitesJogo = 0;

    @Getter
    private int totalConvitesLiga = 0;

    @Getter
    private int totalNotificacoes = 0;

    @PostConstruct
    public void init() {
        carregarNotificacoes();
    }

    public void carregarNotificacoes() {
        if (sessaoBean.isLogado()) {
            try {
                Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
                if (meuTimeId != null) {
                    this.listaNotificacoes = notificacaoClient.buscarNotificacoes(meuTimeId);
                    aplicarResumo(notificacaoClient.buscarResumo(meuTimeId));
                    sessaoBean.setQtdNotificacoes(this.totalNotificacoes);
                }
            } catch (Exception e) {
                log.error("Erro ao buscar notificações do topo", e);
            }
        }
    }

    public void aceitarConvite(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "LIGA")) {
                ligaClient.responderConvite(notificacao.getIdReferencia(), true);
                msgInfo("Você entrou na liga!");
            } else if (isTipo(notificacao, "JOGO")) {
                agendaClient.aceitarDesafio(notificacao.getIdReferencia());
                msgInfo("Desafio aceito! Jogo confirmado.");
            }

            aposAcao();
        } catch (RestClientResponseException e) {
            log.warn("Convite nao aceito por regra de negocio: status={}", e.getStatusCode());
            msgErro(mensagemErroResposta(e, "Erro ao aceitar convite."));
        } catch (Exception e) {
            log.error("Erro inesperado ao aceitar convite", e);
            msgErro("Erro ao aceitar convite.");
        }
    }

    public void recusarConvite(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "LIGA")) {
                ligaClient.responderConvite(notificacao.getIdReferencia(), false);
                msgInfo("Convite de liga recusado.");
            } else if (isTipo(notificacao, "JOGO")) {
                agendaClient.excluirPartida(notificacao.getIdReferencia());
                msgInfo("Convite de jogo recusado.");
            }

            aposAcao();
        } catch (Exception e) {
            msgErro("Erro ao recusar convite.");
        }
    }

    public void cancelarConvite(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "JOGO")) {
                agendaClient.excluirPartida(notificacao.getIdReferencia());
                msgInfo("Desafio cancelado.");
                aposAcao();
            }
        } catch (Exception e) {
            msgErro("Erro ao cancelar o convite enviado.");
        }
    }

    public void aceitarCancelamentoJogo(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "CANCELAMENTO_JOGO")) {
                agendaClient.responderCancelamento(
                        notificacao.getIdReferencia(),
                        sessaoBean.getUsuarioLogado().getIdTime(),
                        true);
                msgInfo("Jogo cancelado.");
                aposAcao();
            }
        } catch (Exception e) {
            log.error("Erro ao aceitar cancelamento via notificacao", e);
            msgErro("Erro ao aceitar o cancelamento.");
        }
    }

    public void recusarCancelamentoJogo(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "CANCELAMENTO_JOGO")) {
                agendaClient.responderCancelamento(
                        notificacao.getIdReferencia(),
                        sessaoBean.getUsuarioLogado().getIdTime(),
                        false);
                msgInfo("Cancelamento recusado. O jogo continua agendado.");
                aposAcao();
            }
        } catch (Exception e) {
            log.error("Erro ao recusar cancelamento via notificacao", e);
            msgErro("Erro ao recusar o cancelamento.");
        }
    }

    public void confirmarResultadoPlacar(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "PLACAR")) {
                partidaClient.confirmarPlacar(notificacao.getIdReferencia());
                msgInfo("Placar confirmado! Os pontos foram para o Ranking.");
                aposAcao();
            }
        } catch (Exception e) {
            log.error("Erro ao confirmar placar via notificação", e);
            msgErro("Erro ao confirmar o placar.");
        }
    }

    public void contestarResultadoPlacar(NotificacaoDTO notificacao) {
        try {
            if (isTipo(notificacao, "PLACAR")) {
                partidaClient.contestarPlacar(notificacao.getIdReferencia());
                msgInfo("Placar contestado! A partida entrou em disputa.");
                aposAcao();
            }
        } catch (Exception e) {
            log.error("Erro ao contestar placar via notificação", e);
            msgErro("Erro ao contestar o placar.");
        }
    }

    public void informarPlacarPendente(NotificacaoDTO notificacao) {
        if (!isTipo(notificacao, "PLACAR_PENDENTE")) {
            return;
        }

        Long idPartida = notificacao.getIdReferencia();
        Integer golsMandante = notificacao.getGolsMandanteInformado();
        Integer golsVisitante = notificacao.getGolsVisitanteInformado();

        if (golsMandante == null || golsVisitante == null) {
            msgErro("Por favor, preencha os gols dos dois times.");
            return;
        }

        try {
            partidaClient.informarPlacar(
                    idPartida,
                    golsMandante,
                    golsVisitante,
                    sessaoBean.getUsuarioLogado().getIdTime());
            msgInfo("Placar enviado! Aguardando confirmação do adversário.");
            aposAcao();
        } catch (Exception e) {
            log.error("Erro ao informar placar via notificacao", e);
            msgErro("Erro ao salvar o placar.");
        }
    }

    public void informarPlacarDiferente(NotificacaoDTO notificacao) {
        if (!isTipo(notificacao, "PLACAR")) {
            return;
        }

        Integer golsMandante = notificacao.getGolsMandanteInformado();
        Integer golsVisitante = notificacao.getGolsVisitanteInformado();

        if (golsMandante == null || golsVisitante == null) {
            msgErro("Para recusar o resultado, informe o placar correto dos dois times.");
            return;
        }

        try {
            partidaClient.informarPlacar(
                    notificacao.getIdReferencia(),
                    golsMandante,
                    golsVisitante,
                    sessaoBean.getUsuarioLogado().getIdTime());
            msgInfo("Novo placar enviado! Aguardando confirmação do adversário.");
            aposAcao();
        } catch (Exception e) {
            log.error("Erro ao informar placar diferente via notificacao", e);
            msgErro("Erro ao enviar o novo placar.");
        }
    }

    private void aplicarResumo(ResumoNotificacoesDTO resumo) {
        this.totalConvitesJogo = resumo.getTotalConvitesJogo();
        this.totalConvitesLiga = resumo.getTotalConvitesLiga();
        this.totalNotificacoes = resumo.getTotalNotificacoes();
    }

    private boolean isTipo(NotificacaoDTO notificacao, String tipo) {
        return notificacao != null && tipo.equals(notificacao.getTipo());
    }

    private void aposAcao() {
        carregarNotificacoes();
        atualizarTelaAgendaSeNecessario();
    }

    private void atualizarTelaAgendaSeNecessario() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (viewId != null && viewId.contains("minha-agenda")) {
            org.primefaces.PrimeFaces.current().executeScript(
                    "if (typeof atualizarCalendarioAjax === 'function') atualizarCalendarioAjax();");
        }
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private String mensagemErroResposta(RestClientResponseException e, String mensagemPadrao) {
        String mensagemServidor = e.getResponseBodyAsString();
        return mensagemServidor == null || mensagemServidor.isBlank()
                ? mensagemPadrao
                : mensagemServidor;
    }
}
