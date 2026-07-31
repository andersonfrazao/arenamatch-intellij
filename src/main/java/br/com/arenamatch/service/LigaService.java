package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.CandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.dto.ResultadoCandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.ScoutLigaDTO;
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
    private final PartidaLigaService partidaLigaService;
    private final PublicacaoLigaService publicacaoLigaService;
    private final RankingLigaService rankingLigaService;
    private final ScoutLigaService scoutLigaService;
    private final BanimentoLigaService banimentoLigaService;

    public LigaService(
            LigaRepository ligaRepository,
            TimeRepository timeRepository,
            PlacarPendenteService placarPendenteService,
            LigaMapper ligaMapper,
            ConviteLigaService conviteLigaService,
            LigaConsultaService ligaConsultaService,
            SolicitacaoEntradaLigaService solicitacaoEntradaLigaService,
            PartidaLigaService partidaLigaService,
            PublicacaoLigaService publicacaoLigaService,
            RankingLigaService rankingLigaService,
            ScoutLigaService scoutLigaService,
            BanimentoLigaService banimentoLigaService) {
        this.ligaRepository = ligaRepository;
        this.timeRepository = timeRepository;
        this.placarPendenteService = placarPendenteService;
        this.ligaMapper = ligaMapper;
        this.conviteLigaService = conviteLigaService;
        this.ligaConsultaService = ligaConsultaService;
        this.solicitacaoEntradaLigaService = solicitacaoEntradaLigaService;
        this.partidaLigaService = partidaLigaService;
        this.publicacaoLigaService = publicacaoLigaService;
        this.rankingLigaService = rankingLigaService;
        this.scoutLigaService = scoutLigaService;
        this.banimentoLigaService = banimentoLigaService;
    }

    @Transactional
    public LigaDetalheDTO criarLiga(Long idTimeAdmin, String nome, String descricao) {
        Time admin = timeRepository.findById(idTimeAdmin)
                .orElseThrow(() -> new RuntimeException("Time responsável pela liga não encontrado."));

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
    public void removerMembro(Long idLiga, Long idTime, Long idTimeSolicitante) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));

        Time membro = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time não encontrado."));

        if (idTimeSolicitante == null || !liga.getAdmin().getId().equals(idTimeSolicitante)) {
            throw new RuntimeException("Apenas o responsavel pela liga pode remover membros.");
        }

        if (liga.getAdmin().getId().equals(idTime)) {
            throw new RuntimeException("O responsável pela liga não pode ser removido.");
        }

        liga.getTimes().removeIf(time -> idTime.equals(time.getId()));
        ligaRepository.save(liga);
    }

    public BanimentoLigaDTO banirMembro(Long idLiga, Long idTime, Long idTimeAdmin, String motivo) {
        return banimentoLigaService.banirTime(idLiga, idTime, idTimeAdmin, motivo);
    }

    public void reverterBanimento(Long idLiga, Long idTime, Long idTimeAdmin) {
        banimentoLigaService.reverterBanimento(idLiga, idTime, idTimeAdmin);
    }

    public List<BanimentoLigaDTO> listarBanimentosAtivos(Long idLiga) {
        return banimentoLigaService.listarBanimentosAtivos(idLiga);
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

    public List<PartidaDTO> listarJogosDaLiga(Long idLiga) {
        return partidaLigaService.listarJogosDaLiga(idLiga);
    }

    public List<JogoRecenteLigaDTO> listarJogosRecentesDoMural(int limite) {
        return partidaLigaService.listarJogosRecentesDoMural(limite);
    }

    public PublicacaoLigaDTO criarPublicacao(NovaPublicacaoLigaDTO dto) {
        return publicacaoLigaService.criarPublicacao(dto);
    }

    public List<PublicacaoLigaDTO> listarPublicacoesDaLiga(Long idLiga) {
        return publicacaoLigaService.listarPublicacoesDaLiga(idLiga);
    }

    public List<PublicacaoLigaDTO> listarPublicacoesGlobais(Long meuTimeId) {
        return publicacaoLigaService.listarPublicacoesGlobais(meuTimeId);
    }

    public ResultadoCandidaturaPublicacaoLigaDTO candidatarPublicacao(
            Long idPublicacao,
            CandidaturaPublicacaoLigaDTO dto) {
        return publicacaoLigaService.candidatar(idPublicacao, dto);
    }

    public void cancelarPublicacao(Long idPublicacao, Long idTimeSolicitante) {
        publicacaoLigaService.cancelarPublicacao(idPublicacao, idTimeSolicitante);
    }

    public List<RankingLigaDTO> calcularRankingDaLiga(Long idLiga) {
        return rankingLigaService.calcularRanking(idLiga);
    }

    public ScoutLigaDTO buscarScoutDaLiga(Long idLiga, Long idTime) {
        return scoutLigaService.buscarScout(idLiga, idTime);
    }
}
