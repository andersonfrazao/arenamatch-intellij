package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ChatBloqueioPolicy {

    public boolean isEncerrada(Partida partida) {
        boolean placarConfirmado = partida.getStatusPlacar() != null
                && "CONFIRMADO".equals(partida.getStatusPlacar().name());
        boolean jogoCancelado = partida.getStatus() != null
                && "CANCELADO".equals(partida.getStatus().name());
        boolean conviteExpirado = partida.getStatus() != null
                && "PENDENTE".equals(partida.getStatus().name())
                && partida.getDataHora() != null
                && partida.getDataHora().isBefore(LocalDateTime.now());

        return placarConfirmado || jogoCancelado || conviteExpirado;
    }

    public void validarEnvioPermitido(Partida partida) {
        if (isEncerrada(partida)) {
            throw new RuntimeException("Não é possível enviar mensagens. Esta partida já está encerrada ou o convite expirou.");
        }
    }
}
