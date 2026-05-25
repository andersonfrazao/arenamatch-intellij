package br.com.arenamatch.service;

import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoEntradaLigaService {

    private final LigaRepository ligaRepository;
    private final ConviteLigaRepository conviteLigaRepository;
    private final PlacarPendenteService placarPendenteService;

    public SolicitacaoEntradaLigaService(
            LigaRepository ligaRepository,
            ConviteLigaRepository conviteLigaRepository,
            PlacarPendenteService placarPendenteService) {
        this.ligaRepository = ligaRepository;
        this.conviteLigaRepository = conviteLigaRepository;
        this.placarPendenteService = placarPendenteService;
    }

    @Transactional
    public void solicitarEntradaNaLiga(Long idLiga, Long meuTimeId) {
        Liga liga = ligaRepository.findById(idLiga).orElseThrow();
        placarPendenteService.validarSemPlacarPendente(meuTimeId);

        boolean jaTemPedido = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(
                idLiga, meuTimeId, StatusConviteLiga.PENDENTE);
        if (jaTemPedido) {
            throw new RuntimeException("Você já enviou uma solicitação para esta liga.");
        }

        Time meuTime = new Time();
        meuTime.setId(meuTimeId);

        ConviteLiga convite = new ConviteLiga();
        convite.setLiga(liga);
        convite.setTimeConvidado(meuTime);
        convite.setStatus(StatusConviteLiga.PENDENTE);
        convite.setDataConvite(LocalDateTime.now());
        convite.setMensagem("Gostaria de participar da liga!");
        convite.setSolicitadoPeloTime(true);

        conviteLigaRepository.save(convite);
    }
}
