package br.com.arenamatch.service;

import br.com.arenamatch.dto.DetalheEstatisticaJogadorDTO;
import br.com.arenamatch.dto.EstatisticaJogadorDTO;
import br.com.arenamatch.dto.HistoricoEstatisticaJogadorDTO;
import br.com.arenamatch.dto.PainelEstatisticasJogadoresDTO;
import br.com.arenamatch.dto.ResumoEstatisticasTimeDTO;
import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.EventoSumula;
import br.com.arenamatch.entity.ParticipacaoPartida;
import br.com.arenamatch.enums.TipoEventoSumula;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.EstatisticasRepository;
import br.com.arenamatch.repository.ParticipacaoPartidaRepository;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EstatisticasGestaoTimeService {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final LocalDate DATA_MAXIMA = LocalDate.of(2100, 1, 1);
    private static final int TAMANHO_HISTORICO = 10;

    private final EstatisticasRepository estatisticasRepository;
    private final AtletaRepository atletaRepository;
    private final ParticipacaoPartidaRepository participacaoRepository;
    private final GestaoTimeAuthorizationService authorizationService;

    public EstatisticasGestaoTimeService(EstatisticasRepository estatisticasRepository,
                                         AtletaRepository atletaRepository,
                                         ParticipacaoPartidaRepository participacaoRepository,
                                         GestaoTimeAuthorizationService authorizationService) {
        this.estatisticasRepository = estatisticasRepository;
        this.atletaRepository = atletaRepository;
        this.participacaoRepository = participacaoRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public ResumoEstatisticasTimeDTO resumirTime(LocalDate inicio, LocalDate fim) {
        var contexto = authorizationService.exigirAcessoPro();
        Periodo periodo = periodo(inicio, fim);
        var resumo = estatisticasRepository.resumirTime(
                contexto.time().getId(), periodo.inicio(), periodo.fimExclusivo());
        long jogos = numero(resumo == null ? null : resumo.getJogos());
        long vitorias = numero(resumo == null ? null : resumo.getVitorias());
        long empates = numero(resumo == null ? null : resumo.getEmpates());
        long derrotas = numero(resumo == null ? null : resumo.getDerrotas());
        long golsPro = numero(resumo == null ? null : resumo.getGolsPro());
        long golsContra = numero(resumo == null ? null : resumo.getGolsContra());
        long pontos = vitorias * 3 + empates;
        return new ResumoEstatisticasTimeDTO(jogos, vitorias, empates, derrotas, pontos,
                golsPro, golsContra, golsPro - golsContra,
                percentual(pontos, jogos * 3), media(golsPro, jogos), media(golsContra, jogos));
    }

    @Transactional(readOnly = true)
    public PainelEstatisticasJogadoresDTO resumirJogadores(
            LocalDate inicio, LocalDate fim, String busca, String ordenacao) {
        var contexto = authorizationService.exigirAcessoPro();
        Periodo periodo = periodo(inicio, fim);
        Map<Long, EstatisticasRepository.ResumoJogadorProjection> totais = new HashMap<>();
        estatisticasRepository.resumirJogadores(
                contexto.time().getId(), periodo.inicio(), periodo.fimExclusivo())
                .forEach(item -> totais.put(item.getAtletaId(), item));

        String termo = normalizar(busca);
        List<EstatisticaJogadorDTO> jogadores = atletaRepository
                .findByTimeIdOrderByNomeAsc(contexto.time().getId()).stream()
                .filter(atleta -> termo.isBlank() || normalizar(atleta.getNome()).contains(termo)
                        || normalizar(atleta.getApelido()).contains(termo))
                .map(atleta -> converter(atleta, totais.get(atleta.getId())))
                .sorted(comparador(ordenacao))
                .toList();

        List<EstatisticaJogadorDTO> artilheiros = jogadores.stream()
                .filter(item -> item.getGols() > 0)
                .sorted(Comparator.comparingLong(EstatisticaJogadorDTO::getGols).reversed()
                        .thenComparing(EstatisticaJogadorDTO::getNomeExibicao,
                                String.CASE_INSENSITIVE_ORDER))
                .limit(5).toList();
        List<EstatisticaJogadorDTO> disciplina = jogadores.stream()
                .filter(item -> item.getCartoesVermelhos() > 0 || item.getCartoesAmarelos() > 0)
                .sorted(Comparator.comparingLong(EstatisticaJogadorDTO::getCartoesVermelhos).reversed()
                        .thenComparing(Comparator.comparingLong(
                                EstatisticaJogadorDTO::getCartoesAmarelos).reversed())
                        .thenComparing(EstatisticaJogadorDTO::getNomeExibicao,
                                String.CASE_INSENSITIVE_ORDER))
                .limit(5).toList();
        return new PainelEstatisticasJogadoresDTO(jogadores, artilheiros, disciplina);
    }

    @Transactional(readOnly = true)
    public DetalheEstatisticaJogadorDTO detalharJogador(
            Long atletaId, LocalDate inicio, LocalDate fim, int pagina) {
        var contexto = authorizationService.exigirAcessoPro();
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Jogador nao encontrado."));
        authorizationService.exigirRecursoDoTime(atleta.getTime().getId(), contexto.time(), "Jogador");
        Periodo periodo = periodo(inicio, fim);
        int paginaSegura = Math.max(pagina, 0);
        var resultado = participacaoRepository.buscarHistoricoEstatistico(
                contexto.time().getId(), atletaId, periodo.inicio(), periodo.fimExclusivo(),
                PageRequest.of(paginaSegura, TAMANHO_HISTORICO));
        List<HistoricoEstatisticaJogadorDTO> historico = resultado.getContent().stream()
                .map(item -> converterHistorico(item, contexto.time().getId())).toList();

        var resumo = estatisticasRepository.resumirJogadores(
                contexto.time().getId(), periodo.inicio(), periodo.fimExclusivo()).stream()
                .filter(item -> Objects.equals(item.getAtletaId(), atletaId)).findFirst().orElse(null);
        return new DetalheEstatisticaJogadorDTO(atleta.getId(), atleta.getNome(), atleta.getApelido(),
                atleta.getSituacao(), numero(resumo == null ? null : resumo.getPartidas()),
                numero(resumo == null ? null : resumo.getGols()),
                numero(resumo == null ? null : resumo.getCartoesAmarelos()),
                numero(resumo == null ? null : resumo.getCartoesVermelhos()),
                historico, paginaSegura, resultado.hasNext());
    }

    private EstatisticaJogadorDTO converter(
            Atleta atleta, EstatisticasRepository.ResumoJogadorProjection resumo) {
        return new EstatisticaJogadorDTO(atleta.getId(), atleta.getNome(), atleta.getApelido(),
                atleta.getSituacao(), numero(resumo == null ? null : resumo.getPartidas()),
                numero(resumo == null ? null : resumo.getGols()),
                numero(resumo == null ? null : resumo.getCartoesAmarelos()),
                numero(resumo == null ? null : resumo.getCartoesVermelhos()));
    }

    private HistoricoEstatisticaJogadorDTO converterHistorico(
            ParticipacaoPartida participacao, Long timeId) {
        var gestao = participacao.getGestaoPartida();
        var partida = gestao.getPartida();
        boolean mandante = Objects.equals(partida.getMandante().getId(), timeId);
        List<EventoSumula> eventos = gestao.getEventos().stream()
                .filter(item -> item.getParticipacao() != null
                        && Objects.equals(item.getParticipacao().getId(), participacao.getId()))
                .toList();
        Predicate<EventoSumula> gol = evento -> evento.getTipo() == TipoEventoSumula.GOL;
        Predicate<EventoSumula> cartao = evento -> evento.getTipo() == TipoEventoSumula.CARTAO_AMARELO
                || evento.getTipo() == TipoEventoSumula.CARTAO_VERMELHO;
        return new HistoricoEstatisticaJogadorDTO(partida.getId(), partida.getDataHora(),
                mandante ? partida.getVisitante().getNome() : partida.getMandante().getNome(),
                mandante ? partida.getGolsMandante() : partida.getGolsVisitante(),
                mandante ? partida.getGolsVisitante() : partida.getGolsMandante(),
                participacao.getNumeroCamisa(), participacao.getPosicao(),
                participacao.getPapel().name(), eventos.stream().filter(gol).count(),
                eventos.stream().filter(e -> e.getTipo() == TipoEventoSumula.CARTAO_AMARELO).count(),
                eventos.stream().filter(e -> e.getTipo() == TipoEventoSumula.CARTAO_VERMELHO).count(),
                minutos(eventos, gol), minutos(eventos, cartao));
    }

    private String minutos(List<EventoSumula> eventos, Predicate<EventoSumula> filtro) {
        String valor = eventos.stream().filter(filtro).filter(item -> item.getMinuto() != null)
                .map(item -> item.getMinuto() + "'").collect(Collectors.joining(", "));
        return valor.isBlank() ? null : valor;
    }

    private Comparator<EstatisticaJogadorDTO> comparador(String ordenacao) {
        Comparator<EstatisticaJogadorDTO> nome = Comparator.comparing(
                EstatisticaJogadorDTO::getNomeExibicao, String.CASE_INSENSITIVE_ORDER);
        if ("gols".equalsIgnoreCase(ordenacao))
            return Comparator.comparingLong(EstatisticaJogadorDTO::getGols).reversed().thenComparing(nome);
        if ("cartoes".equalsIgnoreCase(ordenacao))
            return Comparator.comparingLong((EstatisticaJogadorDTO item) ->
                    item.getCartoesVermelhos() + item.getCartoesAmarelos()).reversed().thenComparing(nome);
        if ("partidas".equalsIgnoreCase(ordenacao))
            return Comparator.comparingLong(EstatisticaJogadorDTO::getPartidas).reversed().thenComparing(nome);
        return nome;
    }

    private Periodo periodo(LocalDate inicio, LocalDate fim) {
        LocalDate inicioValido = inicio == null ? LocalDate.now().withDayOfYear(1) : inicio;
        LocalDate fimValido = fim == null ? LocalDate.now() : fim;
        if (fimValido.isBefore(inicioValido)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data final deve ser igual ou posterior a data inicial.");
        }
        return new Periodo(inicioValido.atStartOfDay(), fimValido.plusDays(1).atStartOfDay());
    }

    public static LocalDate inicioTodoHistorico() { return DATA_MINIMA; }
    public static LocalDate fimTodoHistorico() { return DATA_MAXIMA; }

    private long numero(Number numero) { return numero == null ? 0 : numero.longValue(); }
    private double percentual(long valor, long total) {
        return total == 0 ? 0 : Math.round(valor * 10000.0 / total) / 100.0;
    }
    private double media(long valor, long total) {
        return total == 0 ? 0 : Math.round(valor * 100.0 / total) / 100.0;
    }
    private String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private record Periodo(LocalDateTime inicio, LocalDateTime fimExclusivo) { }
}
