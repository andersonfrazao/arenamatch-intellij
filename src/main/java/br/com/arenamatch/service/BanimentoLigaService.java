package br.com.arenamatch.service;

import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.entity.BanimentoLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.BanimentoLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BanimentoLigaService {

    private final BanimentoLigaRepository banimentoLigaRepository;
    private final LigaRepository ligaRepository;
    private final TimeRepository timeRepository;
    private final BanimentoLigaMapper banimentoLigaMapper;

    public BanimentoLigaService(
            BanimentoLigaRepository banimentoLigaRepository,
            LigaRepository ligaRepository,
            TimeRepository timeRepository,
            BanimentoLigaMapper banimentoLigaMapper) {
        this.banimentoLigaRepository = banimentoLigaRepository;
        this.ligaRepository = ligaRepository;
        this.timeRepository = timeRepository;
        this.banimentoLigaMapper = banimentoLigaMapper;
    }

    @Transactional
    public BanimentoLigaDTO banirTime(Long idLiga, Long idTime, Long idTimeAdmin, String motivo) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));
        Time timeBanido = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time nao encontrado."));
        Time admin = timeRepository.findById(idTimeAdmin)
                .orElseThrow(() -> new RuntimeException("Time admin nao encontrado."));

        validarAdmin(liga, idTimeAdmin, "Apenas o responsavel pela liga pode banir membros.");
        if (liga.getAdmin().getId().equals(idTime)) {
            throw new RuntimeException("O responsavel pela liga nao pode ser banido.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("Informe o motivo do banimento.");
        }
        if (banimentoLigaRepository.existsByLigaIdAndTimeBanidoIdAndAtivoTrue(idLiga, idTime)) {
            throw new RuntimeException("Este time ja esta banido desta liga.");
        }

        liga.getTimes().removeIf(time -> idTime.equals(time.getId()));
        ligaRepository.save(liga);

        BanimentoLiga banimento = new BanimentoLiga();
        banimento.setLiga(liga);
        banimento.setTimeBanido(timeBanido);
        banimento.setAdmin(admin);
        banimento.setMotivo(motivo.trim());
        banimento.setDataBanimento(LocalDateTime.now());
        banimento.setAtivo(true);
        return banimentoLigaMapper.toDTO(banimentoLigaRepository.save(banimento));
    }

    @Transactional
    public void reverterBanimento(Long idLiga, Long idTime, Long idTimeAdmin) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));
        validarAdmin(liga, idTimeAdmin, "Apenas o responsavel pela liga pode reverter banimentos.");

        BanimentoLiga banimento = banimentoLigaRepository.findByLigaIdAndTimeBanidoIdAndAtivoTrue(idLiga, idTime)
                .orElseThrow(() -> new RuntimeException("Banimento ativo nao encontrado."));
        banimento.setAtivo(false);
        banimentoLigaRepository.save(banimento);
    }

    @Transactional(readOnly = true)
    public List<BanimentoLigaDTO> listarBanimentosAtivos(Long idLiga) {
        if (!ligaRepository.existsById(idLiga)) {
            throw new RuntimeException("Liga nao encontrada.");
        }

        return banimentoLigaRepository.findByLigaIdAndAtivoTrueOrderByDataBanimentoDesc(idLiga).stream()
                .map(banimentoLigaMapper::toDTO)
                .toList();
    }

    public void validarTimeNaoBanido(Long idLiga, Long idTime) {
        if (banimentoLigaRepository.existsByLigaIdAndTimeBanidoIdAndAtivoTrue(idLiga, idTime)) {
            throw new RuntimeException("Este time esta banido desta liga.");
        }
    }

    private void validarAdmin(Liga liga, Long idTimeAdmin, String mensagem) {
        if (idTimeAdmin == null || liga.getAdmin() == null || !liga.getAdmin().getId().equals(idTimeAdmin)) {
            throw new RuntimeException(mensagem);
        }
    }
}
