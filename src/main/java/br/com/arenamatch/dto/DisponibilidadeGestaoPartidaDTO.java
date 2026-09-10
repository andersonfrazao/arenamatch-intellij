package br.com.arenamatch.dto;

import java.time.LocalDateTime;

public record DisponibilidadeGestaoPartidaDTO(
        boolean atalhoVisivel,
        boolean acessoPro,
        boolean editavel,
        boolean placarInformado,
        boolean placarConfirmado,
        boolean pendenteConclusao,
        LocalDateTime liberadaEm,
        String nomeTimeMandante,
        Integer golsMandante,
        Integer golsVisitante,
        String nomeTimeVisitante,
        String mensagem) {
}
