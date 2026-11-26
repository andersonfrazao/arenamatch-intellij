package br.com.arenamatch.service;

import br.com.arenamatch.dto.*;
import br.com.arenamatch.entity.*;
import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.SituacaoPartidaExterna;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PartidaExternaService {
    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final Duration DURACAO_PADRAO_JOGO = Duration.ofHours(2);

    private final PartidaExternaRepository partidas;
    private final AdversarioExternoRepository adversarios;
    private final TimeRepository times;
    private final UsuarioRepository usuarios;
    private final PartidaRepository partidasOficiais;
    private final AgendaRepository agendas;
    private final EntityManager entityManager;

    public PartidaExternaService(PartidaExternaRepository partidas, AdversarioExternoRepository adversarios,
            TimeRepository times, UsuarioRepository usuarios, PartidaRepository partidasOficiais,
            AgendaRepository agendas, EntityManager entityManager) {
        this.partidas = partidas;
        this.adversarios = adversarios;
        this.times = times;
        this.usuarios = usuarios;
        this.partidasOficiais = partidasOficiais;
        this.agendas = agendas;
        this.entityManager = entityManager;
    }

    public Time buscarTimeAutenticado() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta para registrar um jogo.");
        }
        Usuario usuario = usuarios.findByEmail(autenticacao.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));
        if (usuario.getStatusUsuario() != StatusUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta precisa estar ativa para acessar a agenda.");
        }
        return times.findByResponsavel(usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cadastre seu time para registrar jogos."));
    }

    public void validarAcessoAoTime(Long timeId) {
        if (!Objects.equals(buscarTimeAutenticado().getId(), timeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você pode acessar somente a agenda do seu time.");
        }
    }

    public OpcoesPartidaExternaDTO buscarOpcoes() {
        Time time = buscarTimeAutenticado();
        OpcoesPartidaExternaDTO dto = new OpcoesPartidaExternaDTO();
        dto.setNomeTime(time.getNome());
        dto.setModalidades(time.getModalidades().stream().sorted().toList());
        dto.setAdversarios(adversarios.findByTimeIdOrderByNomeAsc(time.getId()).stream()
                .map(AdversarioExterno::getNome).toList());
        dto.setHorarios(horariosDoTime(time));
        MandoPartida mando = mandoDoTime(time);
        dto.setMandoTime(mando);
        if (mando == MandoPartida.MANDANTE) {
            // O jogo é no campo do mandante: local e taxa vêm do cadastro. O visitante informa os do adversário.
            dto.setValorTaxa(taxaDoCadastro(time));
            dto.setNomeCampo(nomeCampoDoCadastro(time));
            dto.setEndereco(enderecoDoCadastro(time));
        }
        return dto;
    }

    /** Horários da agenda do cadastro, por dia e início. Linhas incompletas ou com hora inválida são ignoradas. */
    private List<HorarioAgendaTimeDTO> horariosDoTime(Time time) {
        List<HorarioAgendaTimeDTO> horarios = new ArrayList<>();
        for (Agenda agenda : agendas.findByTimeId(time.getId())) {
            DayOfWeek dia = diaDaAgenda(agenda.getDiaSemana());
            LocalTime inicio = hora(agenda.getHoraInicio());
            LocalTime fim = hora(agenda.getHoraFim());
            if (dia != null && inicio != null && fim != null
                    && agenda.getModalidade() != null && agenda.getCategoria() != null) {
                horarios.add(new HorarioAgendaTimeDTO(dia, agenda.getModalidade(), agenda.getCategoria(), inicio, fim));
            }
        }
        horarios.sort(Comparator.comparing(HorarioAgendaTimeDTO::getDia)
                .thenComparing(HorarioAgendaTimeDTO::getHoraInicio));
        return horarios;
    }

    private static DayOfWeek diaDaAgenda(String nome) {
        if (nome == null) {
            return null;
        }
        for (DayOfWeek dia : DayOfWeek.values()) {
            if (PartidaHorarioService.nomeDia(dia).equalsIgnoreCase(nome.strip())) {
                return dia;
            }
        }
        return null;
    }

    private static LocalTime hora(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : LocalTime.parse(valor.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * O jogo só pode cair num dia da semana em que o time joga, na modalidade e categoria escolhidas,
     * conforme a agenda do cadastro (mesma regra dos desafios).
     */
    private HorarioAgendaTimeDTO horarioDoDia(Time time, RegistroPartidaExternaDTO dto) {
        List<HorarioAgendaTimeDTO> horarios = horariosDoTime(time).stream()
                .filter(h -> h.getModalidade() == dto.getModalidade() && h.getCategoria() == dto.getCategoria())
                .toList();
        String modalidadeCategoria = dto.getModalidade().getDescricao() + ", categoria " + dto.getCategoria().getDescricao();
        if (horarios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seu time não tem dia de jogo cadastrado para "
                    + modalidadeCategoria + ". Inclua o horário na agenda do time em Meus Dados.");
        }
        DayOfWeek dia = dto.getData().getDayOfWeek();
        return horarios.stream().filter(h -> h.getDia() == dia).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seu time não joga de "
                        + PartidaHorarioService.nomeDia(dia).toLowerCase(LOCALE_BR) + " em " + modalidadeCategoria
                        + ", conforme o cadastro. Escolha uma data de " + listarDias(horarios) + "."));
    }

    /** "sexta", "sexta ou domingo", "segunda, quarta ou sexta". */
    private static String listarDias(List<HorarioAgendaTimeDTO> horarios) {
        List<String> dias = horarios.stream().map(HorarioAgendaTimeDTO::getDia).distinct().sorted()
                .map(dia -> PartidaHorarioService.nomeDia(dia).toLowerCase(LOCALE_BR)).toList();
        if (dias.size() == 1) {
            return dias.getFirst();
        }
        return String.join(", ", dias.subList(0, dias.size() - 1)) + " ou " + dias.getLast();
    }

    private static BigDecimal taxaDoCadastro(Time time) {
        return time.getValorTaxa() == null ? null
                : BigDecimal.valueOf(time.getValorTaxa()).setScale(2, RoundingMode.HALF_UP);
    }

    private static String nomeCampoDoCadastro(Time time) {
        String nome = time.getNomeCampo() == null ? "" : time.getNomeCampo().strip();
        return nome.isEmpty() ? null : limitar(nome, 160);
    }

    /** Mesmo formato do endereço dos jogos oficiais na agenda: "Rua, número, bairro, cidade/UF". */
    private static String enderecoDoCadastro(Time time) {
        List<String> partes = new ArrayList<>();
        adicionar(partes, time.getLogradouro());
        adicionar(partes, time.getNumero());
        adicionar(partes, time.getBairro());
        String cidade = time.getCidade() == null ? "" : time.getCidade().strip();
        if (!cidade.isEmpty()) {
            String uf = time.getUf() == null ? "" : time.getUf().strip();
            partes.add(uf.isEmpty() ? cidade : cidade + "/" + uf);
        }
        return partes.isEmpty() ? null : limitar(String.join(", ", partes), 300);
    }

    private static void adicionar(List<String> partes, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(valor.strip());
        }
    }

    private static String limitar(String texto, int limite) {
        return texto.length() <= limite ? texto : texto.substring(0, limite).strip();
    }

    /**
     * Mandante: horário (da agenda daquele dia), campo, endereço e taxa vêm do cadastro, e o que veio
     * do formulário é descartado. Visitante: sem horário informado, vale o horário da agenda do time.
     */
    private void completarComCadastro(RegistroPartidaExternaDTO dto, Time time, HorarioAgendaTimeDTO horario) {
        if (mandoDoTime(time) == MandoPartida.MANDANTE) {
            dto.setHoraInicio(horario.getHoraInicio());
            dto.setHoraFim(horario.getHoraFim());
            dto.setNomeCampo(nomeCampoDoCadastro(time));
            dto.setEndereco(enderecoDoCadastro(time));
            dto.setValorTaxa(taxaDoCadastro(time));
        } else if (dto.getHoraInicio() == null) {
            dto.setHoraInicio(horario.getHoraInicio());
            dto.setHoraFim(horario.getHoraFim());
        }
    }

    /**
     * Sem horário de término, o jogo dura {@link #DURACAO_PADRAO_JOGO} (a mesma duração exigida do mandante
     * no cadastro). Um término da agenda igual ou anterior ao início (ex.: 22:00 às 00:00) cai no dia seguinte;
     * para horários digitados, isso já foi recusado em validarDados.
     */
    private static LocalDateTime calcularFim(LocalDateTime inicio, LocalTime horaFim) {
        if (horaFim == null) {
            return inicio.plus(DURACAO_PADRAO_JOGO);
        }
        LocalDateTime fim = inicio.toLocalDate().atTime(horaFim);
        return fim.isAfter(inicio) ? fim : fim.plusDays(1);
    }

    /** O mando vem do cadastro do time; o adversário externo sempre joga com o mando oposto. */
    private MandoPartida mandoDoTime(Time time) {
        return time.isMandoCampo() ? MandoPartida.MANDANTE : MandoPartida.VISITANTE;
    }

    @Transactional
    public EventoAgendaDTO registrar(RegistroPartidaExternaDTO dto) {
        validarDados(dto);
        Time time = buscarTimeAutenticado();
        // Serializa cadastros do mesmo time para proteger a chave de reenvio e a reutilização do adversário.
        entityManager.lock(time, LockModeType.PESSIMISTIC_WRITE);
        if (!time.getModalidades().contains(dto.getModalidade())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A modalidade selecionada não está cadastrada no seu time.");
        }
        HorarioAgendaTimeDTO horario = horarioDoDia(time, dto);
        completarComCadastro(dto, time, horario);
        LocalDateTime inicio = dto.getData().atTime(dto.getHoraInicio());
        LocalDateTime fim = calcularFim(inicio, dto.getHoraFim());
        var existente = partidas.findByTimeIdAndChaveSolicitacao(time.getId(), dto.getChaveSolicitacao());
        if (existente.isPresent()) {
            conferirReenvio(existente.get(), dto, inicio, fim);
            return converterParaEvento(existente.get());
        }
        validarPlacar(dto, inicio);
        validarDisponibilidade(time.getId(), dto, inicio, fim);

        String nome = dto.getNomeAdversario();
        String nomeNormalizado = nome.toLowerCase(Locale.ROOT);
        AdversarioExterno adversario = adversarios.findByTimeIdAndNomeNormalizado(time.getId(), nomeNormalizado)
                .orElseGet(() -> {
                    AdversarioExterno novo = new AdversarioExterno();
                    novo.setTime(time);
                    novo.setNome(nome);
                    novo.setNomeNormalizado(nomeNormalizado);
                    return adversarios.save(novo);
                });
        PartidaExterna partida = new PartidaExterna();
        partida.setTime(time);
        partida.setAutor(time.getResponsavel());
        partida.setAdversario(adversario);
        partida.setNomeAdversario(nome);
        partida.setModalidade(dto.getModalidade());
        partida.setCategoria(dto.getCategoria());
        partida.setMando(mandoDoTime(time));
        partida.setGolsMandante(dto.getGolsMandante());
        partida.setGolsVisitante(dto.getGolsVisitante());
        partida.setDataHoraInicio(inicio);
        partida.setDataHoraFim(fim);
        partida.setNomeCampo(dto.getNomeCampo());
        partida.setEndereco(dto.getEndereco());
        partida.setValorTaxa(dto.getValorTaxa());
        partida.setObservacoes(dto.getObservacoes());
        partida.setChaveSolicitacao(dto.getChaveSolicitacao());
        return converterParaEvento(partidas.save(partida));
    }

    public EventoAgendaDTO buscar(Long id) {
        Long timeId = buscarTimeAutenticado().getId();
        return converterParaEvento(partidas.findByIdAndTimeId(id, timeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado na sua agenda.")));
    }

    public List<EventoAgendaDTO> buscarEventos(Long timeId, LocalDate inicio, LocalDate fim) {
        validarAcessoAoTime(timeId);
        return partidas.findByTimeIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(
                timeId, inicio.atStartOfDay(), fim.atTime(23, 59, 59)).stream()
                .map(this::converterParaEvento).toList();
    }

    public List<ProximoJogoDTO> buscarProximosJogos(Long timeId) {
        validarAcessoAoTime(timeId);
        return partidas.findByTimeIdAndDataHoraInicioGreaterThanEqualOrderByDataHoraInicioAsc(
                timeId, LocalDate.now().atStartOfDay()).stream().map(partida -> {
                    EventoAgendaDTO evento = converterParaEvento(partida);
                    ProximoJogoDTO dto = new ProximoJogoDTO();
                    dto.setIdPartidaExterna(partida.getId());
                    dto.setRegistroManual(true);
                    dto.setModalidade(partida.getModalidade());
                    dto.setDataHora(partida.getDataHoraInicio());
                    dto.setNomeTimeMandante(evento.getNomeTimeMandante());
                    dto.setNomeTimeVisitante(evento.getNomeTimeVisitante());
                    dto.setEscudoTimeMandante(evento.getEscudoTimeMandante());
                    dto.setEscudoTimeVisitante(evento.getEscudoTimeVisitante());
                    dto.setPosicaoMeuTime(partida.getMando().getDescricao());
                    dto.setEndereco(partida.getEndereco());
                    return dto;
                }).toList();
    }

    private EventoAgendaDTO converterParaEvento(PartidaExterna partida) {
        boolean mandante = partida.getMando() == MandoPartida.MANDANTE;
        Time time = partida.getTime();
        EventoAgendaDTO dto = new EventoAgendaDTO();
        dto.setIdPartidaExterna(partida.getId());
        dto.setRegistroManual(true);
        dto.setTipo("MANUAL");
        dto.setTitulo("Contra " + partida.getNomeAdversario());
        dto.setNomeTimeMandante(mandante ? time.getNome() : partida.getNomeAdversario());
        dto.setNomeTimeVisitante(mandante ? partida.getNomeAdversario() : time.getNome());
        dto.setEscudoTimeMandante(mandante ? time.getEscudo() : null);
        dto.setEscudoTimeVisitante(mandante ? null : time.getEscudo());
        dto.setModalidade(partida.getModalidade());
        dto.setCategoria(partida.getCategoria());
        dto.setDataHora(partida.getDataHoraInicio());
        dto.setDataHoraFim(partida.getDataHoraFim());
        dto.setPosicaoMeuTime(partida.getMando().getDescricao());
        dto.setNomeCampo(partida.getNomeCampo());
        dto.setEndereco(partida.getEndereco());
        dto.setTaxaRegistroManual(partida.getValorTaxa());
        dto.setMensagem(partida.getObservacoes());
        SituacaoPartidaExterna situacao = calcularSituacao(partida);
        dto.setSituacaoRegistroManual(situacao);
        dto.setPassouDaHora(situacao != SituacaoPartidaExterna.AGENDADO);
        dto.setStatusPlacar(situacao == SituacaoPartidaExterna.REALIZADO ? "INFORMADO" : "PENDENTE");
        dto.setGolsMandante(partida.getGolsMandante());
        dto.setGolsVisitante(partida.getGolsVisitante());
        return dto;
    }

    /** Mesma regra das partidas oficiais na agenda: depois do horário de início, o jogo aguarda o placar. */
    private SituacaoPartidaExterna calcularSituacao(PartidaExterna partida) {
        if (partida.getDataHoraInicio().isAfter(LocalDateTime.now())) {
            return SituacaoPartidaExterna.AGENDADO;
        }
        return partida.getGolsMandante() != null && partida.getGolsVisitante() != null
                ? SituacaoPartidaExterna.REALIZADO
                : SituacaoPartidaExterna.AGUARDANDO_PLACAR;
    }

    private void validarPlacar(RegistroPartidaExternaDTO dto, LocalDateTime inicio) {
        Integer golsMandante = dto.getGolsMandante();
        Integer golsVisitante = dto.getGolsVisitante();
        if (golsMandante == null && golsVisitante == null) {
            return;
        }
        if (golsMandante == null || golsVisitante == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe os gols dos dois times. Para empate sem gols, preencha 0 nos dois campos.");
        }
        if (golsMandante < 0 || golsVisitante < 0 || golsMandante > 99 || golsVisitante > 99) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um placar entre 0 e 99 gols para cada time.");
        }
        if (inicio.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O placar só pode ser informado para jogos que já começaram ou foram realizados.");
        }
    }

    private void validarDisponibilidade(Long timeId, RegistroPartidaExternaDTO dto,
            LocalDateTime inicio, LocalDateTime fim) {
        if (partidas.existeConflito(timeId, dto.getModalidade(), inicio, fim)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seu time já possui um jogo dessa modalidade neste horário.");
        }
        boolean jogoOficial = partidasOficiais.buscarPartidasPorIntervalo(
                timeId, dto.getData().atStartOfDay(), dto.getData().atTime(23, 59, 59)).stream()
                .anyMatch(p -> p.getStatus() == StatusPartida.AGENDADO && p.getModalidade() == dto.getModalidade());
        if (jogoOficial) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seu time já possui um jogo confirmado dessa modalidade nesta data.");
        }
    }

    private void validarDados(RegistroPartidaExternaDTO dto) {
        if (dto == null || dto.getChaveSolicitacao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível identificar o registro. Reabra o formulário.");
        }
        dto.setNomeAdversario(validarTexto(dto.getNomeAdversario(), "Nome do adversário", 120, true));
        dto.setNomeCampo(validarTexto(dto.getNomeCampo(), "Campo ou quadra", 160, false));
        dto.setEndereco(validarTexto(dto.getEndereco(), "Endereço", 300, false));
        dto.setObservacoes(validarTexto(dto.getObservacoes(), "Observações", 1000, false));
        if (dto.getModalidade() == null || dto.getCategoria() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a modalidade e a categoria.");
        }
        if (dto.getData() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data do jogo.");
        }
        if (dto.getHoraInicio() == null && dto.getHoraFim() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe também o horário de início ou deixe os dois horários em branco.");
        }
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null && !dto.getHoraFim().isAfter(dto.getHoraInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de término deve ser depois do início.");
        }
        BigDecimal taxa = dto.getValorTaxa();
        if (taxa != null && (taxa.signum() < 0 || taxa.compareTo(new BigDecimal("9999999999.99")) > 0
                || taxa.stripTrailingZeros().scale() > 2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma taxa válida, com até duas casas decimais, ou deixe em branco.");
        }
        dto.setValorTaxa(taxa == null ? null : taxa.setScale(2, RoundingMode.UNNECESSARY));
    }

    private String validarTexto(String valor, String campo, int limite, boolean obrigatorio) {
        String texto = valor == null ? "" : valor.strip().replaceAll("\\s+", " ");
        if ((obrigatorio && texto.isEmpty()) || texto.length() > limite) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " deve ter "
                    + (obrigatorio ? "de 1 a " : "até ") + limite + " caracteres.");
        }
        return texto.isEmpty() ? null : texto;
    }

    private void conferirReenvio(PartidaExterna partida, RegistroPartidaExternaDTO dto,
            LocalDateTime inicio, LocalDateTime fim) {
        boolean mesmosDados = Objects.equals(partida.getNomeAdversario(), dto.getNomeAdversario())
                && partida.getModalidade() == dto.getModalidade() && partida.getCategoria() == dto.getCategoria()
                && Objects.equals(partida.getGolsMandante(), dto.getGolsMandante())
                && Objects.equals(partida.getGolsVisitante(), dto.getGolsVisitante())
                && partida.getDataHoraInicio().equals(inicio)
                && partida.getDataHoraFim().equals(fim)
                && Objects.equals(partida.getNomeCampo(), dto.getNomeCampo())
                && Objects.equals(partida.getEndereco(), dto.getEndereco())
                && mesmoValor(partida.getValorTaxa(), dto.getValorTaxa())
                && Objects.equals(partida.getObservacoes(), dto.getObservacoes());
        if (!mesmosDados) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este registro já foi salvo. Abra um novo formulário para cadastrar outro jogo.");
        }
    }

    private static boolean mesmoValor(BigDecimal a, BigDecimal b) {
        return a == null || b == null ? a == b : a.compareTo(b) == 0;
    }
}
