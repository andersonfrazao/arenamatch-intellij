package br.com.arenamatch.service;

import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO.EventoRequestDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO.ParticipacaoRequestDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.enums.TipoEventoSumula;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GestaoPartidaValidator {

    public void validarRascunho(GestaoPartidaRequestDTO request) {
        if (request == null) {
            falha(HttpStatus.BAD_REQUEST, "Dados da gestao da partida nao informados.");
        }

        List<ParticipacaoRequestDTO> participacoes = lista(request.participacoes());
        List<EventoRequestDTO> eventos = lista(request.eventos());
        var substituicoes = lista(request.substituicoes());
        if (request.duracaoMinutos() != null
                && (request.duracaoMinutos() < 1 || request.duracaoMinutos() > 300)) {
            falha(HttpStatus.BAD_REQUEST, "Duracao da partida deve estar entre 1 e 300 minutos.");
        }
        Set<Long> atletas = new HashSet<>();
        Set<String> slotsTitulares = new HashSet<>();
        Map<Long, PapelParticipacao> papeis = new HashMap<>();

        for (ParticipacaoRequestDTO participacao : participacoes) {
            if (participacao == null || participacao.atletaId() == null || participacao.papel() == null) {
                falha(HttpStatus.BAD_REQUEST, "Atleta e papel sao obrigatorios na participacao.");
            }
            if (!atletas.add(participacao.atletaId())) {
                falha(HttpStatus.BAD_REQUEST, "O mesmo atleta nao pode aparecer duas vezes na escalação.");
            }
            papeis.put(participacao.atletaId(), participacao.papel());
            if (participacao.numeroCamisa() != null
                    && (participacao.numeroCamisa() < 1 || participacao.numeroCamisa() > 99)) {
                falha(HttpStatus.BAD_REQUEST, "Numero da camisa deve estar entre 1 e 99.");
            }
            validarCoordenada(participacao.coordenadaX(), "X");
            validarCoordenada(participacao.coordenadaY(), "Y");
            if (participacao.papel() == PapelParticipacao.TITULAR
                    && textoPresente(participacao.slotTatico())
                    && !slotsTitulares.add(participacao.slotTatico().trim())) {
                falha(HttpStatus.BAD_REQUEST, "Duas pessoas nao podem ocupar a mesma posicao tatica.");
            }
        }

        for (var substituicao : substituicoes) {
            if (substituicao == null || substituicao.ordem() == null) {
                falha(HttpStatus.BAD_REQUEST, "Atletas e ordem sao obrigatorios na substituicao.");
            }
        }
        Set<Integer> ordens = new HashSet<>();
        Integer ultimoMinuto = null;
        for (var substituicao : substituicoes.stream()
                .sorted(java.util.Comparator.comparing(item -> item.ordem() == null ? Integer.MAX_VALUE : item.ordem()))
                .toList()) {
            if (substituicao.atletaSaiuId() == null || substituicao.atletaEntrouId() == null) {
                falha(HttpStatus.BAD_REQUEST, "Atletas e ordem sao obrigatorios na substituicao.");
            }
            if (Objects.equals(substituicao.atletaSaiuId(), substituicao.atletaEntrouId())) {
                falha(HttpStatus.BAD_REQUEST, "Os atletas da substituicao devem ser diferentes.");
            }
            if (!atletas.contains(substituicao.atletaSaiuId()) || !atletas.contains(substituicao.atletaEntrouId())) {
                falha(HttpStatus.BAD_REQUEST, "Substituicao exige atletas participantes da partida.");
            }
            if (substituicao.ordem() < 0 || !ordens.add(substituicao.ordem())) {
                falha(HttpStatus.BAD_REQUEST, "A ordem das substituicoes deve ser unica e valida.");
            }
            if (papeis.get(substituicao.atletaSaiuId()) != PapelParticipacao.TITULAR
                    || papeis.get(substituicao.atletaEntrouId()) == PapelParticipacao.TITULAR) {
                falha(HttpStatus.BAD_REQUEST, "A substituicao deve trocar um atleta em campo por um reserva.");
            }
            Integer minuto = substituicao.minuto();
            int limite = request.duracaoMinutos() == null ? 300 : request.duracaoMinutos();
            if (minuto != null && (minuto < 0 || minuto > limite)) {
                falha(HttpStatus.BAD_REQUEST, "Minuto da substituicao fora da duracao da partida.");
            }
            if (minuto != null && ultimoMinuto != null && minuto < ultimoMinuto) {
                falha(HttpStatus.BAD_REQUEST, "Os minutos das substituicoes devem respeitar a ordem informada.");
            }
            if (minuto != null) ultimoMinuto = minuto;
            papeis.put(substituicao.atletaSaiuId(), PapelParticipacao.RESERVA);
            papeis.put(substituicao.atletaEntrouId(), PapelParticipacao.TITULAR);
        }

        for (EventoRequestDTO evento : eventos) {
            if (evento == null || evento.tipo() == null) {
                falha(HttpStatus.BAD_REQUEST, "Tipo do evento e obrigatorio.");
            }
            if (evento.tipo() == TipoEventoSumula.GOL_CONTRA) {
                if (evento.atletaId() != null) {
                    falha(HttpStatus.BAD_REQUEST, "Gol contra a favor nao deve apontar para atleta do proprio time.");
                }
            } else if (evento.atletaId() == null || !atletas.contains(evento.atletaId())) {
                falha(HttpStatus.BAD_REQUEST, "Evento comum exige atleta participante da partida.");
            }
        }
    }

    public void validarPartidaEditavel(Partida partida, Time time, LocalDateTime agora) {
        if (!pertenceAPartida(partida, time)) {
            falha(HttpStatus.FORBIDDEN, "A partida nao pertence ao time autenticado.");
        }
        if (!estadoPermiteGestao(partida)) {
            falha(HttpStatus.CONFLICT, "O estado atual da partida nao permite gestao.");
        }
        if (!estaLiberada(partida, agora)) {
            falha(HttpStatus.CONFLICT, "A prancheta sera liberada no horario marcado da partida.");
        }
    }

    public void validarPublicacao(Partida partida, Time time, GestaoPartidaRequestDTO request) {
        validarRascunho(request);
        if (lista(request.participacoes()).isEmpty()) {
            falha(HttpStatus.CONFLICT, "Informe ao menos um atleta participante antes de publicar.");
        }
        if (partida.getStatusPlacar() != StatusPlacar.CONFIRMADO) {
            falha(HttpStatus.CONFLICT, "A sumula so pode ser publicada com o placar confirmado.");
        }

        int golsRegistrados = (int) lista(request.eventos()).stream()
                .filter(evento -> evento.tipo() == TipoEventoSumula.GOL)
                .count();
        Integer golsDoTime = Objects.equals(partida.getMandante().getId(), time.getId())
                ? partida.getGolsMandante()
                : partida.getGolsVisitante();
        if (golsDoTime == null || golsRegistrados > golsDoTime) {
            falha(HttpStatus.CONFLICT,
                    "A quantidade de gols atribuida aos jogadores nao pode exceder o placar do time.");
        }
    }

    public boolean estaLiberada(Partida partida, LocalDateTime agora) {
        boolean horarioAtingido = partida.getDataHora() != null && !agora.isBefore(partida.getDataHora());
        boolean placarInformado = partida.getStatusPlacar() != null
                && partida.getStatusPlacar() != StatusPlacar.PENDENTE;
        return horarioAtingido || placarInformado;
    }

    public boolean pertenceAPartida(Partida partida, Time time) {
        return partida != null && time != null
                && ((partida.getMandante() != null && Objects.equals(partida.getMandante().getId(), time.getId()))
                || (partida.getVisitante() != null && Objects.equals(partida.getVisitante().getId(), time.getId())));
    }

    public boolean estadoPermiteGestao(Partida partida) {
        return partida != null
                && (partida.getStatus() == StatusPartida.AGENDADO
                || partida.getStatus() == StatusPartida.FINALIZADO);
    }

    private void validarCoordenada(BigDecimal valor, String eixo) {
        if (valor != null && (valor.compareTo(BigDecimal.ZERO) < 0
                || valor.compareTo(BigDecimal.valueOf(100)) > 0)) {
            falha(HttpStatus.BAD_REQUEST, "Coordenada " + eixo + " deve estar entre 0 e 100.");
        }
    }

    private boolean textoPresente(String valor) {
        return valor != null && !valor.isBlank();
    }

    private <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : valores;
    }

    private void falha(HttpStatus status, String mensagem) {
        throw new ResponseStatusException(status, mensagem);
    }
}
