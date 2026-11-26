package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.HorarioAgendaTimeDTO;
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
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class RegistroPartidaBean implements Serializable {
    private static final Locale LOCALE_BR = Locale.of("pt", "BR");

    @Inject private PartidaExternaClient partidaExternaClient;
    @Inject private SessaoBean sessaoBean;

    @Getter private RegistroPartidaExternaDTO dto = new RegistroPartidaExternaDTO();
    @Getter private String nomeTime;
    /** Mando do time logado, vindo do cadastro. O adversário sempre joga com o mando oposto. */
    @Getter private MandoPartida mandoTime;
    /** Dias e horários da agenda do cadastro. O backend repete a validação do dia da semana. */
    private List<HorarioAgendaTimeDTO> horarios = List.of();
    private List<Modalidade> modalidadesDoTime = List.of();
    private List<String> adversarios = List.of();
    /** Dados do cadastro do mandante, só para exibição: o backend usa o cadastro ao salvar. */
    @Getter private String nomeCampoCadastro;
    @Getter private String enderecoCadastro;
    @Getter private BigDecimal taxaCadastro;
    @Getter private boolean pronto;
    /**
     * Falha ao abrir o formulário. Fica na própria página (com "Tentar novamente"), porque
     * o growl do layout já foi desenhado quando o bean é criado na primeira requisição.
     */
    @Getter private String erroCarga;

    @PostConstruct
    public void init() {
        dto.setChaveSolicitacao(UUID.randomUUID());
        if (!sessaoBean.isLogado()) {
            redirecionar("/login.xhtml");
            return;
        }
        try {
            var opcoes = partidaExternaClient.buscarOpcoes();
            modalidadesDoTime = opcoes.getModalidades() == null ? List.of() : opcoes.getModalidades();
            horarios = opcoes.getHorarios() == null ? List.of() : opcoes.getHorarios();
            adversarios = opcoes.getAdversarios() == null ? List.of() : opcoes.getAdversarios();
            nomeTime = opcoes.getNomeTime();
            mandoTime = opcoes.getMandoTime();
            nomeCampoCadastro = opcoes.getNomeCampo();
            enderecoCadastro = opcoes.getEndereco();
            taxaCadastro = opcoes.getValorTaxa();
            aplicarParametros();
            if (dto.getModalidade() == null && getModalidades().size() == 1) dto.setModalidade(getModalidades().getFirst());
            ajustarCategoria();
            if (dto.getData() == null && diaPermitido(LocalDate.now())) dto.setData(LocalDate.now());
            pronto = true;
        } catch (RestClientResponseException e) {
            erroCarga = textoOuPadrao(e.getResponseBodyAsString(), "Não foi possível abrir o registro de jogo. Tente novamente.");
        } catch (Exception e) {
            erroCarga = "Não foi possível abrir o registro de jogo. Tente novamente.";
        }
    }

    private void aplicarParametros() {
        var parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        try {
            if (parametros.containsKey("data")) dto.setData(LocalDate.parse(parametros.get("data")));
            if (parametros.containsKey("modalidade")) {
                Modalidade modalidade = Modalidade.valueOf(parametros.get("modalidade"));
                if (getModalidades().contains(modalidade)) dto.setModalidade(modalidade);
            }
            if (parametros.containsKey("categoria")) dto.setCategoria(Categoria.valueOf(parametros.get("categoria")));
        } catch (IllegalArgumentException e) {
            // Parâmetro inválido no link (data, modalidade ou categoria): o formulário abre com o
            // que foi possível aproveitar e o usuário escolhe o restante.
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

    /** Ao trocar a modalidade, a categoria só continua se o time tiver horário com as duas. */
    public void aoMudarModalidade() {
        ajustarCategoria();
    }

    private void ajustarCategoria() {
        List<Categoria> categorias = getCategorias();
        if (dto.getCategoria() != null && !categorias.contains(dto.getCategoria())) dto.setCategoria(null);
        if (dto.getCategoria() == null && categorias.size() == 1) dto.setCategoria(categorias.getFirst());
    }

    public List<String> completarAdversario(String consulta) {
        String termo = consulta == null ? "" : consulta.toLowerCase(Locale.ROOT);
        return adversarios.stream().filter(nome -> nome.toLowerCase(Locale.ROOT).contains(termo)).limit(15).toList();
    }

    /** Modalidades do time que têm dia de jogo na agenda do cadastro. */
    public List<Modalidade> getModalidades() {
        Set<Modalidade> comHorario = horarios.stream().map(HorarioAgendaTimeDTO::getModalidade).collect(Collectors.toSet());
        return modalidadesDoTime.stream().filter(comHorario::contains).toList();
    }

    /** Categorias da agenda do time (da modalidade escolhida, quando houver). */
    public List<Categoria> getCategorias() {
        return horarios.stream()
                .filter(h -> dto.getModalidade() == null || h.getModalidade() == dto.getModalidade())
                .map(HorarioAgendaTimeDTO::getCategoria).distinct().sorted().toList();
    }

    /** O cadastro não tem nenhum dia de jogo: não há data que o backend aceite. */
    public boolean isSemAgenda() {
        return getModalidades().isEmpty();
    }

    /** Horários que valem para a modalidade e a categoria escolhidas (todos, enquanto não forem escolhidas). */
    private Stream<HorarioAgendaTimeDTO> horariosDaEscolha() {
        return horarios.stream()
                .filter(h -> dto.getModalidade() == null || h.getModalidade() == dto.getModalidade())
                .filter(h -> dto.getCategoria() == null || h.getCategoria() == dto.getCategoria());
    }

    private List<DayOfWeek> diasPermitidos() {
        return horariosDaEscolha().map(HorarioAgendaTimeDTO::getDia).distinct().sorted().toList();
    }

    private boolean diaPermitido(LocalDate data) {
        return data != null && diasPermitidos().contains(data.getDayOfWeek());
    }

    /** Dias bloqueados no calendário, no formato do p:datePicker (0 = domingo ... 6 = sábado). */
    public List<Integer> getDiasDesabilitados() {
        List<DayOfWeek> permitidos = diasPermitidos();
        return Stream.of(DayOfWeek.values()).filter(dia -> !permitidos.contains(dia))
                .map(dia -> dia.getValue() % 7).sorted().toList();
    }

    /** "sexta", "sexta e domingo", "segunda, quarta e sexta". */
    public String getDiasDoTime() {
        List<String> nomes = diasPermitidos().stream()
                .map(dia -> dia.getDisplayName(TextStyle.FULL, LOCALE_BR).replace("-feira", "")).toList();
        if (nomes.size() <= 1) return nomes.isEmpty() ? "" : nomes.getFirst();
        return String.join(", ", nomes.subList(0, nomes.size() - 1)) + " e " + nomes.getLast();
    }

    /** A data escolhida (ex.: vinda da busca) não cai num dia da agenda do time. */
    public boolean isDataForaDaAgenda() {
        return dto.getData() != null && !diaPermitido(dto.getData());
    }

    /** Horário da agenda do time no dia escolhido, para a modalidade e a categoria escolhidas. */
    public HorarioAgendaTimeDTO getHorarioDoDia() {
        if (dto.getData() == null || dto.getModalidade() == null || dto.getCategoria() == null) return null;
        DayOfWeek dia = dto.getData().getDayOfWeek();
        return horariosDaEscolha().filter(h -> h.getDia() == dia).findFirst().orElse(null);
    }

    public String getHorarioDoDiaTexto() {
        HorarioAgendaTimeDTO horario = getHorarioDoDia();
        return horario == null ? null : horario.getHoraInicio() + " às " + horario.getHoraFim();
    }

    public boolean isMandante() {
        return mandoTime == MandoPartida.MANDANTE;
    }

    public MandoPartida getMandoAdversario() {
        return isMandante() ? MandoPartida.VISITANTE : MandoPartida.MANDANTE;
    }

    public boolean isTaxaCadastroGratuita() {
        return taxaCadastro != null && taxaCadastro.signum() == 0;
    }

    public String getOrientacaoTaxa() {
        return "Valor que seu time vai pagar ao mandante. Deixe em branco se ainda não souber.";
    }

    private void redirecionar(String caminho) {
        try {
            var externo = FacesContext.getCurrentInstance().getExternalContext();
            externo.redirect(externo.getRequestContextPath() + caminho);
        } catch (IOException e) {
            throw new jakarta.faces.FacesException("Não foi possível abrir a página.", e);
        }
    }

    /** O growl da página mostra só o resumo (como as mensagens de campo obrigatório), então o texto vai nele. */
    private void mensagemErro(String mensagem) {
        String texto = textoOuPadrao(mensagem, "Não foi possível registrar o jogo.");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, texto));
    }

    private static String textoOuPadrao(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto;
    }
}
