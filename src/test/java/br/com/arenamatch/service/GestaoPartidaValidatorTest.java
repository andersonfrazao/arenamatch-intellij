package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO.EventoRequestDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO.ParticipacaoRequestDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.enums.TipoEventoSumula;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GestaoPartidaValidatorTest {

    private GestaoPartidaValidator validator;
    private Time mandante;
    private Time visitante;
    private Partida partida;

    @BeforeEach
    void setUp() {
        validator = new GestaoPartidaValidator();
        mandante = time(1L);
        visitante = time(2L);
        partida = new Partida();
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setStatus(StatusPartida.AGENDADO);
        partida.setDataHora(LocalDateTime.of(2026, 8, 31, 20, 0));
        partida.setStatusPlacar(StatusPlacar.PENDENTE);
    }

    @Test
    void deveBloquearAtletaDuplicadoNaEscalacao() {
        var request = request(
                List.of(participacao(10L, "gk"), participacao(10L, "z1")),
                List.of());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarRascunho(request));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void deveBloquearDoisTitularesNoMesmoSlot() {
        var request = request(
                List.of(participacao(10L, "gk"), participacao(11L, "gk")),
                List.of());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarRascunho(request));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void deveExigirParticipacaoParaEventoComum() {
        var request = request(
                List.of(participacao(10L, "a1")),
                List.of(new EventoRequestDTO(99L, TipoEventoSumula.GOL, 15)));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarRascunho(request));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void deveAceitarGolContraSemAtletaDoProprioTime() {
        var request = request(
                List.of(participacao(10L, "a1")),
                List.of(new EventoRequestDTO(null, TipoEventoSumula.GOL_CONTRA, 22)));

        assertDoesNotThrow(() -> validator.validarRascunho(request));
    }

    @Test
    void deveLiberarNoHorarioExatoDaPartida() {
        assertFalse(validator.estaLiberada(partida, LocalDateTime.of(2026, 8, 31, 19, 59, 59)));
        assertTrue(validator.estaLiberada(partida, LocalDateTime.of(2026, 8, 31, 20, 0)));
    }

    @Test
    void deveLiberarAntesDoHorarioQuandoPlacarJaFoiInformado() {
        partida.setStatusPlacar(StatusPlacar.AGUARDANDO_CONFIRMACAO);

        assertTrue(validator.estaLiberada(partida, LocalDateTime.of(2026, 8, 31, 18, 0)));
    }

    @Test
    void devePublicarQuandoGolsEContraFechamComPlacar() {
        partida.setStatusPlacar(StatusPlacar.CONFIRMADO);
        partida.setGolsMandante(2);
        partida.setGolsVisitante(0);
        var request = request(
                List.of(participacao(10L, "a1")),
                List.of(
                        new EventoRequestDTO(10L, TipoEventoSumula.GOL, 10),
                        new EventoRequestDTO(null, TipoEventoSumula.GOL_CONTRA, 55)));

        assertDoesNotThrow(() -> validator.validarPublicacao(partida, mandante, request));
    }

    @Test
    void deveBloquearPublicacaoQuandoGolsNaoFechamComPlacar() {
        partida.setStatusPlacar(StatusPlacar.CONFIRMADO);
        partida.setGolsMandante(2);
        partida.setGolsVisitante(0);
        var request = request(
                List.of(participacao(10L, "a1")),
                List.of(new EventoRequestDTO(10L, TipoEventoSumula.GOL, 10)));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarPublicacao(partida, mandante, request));

        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }

    @Test
    void deveBloquearTimeQueNaoParticipaDaPartida() {
        Time terceiro = time(3L);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarPartidaEditavel(
                        partida, terceiro, LocalDateTime.of(2026, 8, 31, 21, 0)));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
    }

    @Test
    void deveBloquearConvitePendenteMesmoAposOHorario() {
        partida.setStatus(StatusPartida.PENDENTE);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> validator.validarPartidaEditavel(
                        partida, mandante, LocalDateTime.of(2026, 8, 31, 21, 0)));

        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }

    private GestaoPartidaRequestDTO request(
            List<ParticipacaoRequestDTO> participacoes,
            List<EventoRequestDTO> eventos) {
        return new GestaoPartidaRequestDTO(
                null, EtapaGestaoPartida.ESCALACAO, "4-4-2", null, participacoes, eventos);
    }

    private ParticipacaoRequestDTO participacao(Long atletaId, String slot) {
        return new ParticipacaoRequestDTO(
                atletaId,
                PapelParticipacao.TITULAR,
                10,
                "Atacante",
                slot,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                0);
    }

    private Time time(Long id) {
        Time time = new Time();
        time.setId(id);
        return time;
    }
}
