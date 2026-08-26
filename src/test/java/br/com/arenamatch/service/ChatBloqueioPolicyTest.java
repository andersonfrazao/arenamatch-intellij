package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.enums.StatusPartida;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ChatBloqueioPolicyTest {
    private final ChatBloqueioPolicy policy = new ChatBloqueioPolicy();

    @Test
    void deveBloquearConvitePendenteAoChegarODiaDoJogo() {
        Partida partida = new Partida();
        partida.setStatus(StatusPartida.PENDENTE);
        partida.setDataHora(LocalDate.now().atTime(23, 59));
        assertTrue(policy.isEncerrada(partida));
    }

    @Test
    void deveManterChatAbertoAntesDoDiaDoJogo() {
        Partida partida = new Partida();
        partida.setStatus(StatusPartida.PENDENTE);
        partida.setDataHora(LocalDate.now().plusDays(1).atStartOfDay());
        assertFalse(policy.isEncerrada(partida));
    }

    @Test
    void deveBloquearConviteMarcadoComoExpirado() {
        Partida partida = new Partida();
        partida.setStatus(StatusPartida.EXPIRADO);
        assertTrue(policy.isEncerrada(partida));
    }
}
