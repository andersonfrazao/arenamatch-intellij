package br.com.arenamatch.dto;

import java.time.LocalDateTime;

public record DisponibilidadeGestaoPartidaDTO(
        boolean atalhoVisivel,
        boolean acessoPro,
        boolean editavel,
        boolean placarInformado,
        boolean pendenteConclusao,
        LocalDateTime liberadaEm,
        String mensagem) {
}
