package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Modalidade {
    FUTEBOL_CAMPO("Futebol de Campo"),
    FUTSAL("Futsal"),
    FUT_7("Fut 7"),
    FUTEBOL_SOCIETY("Futebol Society");

    private final String descricao;
}
