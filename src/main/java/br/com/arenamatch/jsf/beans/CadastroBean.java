package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.integracao.GeoClient;
import br.com.arenamatch.integracao.ViaCepClient;
import br.com.arenamatch.jsf.client.CadastroClient;
import br.com.arenamatch.service.CadastroFormularioService;
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
import org.primefaces.event.FlowEvent;
import org.springframework.beans.factory.annotation.Value;

@Named
@ViewScoped
public class CadastroBean implements Serializable {

    @Getter @Setter
    private CadastroDTO dto = new CadastroDTO();

    @Getter @Setter
    private String confirmarSenha;

    @Getter @Setter
    private String stepAtual = "responsavel";

    @Getter @Setter private Categoria tempCategoria;
    @Getter @Setter private String tempDia;
    @Getter @Setter private String tempInicio;
    @Getter @Setter private String tempFim;

    @Getter @Setter
    private List<DisponibilidadeDTO> agenda = new ArrayList<>();

    @Inject private ViaCepClient viaCepClient;
    @Inject private GeoClient geoClient;
    @Inject private CadastroClient cadastroClient;
    @Inject private CadastroFormularioService cadastroFormularioService;
    @Inject private SessaoBean sessaoBean;

    @Getter
    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean ativacaoEmailHabilitada;

    @PostConstruct
    public void init() {
        if (sessaoBean.isLogado() && sessaoBean.getUsuarioLogado() != null) {
            try {
                Long idUsuario = sessaoBean.getUsuarioLogado().getId();
                this.dto = cadastroClient.buscarDadosParaEdicao(idUsuario);

                if (this.dto.getDisponibilidades() != null) {
                    this.agenda = new ArrayList<>(this.dto.getDisponibilidades());
                }
            } catch (Exception e) {
                msgErro("Erro ao carregar seus dados para edição.");
            }
        } else {
            this.dto = new CadastroDTO();
            this.agenda = new ArrayList<>();
        }
    }

    public String onFlowProcess(FlowEvent event) {
        String abaAtual = event.getOldStep();
        String proximaAba = event.getNewStep();

        if ("responsavel".equals(abaAtual) && "time".equals(proximaAba)) {
            try {
                cadastroFormularioService.validarAvancoResponsavel(dto, confirmarSenha, !sessaoBean.isLogado());
            } catch (RuntimeException e) {
                msgErro(e.getMessage());
                return "responsavel";
            }
        }

        this.stepAtual = proximaAba;
        return proximaAba;
    }

    public void adicionarHorario() {
        try {
            DisponibilidadeDTO novoItem = cadastroFormularioService.criarDisponibilidade(
                    dto, agenda, tempCategoria, tempDia, tempInicio, tempFim);
            this.agenda.add(novoItem);
            this.tempInicio = "";
            this.tempFim = "";
            this.tempCategoria = null;
        } catch (RuntimeException e) {
            msgErro(e.getMessage());
        }
    }

    public void removerHorario(DisponibilidadeDTO item) {
        agenda.remove(item);
    }

    public Categoria[] getCategorias() {
        return Categoria.values();
    }

    public void buscarCep() {
        if (dto.getCep() != null && !dto.getCep().isEmpty()) {
            String cepLimpo = dto.getCep().replaceAll("\\D", "");
            if (cepLimpo.length() == 8) {
                EnderecoDTO endereco = viaCepClient.buscarEndereco(cepLimpo);
                if (endereco != null && !endereco.isErro()) {
                    preencherEndereco(endereco);
                    dto.setLatitude(null);
                    dto.setLongitude(null);
                } else {
                    msgErro("CEP não encontrado.");
                }
            }
        }
    }

    public void buscarEnderecoPorLocalizacao() {
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            msgErro("Não foi possível obter sua localização atual.");
            return;
        }

        EnderecoDTO enderecoGoogle = geoClient.buscarEnderecoPorCoordenadas(dto.getLatitude(), dto.getLongitude());
        if (enderecoGoogle == null) {
            msgErro("Não foi possível converter sua localização em endereço.");
            return;
        }

        String cepLimpo = enderecoGoogle.getCep() != null ? enderecoGoogle.getCep().replaceAll("\\D", "") : "";
        if (cepLimpo.length() == 8) {
            EnderecoDTO enderecoViaCep = viaCepClient.buscarEndereco(cepLimpo);
            if (enderecoViaCep != null && !enderecoViaCep.isErro()) {
                preencherEndereco(enderecoViaCep);
                msgInfo("Localização atual usada para preencher o endereço.");
                return;
            }
        }

        preencherEndereco(enderecoGoogle);
        msgInfo("Localização atual usada para preencher o endereço.");
    }

    public String finalizar() {
        try {
            cadastroFormularioService.validarFinalizacao(dto, agenda, !sessaoBean.isLogado());
            this.dto.setDisponibilidades(agenda);

            if (sessaoBean.isLogado()) {
                Long idUsuario = sessaoBean.getUsuarioLogado().getId();
                cadastroClient.atualizarConta(idUsuario, this.dto);
                sessaoBean.getUsuarioLogado().setNome(this.dto.getNomeResponsavel());

                msgInfo("Dados atualizados com sucesso!");
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                return "/minha-agenda.xhtml?faces-redirect=true";
            }

            cadastroClient.salvarTime(this.dto);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            if (ativacaoEmailHabilitada) {
                msgInfo("Cadastro finalizado com sucesso! Foi enviado um codigo para seu e-mail para a ativacao da conta no primeiro acesso.");
                return "/ativar-conta.xhtml?faces-redirect=true";
            }
            msgInfo("Cadastro finalizado com sucesso! Faca login.");
            return "/login.xhtml?faces-redirect=true";
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String msgServidor = e.getResponseBodyAsString();
            if (msgServidor == null || msgServidor.trim().isEmpty()) {
                msgErro("Erro ao processar a requisição. Código: " + e.getStatusCode());
            } else {
                msgErro(msgServidor);
            }
            return null;
        } catch (Exception e) {
            msgErro(e.getMessage() != null ? e.getMessage() : "Erro ao finalizar o processo.");
            return null;
        }
    }

    public String desativarConta() {
        if (!sessaoBean.isLogado() || sessaoBean.getUsuarioLogado() == null) {
            return "/login.xhtml?faces-redirect=true";
        }

        try {
            cadastroClient.desativarConta(sessaoBean.getUsuarioLogado().getId());
            return sessaoBean.logout();
        } catch (Exception e) {
            msgErro("Erro ao desativar a conta. Tente novamente.");
            return null;
        }
    }

    private void preencherEndereco(EnderecoDTO endereco) {
        dto.setCep(endereco.getCep());
        dto.setLogradouro(endereco.getLogradouro());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getLocalidade());
        dto.setUf(endereco.getUf());
        dto.setRegiao(endereco.getRegiao());
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atenção", msg));
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }
}
