package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LigaConsultaService {

    private final LigaRepository ligaRepository;
    private final ConviteLigaRepository conviteLigaRepository;
    private final PartidaLigaRepository partidaLigaRepository;
    private final LigaMapper ligaMapper;

    public LigaConsultaService(
            LigaRepository ligaRepository,
            ConviteLigaRepository conviteLigaRepository,
            PartidaLigaRepository partidaLigaRepository,
            LigaMapper ligaMapper) {
        this.ligaRepository = ligaRepository;
        this.conviteLigaRepository = conviteLigaRepository;
        this.partidaLigaRepository = partidaLigaRepository;
        this.ligaMapper = ligaMapper;
    }

    @Transactional(readOnly = true)
    public LigaDetalheDTO buscarLigaDetalhePorId(Long id) {
        return ligaMapper.toDetalheDTO(buscarLigaPorId(id));
    }

    @Transactional(readOnly = true)
    public Liga buscarLigaPorId(Long id) {
        return ligaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<LigaDetalheDTO> buscarLigasDoTime(Long timeId) {
        return ligaRepository.buscarLigasDoTime(timeId).stream()
                .map(ligaMapper::toDetalheDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConviteLigaDTO> buscarConvitesPendentesDoTime(Long timeId) {
        return conviteLigaRepository.findByTimeConvidadoIdAndStatus(timeId, StatusConviteLiga.PENDENTE).stream()
                .map(ligaMapper::toConvitePendenteDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConviteLigaDTO> buscarConvitesParaAgenda(Long timeId) {
        return conviteLigaRepository.findByTimeConvidadoIdAndStatusIn(
                        timeId, List.of(StatusConviteLiga.PENDENTE, StatusConviteLiga.RECUSADO)).stream()
                .map(ligaMapper::toConviteAgendaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LigaExplorarDTO> listarLigasEmAlta(Long meuTimeId) {
        List<Liga> ligas = ligaRepository.buscarLigasMaisMovimentadas().stream().limit(15).toList();
        return converterParaExplorarDTO(ligas, meuTimeId);
    }

    @Transactional(readOnly = true)
    public List<LigaExplorarDTO> buscarLigasPorNome(String nomeBusca, Long meuTimeId) {
        return converterParaExplorarDTO(ligaRepository.buscarLigasPorNome(nomeBusca), meuTimeId);
    }

    private List<LigaExplorarDTO> converterParaExplorarDTO(List<Liga> ligas, Long meuTimeId) {
        return ligas.stream()
                .map(liga -> {
                    boolean temPendente = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(
                            liga.getId(), meuTimeId, StatusConviteLiga.PENDENTE);
                    long publicacoesAbertas = ligaRepository.contarPublicacoesAbertas(liga.getId());
                    long jogos = partidaLigaRepository.countByLigaId(liga.getId());
                    return ligaMapper.toExplorarDTO(liga, meuTimeId, temPendente, publicacoesAbertas, jogos);
                })
                .toList();
    }
}
