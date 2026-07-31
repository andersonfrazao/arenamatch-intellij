package br.com.arenamatch.service;

import br.com.arenamatch.dto.ScoutLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoutLigaService {

    private final LigaRepository ligaRepository;
    private final PartidaLigaRepository partidaLigaRepository;
    private final PartidaMapper partidaMapper;

    public ScoutLigaService(
            LigaRepository ligaRepository,
            PartidaLigaRepository partidaLigaRepository,
            PartidaMapper partidaMapper) {
        this.ligaRepository = ligaRepository;
        this.partidaLigaRepository = partidaLigaRepository;
        this.partidaMapper = partidaMapper;
    }

    @Transactional(readOnly = true)
    public ScoutLigaDTO buscarScout(Long ligaId, Long idTime) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));

        Time time = liga.getTimes().stream()
                .filter(membro -> membro.getId().equals(idTime))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Time nao faz parte da liga."));

        ScoutLigaDTO scout = criarScout(time);
        List<Partida> partidasConfirmadas = partidaLigaRepository.buscarPorLiga(ligaId).stream()
                .filter(PartidaLiga::isContaRankingLiga)
                .map(PartidaLiga::getPartida)
                .filter(partida -> StatusPlacar.CONFIRMADO.equals(partida.getStatusPlacar()))
                .filter(partida -> participaDaPartida(partida, idTime))
                .toList();

        partidasConfirmadas.forEach(partida -> registrarPartida(scout, partida, idTime));
        calcularAproveitamento(scout);
        scout.setUltimosJogos(partidasConfirmadas.stream()
                .sorted(Comparator.comparing(Partida::getDataHora, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(partidaMapper::toDTO)
                .toList());

        return scout;
    }

    private ScoutLigaDTO criarScout(Time time) {
        ScoutLigaDTO scout = new ScoutLigaDTO();
        scout.setIdTime(time.getId());
        scout.setNomeTime(time.getNome());
        return scout;
    }

    private boolean participaDaPartida(Partida partida, Long idTime) {
        return partida.getMandante() != null
                && partida.getVisitante() != null
                && (partida.getMandante().getId().equals(idTime) || partida.getVisitante().getId().equals(idTime));
    }

    private void registrarPartida(ScoutLigaDTO scout, Partida partida, Long idTime) {
        if (partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
            return;
        }

        boolean mandante = partida.getMandante().getId().equals(idTime);
        int golsPro = mandante ? partida.getGolsMandante() : partida.getGolsVisitante();
        int golsContra = mandante ? partida.getGolsVisitante() : partida.getGolsMandante();

        scout.setJogos(scout.getJogos() + 1);
        scout.setGolsPro(scout.getGolsPro() + golsPro);
        scout.setGolsContra(scout.getGolsContra() + golsContra);
        scout.setSaldoGols(scout.getGolsPro() - scout.getGolsContra());

        if (golsPro > golsContra) {
            scout.setVitorias(scout.getVitorias() + 1);
        } else if (golsPro == golsContra) {
            scout.setEmpates(scout.getEmpates() + 1);
        } else {
            scout.setDerrotas(scout.getDerrotas() + 1);
        }
    }

    private void calcularAproveitamento(ScoutLigaDTO scout) {
        if (scout.getJogos() == 0) {
            scout.setAproveitamento(0.0);
            return;
        }

        int pontos = (scout.getVitorias() * 3) + scout.getEmpates();
        scout.setAproveitamento((pontos / (scout.getJogos() * 3.0)) * 100.0);
    }
}
