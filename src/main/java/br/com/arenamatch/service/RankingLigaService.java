package br.com.arenamatch.service;

import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingLigaService {

    private final LigaRepository ligaRepository;
    private final PartidaLigaRepository partidaLigaRepository;

    public RankingLigaService(LigaRepository ligaRepository, PartidaLigaRepository partidaLigaRepository) {
        this.ligaRepository = ligaRepository;
        this.partidaLigaRepository = partidaLigaRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingLigaDTO> calcularRanking(Long ligaId) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));

        Map<Long, RankingLigaDTO> ranking = new LinkedHashMap<>();
        liga.getTimes().forEach(time -> ranking.put(time.getId(), criarLinha(time)));

        partidaLigaRepository.buscarPorLiga(ligaId).stream()
                .filter(PartidaLiga::isContaRankingLiga)
                .map(PartidaLiga::getPartida)
                .filter(partida -> StatusPlacar.CONFIRMADO.equals(partida.getStatusPlacar()))
                .forEach(partida -> computarPartida(ranking, partida));

        ranking.values().forEach(this::calcularAproveitamento);

        return ranking.values().stream()
                .sorted(Comparator
                        .comparing(RankingLigaDTO::getPontos).reversed()
                        .thenComparing(RankingLigaDTO::getVitorias, Comparator.reverseOrder())
                        .thenComparing(RankingLigaDTO::getSaldoGols, Comparator.reverseOrder())
                        .thenComparing(RankingLigaDTO::getGolsPro, Comparator.reverseOrder())
                        .thenComparing(RankingLigaDTO::getNomeTime))
                .toList();
    }

    private RankingLigaDTO criarLinha(Time time) {
        RankingLigaDTO dto = new RankingLigaDTO();
        dto.setIdTime(time.getId());
        dto.setNomeTime(time.getNome());
        return dto;
    }

    private void computarPartida(Map<Long, RankingLigaDTO> ranking, Partida partida) {
        RankingLigaDTO mandante = ranking.get(partida.getMandante().getId());
        RankingLigaDTO visitante = ranking.get(partida.getVisitante().getId());

        if (mandante == null || visitante == null
                || partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
            return;
        }

        registrarResultado(mandante, partida.getGolsMandante(), partida.getGolsVisitante());
        registrarResultado(visitante, partida.getGolsVisitante(), partida.getGolsMandante());
    }

    private void registrarResultado(RankingLigaDTO linha, int golsPro, int golsContra) {
        linha.setJogos(linha.getJogos() + 1);
        linha.setGolsPro(linha.getGolsPro() + golsPro);
        linha.setGolsContra(linha.getGolsContra() + golsContra);
        linha.setSaldoGols(linha.getGolsPro() - linha.getGolsContra());

        if (golsPro > golsContra) {
            linha.setVitorias(linha.getVitorias() + 1);
            linha.setPontos(linha.getPontos() + 3);
        } else if (golsPro == golsContra) {
            linha.setEmpates(linha.getEmpates() + 1);
            linha.setPontos(linha.getPontos() + 1);
        } else {
            linha.setDerrotas(linha.getDerrotas() + 1);
        }
    }

    private void calcularAproveitamento(RankingLigaDTO linha) {
        if (linha.getJogos() == 0) {
            linha.setAproveitamento(0.0);
            return;
        }

        double maxPontos = linha.getJogos() * 3.0;
        linha.setAproveitamento((linha.getPontos() / maxPontos) * 100.0);
    }
}
