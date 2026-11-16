package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Situação de uma partida externa na agenda. Segue a mesma regra das partidas oficiais:
 * jogo futuro é "Agendado"; depois do horário de início, fica "Aguardando placar" até o resultado ser informado.
 */
@Getter
@AllArgsConstructor
public enum SituacaoPartidaExterna {
    AGENDADO("Agendado"),
    AGUARDANDO_PLACAR("Aguardando placar"),
    REALIZADO("Realizado");

    private final String descricao;
}
