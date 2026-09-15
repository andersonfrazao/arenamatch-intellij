package br.com.arenamatch.service;

import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.entity.ParticipacaoPartida;
import br.com.arenamatch.entity.SubstituicaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MinutosJogadosCalculator {

    public Integer calcular(GestaoPartida gestao, ParticipacaoPartida participacao) {
        Integer duracao = gestao.getDuracaoMinutos();
        if (duracao == null || gestao.getSubstituicoes().stream().anyMatch(item -> item.getMinuto() == null)) {
            return null;
        }
        Long atletaId = participacao.getAtleta().getId();
        boolean emCampo = participacao.getPapel() == PapelParticipacao.TITULAR;
        int inicio = 0;
        List<int[]> intervalos = new ArrayList<>();
        for (SubstituicaoPartida troca : gestao.getSubstituicoes().stream()
                .sorted(Comparator.comparing(SubstituicaoPartida::getOrdem)).toList()) {
            if (Objects.equals(troca.getParticipacaoSaiu().getAtleta().getId(), atletaId) && emCampo) {
                intervalos.add(new int[]{inicio, troca.getMinuto()});
                emCampo = false;
            } else if (Objects.equals(troca.getParticipacaoEntrou().getAtleta().getId(), atletaId) && !emCampo) {
                inicio = troca.getMinuto();
                emCampo = true;
            }
        }
        if (emCampo) intervalos.add(new int[]{inicio, duracao});
        int total = 0;
        for (int[] intervalo : intervalos) {
            total += Math.max(0, intervalo[1] - intervalo[0]);
        }
        return total;
    }
}
