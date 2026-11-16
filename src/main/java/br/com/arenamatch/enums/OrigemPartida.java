package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Origem de uma partida do time.
 * <ul>
 *   <li>{@link #MATCH}: partida combinada pelo Arena Match, com os dois times cadastrados e o desafio aceito.
 *       Fica na tabela {@code partida}; não há coluna de origem, a própria tabela identifica o tipo.
 *       Entra no ranking oficial e no scout.</li>
 *   <li>{@link #MANUAL}: partida off-line, combinada fora do Arena Match e registrada pelo próprio time.
 *       Fica em {@code partida_externa.origem = 'MANUAL'}. Não entra no ranking, apenas no scout do time.</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum OrigemPartida {
    MATCH("Match"),
    MANUAL("Off-line");

    private final String descricao;
}
