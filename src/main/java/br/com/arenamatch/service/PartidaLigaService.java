package br.com.arenamatch.service;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartidaLigaService {

    private final LigaRepository ligaRepository;
    private final PartidaLigaRepository partidaLigaRepository;
    private final DesafioPartidaService desafioPartidaService;
    private final PartidaMapper partidaMapper;

    public PartidaLigaService(
            LigaRepository ligaRepository,
            PartidaLigaRepository partidaLigaRepository,
            DesafioPartidaService desafioPartidaService,
            PartidaMapper partidaMapper) {
        this.ligaRepository = ligaRepository;
        this.partidaLigaRepository = partidaLigaRepository;
        this.desafioPartidaService = desafioPartidaService;
        this.partidaMapper = partidaMapper;
    }

    @Transactional
    public PartidaLiga criarJogoDaLiga(Long ligaId, DesafioDTO desafio) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));

        validarTimesDaLiga(liga, desafio);

        Partida partida = desafioPartidaService.criarDesafio(desafio);
        return vincularPartida(liga, partida);
    }

    @Transactional
    public PartidaLiga vincularPartida(Long ligaId, Partida partida) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));

        return vincularPartida(liga, partida);
    }

    @Transactional(readOnly = true)
    public List<PartidaDTO> listarJogosDaLiga(Long ligaId) {
        if (!ligaRepository.existsById(ligaId)) {
            throw new RuntimeException("Liga nao encontrada.");
        }

        return partidaLigaRepository.buscarPorLiga(ligaId).stream()
                .map(PartidaLiga::getPartida)
                .map(partidaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JogoRecenteLigaDTO> listarJogosRecentesDoMural(int limite) {
        int limiteSeguro = limite > 0 ? limite : 10;
        return partidaLigaRepository.buscarJogosRecentes().stream()
                .limit(limiteSeguro)
                .map(this::toJogoRecenteDTO)
                .toList();
    }

    private PartidaLiga vincularPartida(Liga liga, Partida partida) {
        if (partida == null || partida.getId() == null) {
            throw new RuntimeException("Partida invalida para vinculo com liga.");
        }

        if (partidaLigaRepository.existsByPartidaId(partida.getId())) {
            throw new RuntimeException("Esta partida ja esta vinculada a uma liga.");
        }

        PartidaLiga partidaLiga = new PartidaLiga();
        partidaLiga.setLiga(liga);
        partidaLiga.setPartida(partida);
        partidaLiga.setDataVinculo(LocalDateTime.now());
        partidaLiga.setContaRankingLiga(true);

        return partidaLigaRepository.save(partidaLiga);
    }

    private JogoRecenteLigaDTO toJogoRecenteDTO(PartidaLiga partidaLiga) {
        PartidaDTO partida = partidaMapper.toDTO(partidaLiga.getPartida());
        JogoRecenteLigaDTO dto = new JogoRecenteLigaDTO();
        dto.setIdPartida(partida.getId());
        dto.setIdLiga(partidaLiga.getLiga().getId());
        dto.setNomeLiga(partidaLiga.getLiga().getNome());
        dto.setMandante(partida.getMandante());
        dto.setVisitante(partida.getVisitante());
        dto.setDataHora(partida.getDataHora());
        dto.setStatus(partida.getStatus());
        dto.setGolsMandante(partida.getGolsMandante());
        dto.setGolsVisitante(partida.getGolsVisitante());
        dto.setStatusPlacar(partida.getStatusPlacar());
        return dto;
    }

    private void validarTimesDaLiga(Liga liga, DesafioDTO desafio) {
        if (!ligaContemTime(liga, desafio.getIdTimeDesafiante())) {
            throw new RuntimeException("O time desafiante nao pertence a liga.");
        }

        if (!ligaContemTime(liga, desafio.getIdTimeDesafiado())) {
            throw new RuntimeException("O time desafiado nao pertence a liga.");
        }
    }

    private boolean ligaContemTime(Liga liga, Long timeId) {
        return timeId != null && liga.getTimes().stream()
                .anyMatch(time -> timeId.equals(time.getId()));
    }
}
