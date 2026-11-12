package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MandoPartida {
    MANDANTE("Mandante"),
    VISITANTE("Visitante");

    private final String descricao;
}
