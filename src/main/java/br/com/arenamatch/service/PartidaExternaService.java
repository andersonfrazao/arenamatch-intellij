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
import java.time.LocalDateTime;
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
    private final PartidaExternaRepository partidas;
    private final AdversarioExternoRepository adversarios;
    private final TimeRepository times;
    private final UsuarioRepository usuarios;
    private final PartidaRepository partidasOficiais;
    private final EntityManager entityManager;

    public PartidaExternaService(PartidaExternaRepository partidas, AdversarioExternoRepository adversarios,
            TimeRepository times, UsuarioRepository usuarios, PartidaRepository partidasOficiais,
            EntityManager entityManager) {
        this.partidas = partidas;
        this.adversarios = adversarios;
        this.times = times;
        this.usuarios = usuarios;
        this.partidasOficiais = partidasOficiais;
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
        MandoPartida mando = mandoDoTime(time);
        dto.setMandoTime(mando);
        if (mando == MandoPartida.MANDANTE) {
            // O mandante cobra a taxa definida no cadastro do time; o visitante informa a taxa do adversário.
            dto.setValorTaxa(time.getValorTaxa() == null ? null
                    : BigDecimal.valueOf(time.getValorTaxa()).setScale(2, RoundingMode.HALF_UP));
        }
        return dto;
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
        var existente = partidas.findByTimeIdAndChaveSolicitacao(time.getId(), dto.getChaveSolicitacao());
        if (existente.isPresent()) {
            conferirReenvio(existente.get(), dto);
            return converterParaEvento(existente.get());
        }
        if (!time.getModalidades().contains(dto.getModalidade())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A modalidade selecionada não está cadastrada no seu time.");
        }
        LocalDateTime inicio = dto.getData().atTime(dto.getHoraInicio());
        LocalDateTime fim = dto.getData().atTime(dto.getHoraFim());
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
        dto.setNomeCampo(validarTexto(dto.getNomeCampo(), "Campo ou quadra", 160, true));
        dto.setEndereco(validarTexto(dto.getEndereco(), "Endereço", 300, false));
        dto.setObservacoes(validarTexto(dto.getObservacoes(), "Observações", 1000, false));
        if (dto.getModalidade() == null || dto.getCategoria() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a modalidade e a categoria.");
        }
        if (dto.getData() == null || dto.getHoraInicio() == null || dto.getHoraFim() == null
                || !dto.getHoraFim().isAfter(dto.getHoraInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data e um horário final posterior ao início.");
        }
        BigDecimal taxa = dto.getValorTaxa();
        if (taxa == null || taxa.signum() < 0 || taxa.compareTo(new BigDecimal("9999999999.99")) > 0
                || taxa.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma taxa válida, com até duas casas decimais. Use zero para jogo sem cobrança.");
        }
        dto.setValorTaxa(taxa.setScale(2, RoundingMode.UNNECESSARY));
    }

    private String validarTexto(String valor, String campo, int limite, boolean obrigatorio) {
        String texto = valor == null ? "" : valor.strip().replaceAll("\\s+", " ");
        if ((obrigatorio && texto.isEmpty()) || texto.length() > limite) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " deve ter "
                    + (obrigatorio ? "de 1 a " : "até ") + limite + " caracteres.");
        }
        return texto.isEmpty() ? null : texto;
    }

    private void conferirReenvio(PartidaExterna partida, RegistroPartidaExternaDTO dto) {
        boolean mesmosDados = Objects.equals(partida.getNomeAdversario(), dto.getNomeAdversario())
                && partida.getModalidade() == dto.getModalidade() && partida.getCategoria() == dto.getCategoria()
                && Objects.equals(partida.getGolsMandante(), dto.getGolsMandante())
                && Objects.equals(partida.getGolsVisitante(), dto.getGolsVisitante())
                && partida.getDataHoraInicio().equals(dto.getData().atTime(dto.getHoraInicio()))
                && partida.getDataHoraFim().equals(dto.getData().atTime(dto.getHoraFim()))
                && partida.getNomeCampo().equals(dto.getNomeCampo())
                && Objects.equals(partida.getEndereco(), dto.getEndereco())
                && partida.getValorTaxa().compareTo(dto.getValorTaxa()) == 0
                && Objects.equals(partida.getObservacoes(), dto.getObservacoes());
        if (!mesmosDados) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este registro já foi salvo. Abra um novo formulário para cadastrar outro jogo.");
        }
    }
}
