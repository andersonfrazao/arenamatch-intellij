package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.repository.PartidaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConviteExpiracaoService {

    private final PartidaRepository partidaRepository;

    public ConviteExpiracaoService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    @Transactional
    public void expirarConvitesAutomaticamente() {
        expirarConvitesAnterioresA(LocalDate.now());
    }

    void expirarConvitesAnterioresA(LocalDate dataLimite) {
        List<Partida> convites = partidaRepository
                .buscarConvitesPendentesAnterioresA(dataLimite.plusDays(1).atStartOfDay());
        convites.forEach(convite -> convite.setStatus(StatusPartida.EXPIRADO));
        partidaRepository.saveAll(convites);
    }
}
