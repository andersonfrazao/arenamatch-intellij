package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.RegistroPartidaExternaDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.Modalidade;
import br.com.arenamatch.jsf.client.PartidaExternaClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.Getter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class RegistroPartidaBean implements Serializable {
    @Inject private PartidaExternaClient partidaExternaClient;
    @Inject private SessaoBean sessaoBean;

    @Getter private RegistroPartidaExternaDTO dto = new RegistroPartidaExternaDTO();
    @Getter private List<Modalidade> modalidades = List.of();
    @Getter private String nomeTime;
    private List<String> adversarios = List.of();
    @Getter private boolean pronto;

    @PostConstruct
    public void init() {
        dto.setChaveSolicitacao(UUID.randomUUID());
        dto.setData(LocalDate.now());
        if (!sessaoBean.isLogado()) {
            redirecionar("/login.xhtml");
            return;
        }
        try {
            var opcoes = partidaExternaClient.buscarOpcoes();
            modalidades = opcoes.getModalidades();
            adversarios = opcoes.getAdversarios();
            nomeTime = opcoes.getNomeTime();
            if (modalidades.size() == 1) dto.setModalidade(modalidades.getFirst());
            aplicarParametros();
            pronto = true;
        } catch (RestClientResponseException e) {
            mensagemErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            mensagemErro("Não foi possível abrir o registro de jogo. Tente novamente.");
        }
    }

    private void aplicarParametros() {
        var parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        try {
            if (parametros.containsKey("data")) dto.setData(LocalDate.parse(parametros.get("data")));
            if (parametros.containsKey("modalidade")) {
                Modalidade modalidade = Modalidade.valueOf(parametros.get("modalidade"));
                if (modalidades.contains(modalidade)) dto.setModalidade(modalidade);
            }
            if (parametros.containsKey("categoria")) dto.setCategoria(Categoria.valueOf(parametros.get("categoria")));
        } catch (IllegalArgumentException e) {
            mensagemErro("Confira a data, a modalidade e a categoria antes de salvar.");
        }
    }

    public void salvar() {
        if (!pronto) {
            mensagemErro("Reabra o formulário para registrar o jogo.");
            return;
        }
        try {
            var evento = partidaExternaClient.registrar(dto);
            var contexto = FacesContext.getCurrentInstance();
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Jogo registrado", "A partida foi adicionada à sua agenda."));
            contexto.getExternalContext().getFlash().setKeepMessages(true);
            redirecionar("/minha-agenda.xhtml?data=" + evento.getDataHora().toLocalDate());
        } catch (RestClientResponseException e) {
            mensagemErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            mensagemErro("Não foi possível concluir o registro. Seus dados foram mantidos. Tente salvar novamente.");
        }
    }

    public List<String> completarAdversario(String consulta) {
        String termo = consulta == null ? "" : consulta.toLowerCase(Locale.ROOT);
        return adversarios.stream().filter(nome -> nome.toLowerCase(Locale.ROOT).contains(termo)).limit(15).toList();
    }

    public Categoria[] getCategorias() {
        return Categoria.values();
    }

    public MandoPartida[] getMandos() {
        return MandoPartida.values();
    }

    public String getOrientacaoTaxa() {
        if (dto.getMando() == MandoPartida.VISITANTE) {
            return "Valor que seu time pagará ou pagou ao mandante. Informe zero se não houver cobrança.";
        }
        return "Valor combinado com o adversário. Informe zero se não houver cobrança.";
    }

    private void redirecionar(String caminho) {
        try {
            var externo = FacesContext.getCurrentInstance().getExternalContext();
            externo.redirect(externo.getRequestContextPath() + caminho);
        } catch (IOException e) {
            throw new jakarta.faces.FacesException("Não foi possível abrir a página.", e);
        }
    }

    private void mensagemErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Confira o registro", mensagem == null || mensagem.isBlank() ? "Não foi possível registrar o jogo." : mensagem));
    }
}
