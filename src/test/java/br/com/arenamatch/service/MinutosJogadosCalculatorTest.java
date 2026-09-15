package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.entity.ParticipacaoPartida;
import br.com.arenamatch.entity.SubstituicaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinutosJogadosCalculatorTest {
    private final MinutosJogadosCalculator calculator = new MinutosJogadosCalculator();

    @Test
    void deveSomarIntervalosQuandoAtletaRetornaAoCampo() {
        GestaoPartida gestao = new GestaoPartida();
        gestao.setDuracaoMinutos(60);
        ParticipacaoPartida titular = participacao(1L, PapelParticipacao.TITULAR);
        ParticipacaoPartida reserva = participacao(2L, PapelParticipacao.RESERVA);
        SubstituicaoPartida primeira = troca(titular, reserva, 20, 0);
        SubstituicaoPartida segunda = troca(reserva, titular, 45, 1);
        gestao.substituirParticipacoes(List.of(titular, reserva));
        gestao.substituirSubstituicoes(List.of(primeira, segunda));

        assertEquals(35, calculator.calcular(gestao, titular));
        assertEquals(25, calculator.calcular(gestao, reserva));
    }

    @Test
    void deveCalcularPartidaDeSetentaMinutosComSaidaEReentrada() {
        GestaoPartida gestao = new GestaoPartida();
        gestao.setDuracaoMinutos(70);
        ParticipacaoPartida jogadorA = participacao(1L, PapelParticipacao.TITULAR);
        ParticipacaoPartida jogadorB = participacao(2L, PapelParticipacao.RESERVA);
        gestao.substituirParticipacoes(List.of(jogadorA, jogadorB));
        gestao.substituirSubstituicoes(List.of(
                troca(jogadorA, jogadorB, 35, 0),
                troca(jogadorB, jogadorA, 60, 1)));

        assertEquals(45, calculator.calcular(gestao, jogadorA));
        assertEquals(25, calculator.calcular(gestao, jogadorB));
    }

    @Test
    void deveManterMinutosIndisponiveisQuandoTrocaNaoTemMinuto() {
        GestaoPartida gestao = new GestaoPartida();
        gestao.setDuracaoMinutos(60);
        ParticipacaoPartida titular = participacao(1L, PapelParticipacao.TITULAR);
        ParticipacaoPartida reserva = participacao(2L, PapelParticipacao.RESERVA);
        gestao.substituirParticipacoes(List.of(titular, reserva));
        gestao.substituirSubstituicoes(List.of(troca(titular, reserva, null, 0)));

        assertNull(calculator.calcular(gestao, titular));
        assertNull(calculator.calcular(gestao, reserva));
    }

    @Test
    void deveRetornarZeroParaReservaQueNaoEntrou() {
        GestaoPartida gestao = new GestaoPartida();
        gestao.setDuracaoMinutos(50);
        ParticipacaoPartida reserva = participacao(2L, PapelParticipacao.RESERVA);
        gestao.substituirParticipacoes(List.of(reserva));

        assertEquals(0, calculator.calcular(gestao, reserva));
    }

    private ParticipacaoPartida participacao(Long atletaId, PapelParticipacao papel) {
        Atleta atleta = new Atleta(); atleta.setId(atletaId);
        ParticipacaoPartida participacao = new ParticipacaoPartida();
        participacao.setAtleta(atleta); participacao.setPapel(papel);
        return participacao;
    }

    private SubstituicaoPartida troca(ParticipacaoPartida saiu, ParticipacaoPartida entrou,
                                      Integer minuto, int ordem) {
        SubstituicaoPartida troca = new SubstituicaoPartida();
        troca.setParticipacaoSaiu(saiu); troca.setParticipacaoEntrou(entrou);
        troca.setMinuto(minuto); troca.setOrdem(ordem);
        return troca;
    }
}
