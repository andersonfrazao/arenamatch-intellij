package br.com.arenamatch.jsf.beans;

import java.io.Serializable;
import java.time.LocalDate;

import br.com.arenamatch.dto.AdversarioDetalhesDTO;
import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.JogoRealizadoDTO;
import br.com.arenamatch.jsf.client.PartidaClient;
import br.com.arenamatch.jsf.client.TimeClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class AdversarioDetalhesBean implements Serializable {

    @Inject private TimeClient timeClient;
    @Inject private PartidaClient partidaClient;
    @Inject private SessaoBean sessaoBean;

    @Getter @Setter
    private Long idAdversario;

    @Getter @Setter
    private String data;

    @Getter @Setter
    private String categoria;

    @Getter
    private AdversarioDetalhesDTO adversario;

    @Getter
    private boolean bloqueado;

    @Getter
    private boolean carregado;

    @Getter @Setter
    private boolean preparandoDesafio;

    @Getter @Setter
    private String mensagemDesafio;

    public void carregar() {
        if (carregado) {
            return;
        }
        carregado = true;

        if (!sessaoBean.isAcessoCompleto()) {
            bloqueado = true;
            return;
        }
        if (idAdversario == null) {
            msgErro("Adversario nao informado.");
            return;
        }

        try {
            adversario = timeClient.buscarDetalhesAdversario(idAdversario);
        } catch (RestClientResponseException e) {
            msgErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            msgErro("Nao foi possivel carregar os detalhes do adversario.");
        }
    }

    public void prepararDesafio() {
        preparandoDesafio = true;
        mensagemDesafio = "";
    }

    public void cancelarDesafio() {
        preparandoDesafio = false;
        mensagemDesafio = "";
    }

    public void enviarDesafio() {
        LocalDate dataJogo = getDataJogo();
        if (adversario == null || dataJogo == null) {
            msgErro("Volte para a busca e selecione uma data para desafiar este time.");
            return;
        }

        try {
            DesafioDTO dto = new DesafioDTO();
            dto.setIdTimeDesafiante(sessaoBean.getUsuarioLogado().getIdTime());
            dto.setIdTimeDesafiado(adversario.getId());
            dto.setDataHoraPartida(dataJogo.atStartOfDay());
            dto.setCategoria(getCategoriaDesafio());
            dto.setMensagem(mensagemDesafio);
            partidaClient.enviarDesafio(dto);
            preparandoDesafio = false;
            msgInfo("Desafio enviado para " + adversario.getNome() + ".");
        } catch (RestClientResponseException e) {
            msgErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getMessage());
        }
    }

    public LocalDate getDataJogo() {
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(data);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isPodeDesafiar() {
        LocalDate dataJogo = getDataJogo();
        return dataJogo != null && !dataJogo.isBefore(LocalDate.now());
    }

    public String getPosicaoRankingTexto() {
        return adversario == null || adversario.getPosicaoRanking() == null
                ? "-"
                : adversario.getPosicaoRanking() + "\u00BA";
    }

    private br.com.arenamatch.enums.Categoria getCategoriaDesafio() {
        if (categoria != null && !categoria.isBlank()) {
            try {
                return br.com.arenamatch.enums.Categoria.valueOf(categoria);
            } catch (IllegalArgumentException e) {
                // Usa a categoria principal do time quando o parametro nao for valido.
            }
        }
        return adversario.getCategoria();
    }

    public String resultadoLabel(JogoRealizadoDTO jogo) {
        if (jogo.getGolsMeuTime() > jogo.getGolsAdversario()) {
            return "V";
        }
        if (jogo.getGolsMeuTime().equals(jogo.getGolsAdversario())) {
            return "E";
        }
        return "D";
    }

    public String resultadoCss(JogoRealizadoDTO jogo) {
        return switch (resultadoLabel(jogo)) {
            case "V" -> "resultado-vitoria";
            case "E" -> "resultado-empate";
            default -> "resultado-derrota";
        };
    }

    private void msgErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
    }

    private void msgInfo(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", mensagem));
    }
}
