package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.enums.StatusPartida;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ChatBloqueioPolicy {

    public boolean isEncerrada(Partida partida) {
        boolean placarConfirmado = partida.getStatusPlacar() != null
                && "CONFIRMADO".equals(partida.getStatusPlacar().name());
        boolean jogoCancelado = partida.getStatus() != null
                && "CANCELADO".equals(partida.getStatus().name());
        boolean conviteExpirado = partida.getStatus() == StatusPartida.EXPIRADO
                || (partida.getStatus() == StatusPartida.PENDENTE
                && partida.getDataHora() != null
                && !partida.getDataHora().toLocalDate().isAfter(LocalDate.now()));

        return placarConfirmado || jogoCancelado || conviteExpirado;
    }

    public void validarEnvioPermitido(Partida partida) {
        if (isEncerrada(partida)) {
            throw new RuntimeException("Não é possível enviar mensagens. Esta partida já está encerrada ou o convite expirou.");
        }
    }
}
