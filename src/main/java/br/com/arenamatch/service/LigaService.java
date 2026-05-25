package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LigaService {

    private final LigaRepository ligaRepository;
    private final TimeRepository timeRepository;
    private final PlacarPendenteService placarPendenteService;
    private final LigaMapper ligaMapper;
    private final ConviteLigaService conviteLigaService;
    private final LigaConsultaService ligaConsultaService;
    private final SolicitacaoEntradaLigaService solicitacaoEntradaLigaService;

    public LigaService(
            LigaRepository ligaRepository,
            TimeRepository timeRepository,
            PlacarPendenteService placarPendenteService,
            LigaMapper ligaMapper,
            ConviteLigaService conviteLigaService,
            LigaConsultaService ligaConsultaService,
            SolicitacaoEntradaLigaService solicitacaoEntradaLigaService) {
        this.ligaRepository = ligaRepository;
        this.timeRepository = timeRepository;
        this.placarPendenteService = placarPendenteService;
        this.ligaMapper = ligaMapper;
        this.conviteLigaService = conviteLigaService;
        this.ligaConsultaService = ligaConsultaService;
        this.solicitacaoEntradaLigaService = solicitacaoEntradaLigaService;
    }

    @Transactional
    public LigaDetalheDTO criarLiga(Long idTimeAdmin, String nome, String descricao) {
        Time admin = timeRepository.findById(idTimeAdmin)
                .orElseThrow(() -> new RuntimeException("Time administrador não encontrado."));

        placarPendenteService.validarSemPlacarPendente(admin.getId());

        Liga liga = new Liga();
        liga.setNome(nome);
        liga.setDescricao(descricao);
        liga.setDataCriacao(LocalDateTime.now());
        liga.setAdmin(admin);
        liga.getTimes().add(admin);

        return ligaMapper.toDetalheDTO(ligaRepository.save(liga));
    }

    public void enviarConvite(Long idLiga, Long idTimeConvidado, String mensagem) {
        conviteLigaService.enviarConvite(idLiga, idTimeConvidado, mensagem);
    }

    public void responderConvite(Long idConvite, boolean aceitar) {
        conviteLigaService.responderConvite(idConvite, aceitar);
    }

    public Liga buscarLigaPorId(Long id) {
        return ligaConsultaService.buscarLigaPorId(id);
    }

    public LigaDetalheDTO buscarLigaDetalhePorId(Long id) {
        return ligaConsultaService.buscarLigaDetalhePorId(id);
    }

    @Transactional
    public void removerMembro(Long idLiga, Long idTime) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));

        Time membro = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time não encontrado."));

        if (liga.getAdmin().getId().equals(idTime)) {
            throw new RuntimeException("O administrador não pode ser removido da liga.");
        }

        liga.getTimes().remove(membro);
        ligaRepository.save(liga);
    }

    public List<LigaDetalheDTO> buscarLigasDoTime(Long timeId) {
        return ligaConsultaService.buscarLigasDoTime(timeId);
    }

    public List<ConviteLigaDTO> buscarConvitesPendentesDoTime(Long timeId) {
        return ligaConsultaService.buscarConvitesPendentesDoTime(timeId);
    }

    public List<ConviteLigaDTO> buscarConvitesParaAgenda(Long timeId) {
        return ligaConsultaService.buscarConvitesParaAgenda(timeId);
    }

    public List<Long> buscarIdsTimesComConvitePendente(Long ligaId) {
        return conviteLigaService.buscarIdsTimesComConvitePendente(ligaId);
    }

    public List<LigaExplorarDTO> listarLigasEmAlta(Long meuTimeId) {
        return ligaConsultaService.listarLigasEmAlta(meuTimeId);
    }

    public List<LigaExplorarDTO> buscarLigasPorNome(String nomeBusca, Long meuTimeId) {
        return ligaConsultaService.buscarLigasPorNome(nomeBusca, meuTimeId);
    }

    public void solicitarEntradaNaLiga(Long idLiga, Long meuTimeId) {
        solicitacaoEntradaLigaService.solicitarEntradaNaLiga(idLiga, meuTimeId);
    }
}
