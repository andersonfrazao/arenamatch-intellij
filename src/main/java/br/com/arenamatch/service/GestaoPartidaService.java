package br.com.arenamatch.service;

import br.com.arenamatch.dto.DisponibilidadeGestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import br.com.arenamatch.dto.PaginaHistoricoGestaoPartidaDTO;
import br.com.arenamatch.dto.ResumoHistoricoGestaoPartidaDTO;
import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.EventoSumula;
import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.entity.ParticipacaoPartida;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.StatusGestaoPartida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.enums.TipoEventoSumula;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.GestaoPartidaRepository;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.ParticipacaoPartidaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GestaoPartidaService {

    private static final int TAMANHO_PAGINA_HISTORICO = 10;

    private final GestaoPartidaRepository gestaoPartidaRepository;
    private final PartidaRepository partidaRepository;
    private final ParticipacaoPartidaRepository participacaoPartidaRepository;
    private final AtletaRepository atletaRepository;
    private final GestaoPartidaValidator validator;
    private final GestaoTimeAuthorizationService authorizationService;

    public GestaoPartidaService(
            GestaoPartidaRepository gestaoPartidaRepository,
            PartidaRepository partidaRepository,
            ParticipacaoPartidaRepository participacaoPartidaRepository,
            AtletaRepository atletaRepository,
            GestaoPartidaValidator validator,
            GestaoTimeAuthorizationService authorizationService) {
        this.gestaoPartidaRepository = gestaoPartidaRepository;
        this.partidaRepository = partidaRepository;
        this.participacaoPartidaRepository = participacaoPartidaRepository;
        this.atletaRepository = atletaRepository;
        this.validator = validator;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public DisponibilidadeGestaoPartidaDTO consultarDisponibilidade(Long partidaId) {
        Contexto contexto = carregarContexto(partidaId, false);
        boolean acessoPro = contexto.acessoPro();
        boolean estadoValido = validator.estadoPermiteGestao(contexto.partida());
        boolean liberada = estadoValido && validator.estaLiberada(contexto.partida(), LocalDateTime.now());
        boolean placarInformado = contexto.partida().getStatusPlacar() != null
                && contexto.partida().getStatusPlacar() != StatusPlacar.PENDENTE;
        boolean placarConfirmado = contexto.partida().getStatusPlacar() == StatusPlacar.CONFIRMADO;
        boolean pendenteConclusao = placarInformado
                && gestaoPartidaRepository.findByPartidaIdAndTimeId(partidaId, contexto.time().getId())
                        .map(gestao -> gestao.getStatus() != StatusGestaoPartida.PUBLICADO)
                        .orElse(true);

        String mensagem;
        if (!acessoPro) {
            mensagem = authorizationService.mensagemBloqueio(
                    contexto.usuario().getPlanoAssinatura(), contexto.usuario().getStatusPagamento());
        } else if (!estadoValido) {
            mensagem = "Esta partida nao permite gestao.";
        } else if (!liberada) {
            mensagem = "A prancheta sera liberada no horario marcado da partida.";
        } else if (pendenteConclusao) {
            mensagem = "Conclua a escalacao, os gols e os cartoes da partida.";
        } else {
            mensagem = "Prancheta disponivel.";
        }

        return new DisponibilidadeGestaoPartidaDTO(
                true,
                acessoPro,
                acessoPro && liberada,
                placarInformado,
                placarConfirmado,
                pendenteConclusao,
                contexto.partida().getDataHora(),
                contexto.partida().getMandante().getNome(),
                contexto.partida().getGolsMandante(),
                contexto.partida().getGolsVisitante(),
                contexto.partida().getVisitante().getNome(),
                Objects.equals(contexto.partida().getMandante().getId(), contexto.time().getId())
                        ? contexto.partida().getGolsMandante() : contexto.partida().getGolsVisitante(),
                mensagem);
    }

    @Transactional(readOnly = true)
    public GestaoPartidaDTO buscar(Long partidaId) {
        Contexto contexto = carregarContexto(partidaId, true);
        return gestaoPartidaRepository.findByPartidaIdAndTimeId(partidaId, contexto.time().getId())
                .map(this::converter)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public PaginaHistoricoGestaoPartidaDTO buscarHistorico(int pagina) {
        GestaoTimeAuthorizationService.ContextoAcesso acesso = authorizationService.exigirAcessoPro();
        int paginaNormalizada = Math.max(0, pagina);
        var resultado = partidaRepository.buscarPartidasElegiveisGestao(
                acesso.time().getId(), PageRequest.of(paginaNormalizada, TAMANHO_PAGINA_HISTORICO));
        List<Long> partidaIds = resultado.getContent().stream().map(Partida::getId).toList();
        Map<Long, StatusGestaoPartida> statusPorPartida = partidaIds.isEmpty()
                ? Map.of()
                : gestaoPartidaRepository.findByTimeIdAndPartidaIdIn(acesso.time().getId(), partidaIds).stream()
                        .collect(Collectors.toMap(item -> item.getPartida().getId(), GestaoPartida::getStatus));
        List<ResumoHistoricoGestaoPartidaDTO> partidas = resultado.getContent().stream()
                .map(item -> new ResumoHistoricoGestaoPartidaDTO(
                        item.getId(), item.getDataHora(),
                        item.getMandante().getNome(), item.getMandante().getEscudo(),
                        item.getVisitante().getNome(), item.getVisitante().getEscudo(),
                        item.getGolsMandante(), item.getGolsVisitante(), statusPorPartida.get(item.getId()),
                        item.getStatus(), item.getStatusPlacar()))
                .toList();
        return new PaginaHistoricoGestaoPartidaDTO(partidas, resultado.hasNext());
    }

    @Transactional
    public GestaoPartidaDTO salvarRascunho(Long partidaId, GestaoPartidaRequestDTO request) {
        Contexto contexto = carregarContexto(partidaId, true);
        validator.validarPartidaEditavel(contexto.partida(), contexto.time(), LocalDateTime.now());
        validator.validarRascunho(request);
        gestaoPartidaRepository.findByPartidaIdAndTimeId(partidaId, contexto.time().getId())
                .filter(gestao -> gestao.getStatus() == StatusGestaoPartida.PUBLICADO)
                .ifPresent(gestao -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Uma sumula publicada deve ser corrigida pelo fluxo de publicacao.");
                });
        GestaoPartida gestao = prepararGestao(contexto, request);
        gestao.setStatus(placarInformado(contexto.partida())
                ? StatusGestaoPartida.PENDENTE_CONCLUSAO
                : StatusGestaoPartida.RASCUNHO);
        return converter(gestaoPartidaRepository.saveAndFlush(gestao));
    }

    @Transactional
    public GestaoPartidaDTO publicar(Long partidaId, GestaoPartidaRequestDTO request) {
        Contexto contexto = carregarContexto(partidaId, true);
        validator.validarPartidaEditavel(contexto.partida(), contexto.time(), LocalDateTime.now());
        validator.validarPublicacao(contexto.partida(), contexto.time(), request);
        GestaoPartida gestao = prepararGestao(contexto, request);
        gestao.setStatus(StatusGestaoPartida.PUBLICADO);
        gestao.setEtapa(EtapaGestaoPartida.PUBLICACAO);
        gestao.setPublicadoPor(contexto.usuario());
        gestao.setDataPublicacao(LocalDateTime.now());
        return converter(gestaoPartidaRepository.saveAndFlush(gestao));
    }

    private GestaoPartida prepararGestao(Contexto contexto, GestaoPartidaRequestDTO request) {
        GestaoPartida gestao = gestaoPartidaRepository
                .findByPartidaIdAndTimeId(contexto.partida().getId(), contexto.time().getId())
                .orElseGet(() -> novaGestao(contexto));
        if (gestao.getId() != null && !Objects.equals(gestao.getVersao(), request.versao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O rascunho foi alterado em outra sessao. Recarregue antes de salvar.");
        }

        gestao.setEtapa(request.etapa() == null ? EtapaGestaoPartida.ESCALACAO : request.etapa());
        gestao.setFormacao(normalizar(request.formacao()));
        gestao.setFormacaoPersonalizada(normalizar(request.formacaoPersonalizada()));
        gestao.setAlteradoPor(contexto.usuario());

        Map<Long, Atleta> atletas = carregarAtletas(contexto.time(), request);
        // Remove primeiro os eventos e participações anteriores. Isso também garante
        // que uma gestão nova já tenha identidade antes de receber seus dependentes.
        gestao.substituirEventos(List.of());
        gestao.substituirParticipacoes(List.of());
        gestaoPartidaRepository.saveAndFlush(gestao);

        List<ParticipacaoPartida> participacoes = criarParticipacoes(request, atletas);
        gestao.substituirParticipacoes(participacoes);
        participacaoPartidaRepository.saveAllAndFlush(participacoes);

        Map<Long, ParticipacaoPartida> participacaoPorAtleta = participacoes.stream()
                .collect(Collectors.toMap(item -> item.getAtleta().getId(), Function.identity()));
        gestao.substituirEventos(criarEventos(contexto.partida(), contexto.time(), request, participacaoPorAtleta));
        return gestao;
    }

    private GestaoPartida novaGestao(Contexto contexto) {
        GestaoPartida gestao = new GestaoPartida();
        gestao.setPartida(contexto.partida());
        gestao.setTime(contexto.time());
        gestao.setCriadoPor(contexto.usuario());
        return gestao;
    }

    private Map<Long, Atleta> carregarAtletas(Time time, GestaoPartidaRequestDTO request) {
        List<Long> ids = lista(request.participacoes()).stream()
                .map(GestaoPartidaRequestDTO.ParticipacaoRequestDTO::atletaId)
                .distinct()
                .toList();
        Map<Long, Atleta> atletas = atletaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Atleta::getId, Function.identity()));
        if (atletas.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Atleta informado nao foi encontrado.");
        }
        for (Atleta atleta : atletas.values()) {
            authorizationService.exigirRecursoDoTime(atleta.getTime().getId(), time, "Atleta");
            if (atleta.getSituacao() != SituacaoAtleta.ATIVO) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Atleta inativo nao pode entrar em uma nova escalacao.");
            }
        }
        return atletas;
    }

    private List<ParticipacaoPartida> criarParticipacoes(
            GestaoPartidaRequestDTO request, Map<Long, Atleta> atletas) {
        List<ParticipacaoPartida> resultado = new ArrayList<>();
        for (var item : lista(request.participacoes())) {
            ParticipacaoPartida participacao = new ParticipacaoPartida();
            participacao.setAtleta(atletas.get(item.atletaId()));
            participacao.setPapel(item.papel());
            participacao.setNumeroCamisa(item.numeroCamisa());
            participacao.setPosicao(normalizar(item.posicao()));
            participacao.setSlotTatico(normalizar(item.slotTatico()));
            participacao.setCoordenadaX(item.coordenadaX());
            participacao.setCoordenadaY(item.coordenadaY());
            participacao.setOrdem(item.ordem() == null ? resultado.size() : item.ordem());
            resultado.add(participacao);
        }
        return resultado;
    }

    private List<EventoSumula> criarEventos(
            Partida partida,
            Time time,
            GestaoPartidaRequestDTO request,
            Map<Long, ParticipacaoPartida> participacaoPorAtleta) {
        Time adversario = Objects.equals(partida.getMandante().getId(), time.getId())
                ? partida.getVisitante()
                : partida.getMandante();
        List<EventoSumula> resultado = new ArrayList<>();
        for (var item : lista(request.eventos())) {
            EventoSumula evento = new EventoSumula();
            evento.setTipo(item.tipo());
            evento.setMinuto(item.minuto());
            if (item.tipo() == TipoEventoSumula.GOL_CONTRA) {
                evento.setAdversario(adversario);
            } else {
                evento.setParticipacao(participacaoPorAtleta.get(item.atletaId()));
            }
            resultado.add(evento);
        }
        return resultado;
    }

    private Contexto carregarContexto(Long partidaId, boolean exigirPro) {
        GestaoTimeAuthorizationService.ContextoAcesso acesso = exigirPro
                ? authorizationService.exigirAcessoPro()
                : authorizationService.consultarContexto();
        Usuario usuario = acesso.usuario();
        Time time = acesso.time();
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida nao encontrada."));
        authorizationService.exigirAcessoAoRecurso(validator.pertenceAPartida(partida, time), "Jogo");
        return new Contexto(usuario, time, partida, acesso.acessoPro());
    }

    private boolean placarInformado(Partida partida) {
        return partida.getStatusPlacar() != null && partida.getStatusPlacar() != StatusPlacar.PENDENTE;
    }

    private GestaoPartidaDTO converter(GestaoPartida gestao) {
        List<GestaoPartidaDTO.ParticipacaoDTO> participacoes = gestao.getParticipacoes().stream()
                .map(item -> {
                    return new GestaoPartidaDTO.ParticipacaoDTO(
                            item.getId(), item.getAtleta().getId(), item.getAtleta().getNome(), item.getPapel(),
                            item.getNumeroCamisa(), item.getPosicao(), item.getSlotTatico(),
                            item.getCoordenadaX(), item.getCoordenadaY(), item.getOrdem());
                })
                .toList();
        List<GestaoPartidaDTO.EventoDTO> eventos = gestao.getEventos().stream()
                .map(item -> new GestaoPartidaDTO.EventoDTO(
                        item.getId(),
                        item.getParticipacao() == null ? null : item.getParticipacao().getAtleta().getId(),
                        item.getTipo(), item.getMinuto()))
                .toList();
        return new GestaoPartidaDTO(
                gestao.getId(), gestao.getPartida().getId(), gestao.getTime().getId(), gestao.getStatus(),
                gestao.getEtapa(), gestao.getFormacao(), gestao.getFormacaoPersonalizada(), gestao.getVersao(),
                gestao.getDataAlteracao(), gestao.getDataPublicacao(),
                gestao.getPublicadoPor() == null ? null : gestao.getPublicadoPor().getNome(), participacoes, eventos);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : valores;
    }

    private record Contexto(Usuario usuario, Time time, Partida partida, boolean acessoPro) {
    }
}
