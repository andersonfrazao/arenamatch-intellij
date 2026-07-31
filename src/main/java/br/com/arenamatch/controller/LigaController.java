package br.com.arenamatch.controller;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.CandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.dto.BanirTimeLigaDTO;
import br.com.arenamatch.dto.EnviarConviteLigaDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.NovaLigaDTO;
import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.dto.ResponderConviteLigaDTO;
import br.com.arenamatch.dto.ResultadoCandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.ScoutLigaDTO;
import br.com.arenamatch.service.LigaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ligas")
public class LigaController {

    private final LigaService ligaService;

    public LigaController(LigaService ligaService) {
        this.ligaService = ligaService;
    }

    @PostMapping
    public ResponseEntity<LigaDetalheDTO> criarLiga(@RequestBody NovaLigaDTO dto) {
        return ResponseEntity.ok(ligaService.criarLiga(dto.getIdTimeAdmin(), dto.getNome(), dto.getDescricao()));
    }

    @PostMapping("/convites")
    public ResponseEntity<Void> enviarConvite(@RequestBody EnviarConviteLigaDTO dto) {
        ligaService.enviarConvite(dto.getIdLiga(), dto.getIdTimeConvidado(), dto.getMensagem());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/convites/responder")
    public ResponseEntity<Void> responderConvite(@RequestBody ResponderConviteLigaDTO dto) {
        ligaService.responderConvite(dto.getIdConvite(), dto.isAceitar());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idLiga}/membros/{idTime}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Long idLiga,
            @PathVariable Long idTime,
            @RequestParam Long idTimeSolicitante) {
        ligaService.removerMembro(idLiga, idTime, idTimeSolicitante);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{idLiga}/banimentos")
    public ResponseEntity<BanimentoLigaDTO> banirMembro(
            @PathVariable Long idLiga,
            @RequestBody BanirTimeLigaDTO dto) {
        return ResponseEntity.ok(ligaService.banirMembro(idLiga, dto.getIdTime(), dto.getIdTimeAdmin(), dto.getMotivo()));
    }

    @GetMapping("/{idLiga}/banimentos/ativos")
    public ResponseEntity<List<BanimentoLigaDTO>> listarBanimentosAtivos(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.listarBanimentosAtivos(idLiga));
    }

    @DeleteMapping("/{idLiga}/banimentos/{idTime}")
    public ResponseEntity<Void> reverterBanimento(
            @PathVariable Long idLiga,
            @PathVariable Long idTime,
            @RequestParam Long idTimeAdmin) {
        ligaService.reverterBanimento(idLiga, idTime, idTimeAdmin);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LigaDetalheDTO> buscarLigaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ligaService.buscarLigaDetalhePorId(id));
    }

    @GetMapping("/{idLiga}/jogos")
    public ResponseEntity<List<PartidaDTO>> listarJogosDaLiga(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.listarJogosDaLiga(idLiga));
    }

    @PostMapping("/{idLiga}/publicacoes")
    public ResponseEntity<PublicacaoLigaDTO> criarPublicacao(
            @PathVariable Long idLiga,
            @RequestBody NovaPublicacaoLigaDTO dto) {
        dto.setIdLiga(idLiga);
        return ResponseEntity.ok(ligaService.criarPublicacao(dto));
    }

    @GetMapping("/{idLiga}/publicacoes")
    public ResponseEntity<List<PublicacaoLigaDTO>> listarPublicacoesDaLiga(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.listarPublicacoesDaLiga(idLiga));
    }

    @GetMapping("/mural/publicacoes")
    public ResponseEntity<List<PublicacaoLigaDTO>> listarPublicacoesGlobais(@RequestParam Long meuTimeId) {
        return ResponseEntity.ok(ligaService.listarPublicacoesGlobais(meuTimeId));
    }

    @GetMapping("/mural/jogos-recentes")
    public ResponseEntity<List<JogoRecenteLigaDTO>> listarJogosRecentesDoMural(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ligaService.listarJogosRecentesDoMural(limit));
    }

    @PostMapping("/mural/publicacoes")
    public ResponseEntity<PublicacaoLigaDTO> criarPublicacaoGlobal(@RequestBody NovaPublicacaoLigaDTO dto) {
        return ResponseEntity.ok(ligaService.criarPublicacao(dto));
    }

    @PostMapping("/mural/publicacoes/{idPublicacao}/candidatar")
    public ResponseEntity<ResultadoCandidaturaPublicacaoLigaDTO> candidatarPublicacao(
            @PathVariable Long idPublicacao,
            @RequestBody CandidaturaPublicacaoLigaDTO dto) {
        return ResponseEntity.ok(ligaService.candidatarPublicacao(idPublicacao, dto));
    }

    @GetMapping("/{idLiga}/ranking")
    public ResponseEntity<List<RankingLigaDTO>> calcularRankingDaLiga(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.calcularRankingDaLiga(idLiga));
    }

    @GetMapping("/{idLiga}/scout/time/{idTime}")
    public ResponseEntity<ScoutLigaDTO> buscarScoutDaLiga(
            @PathVariable Long idLiga,
            @PathVariable Long idTime) {
        return ResponseEntity.ok(ligaService.buscarScoutDaLiga(idLiga, idTime));
    }

    @DeleteMapping("/publicacoes/{idPublicacao}/time/{idTimeSolicitante}")
    public ResponseEntity<Void> cancelarPublicacao(
            @PathVariable Long idPublicacao,
            @PathVariable Long idTimeSolicitante) {
        ligaService.cancelarPublicacao(idPublicacao, idTimeSolicitante);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<LigaDetalheDTO>> buscarLigasDoTime(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarLigasDoTime(timeId));
    }

    @GetMapping("/convites/time/{timeId}")
    public ResponseEntity<List<ConviteLigaDTO>> buscarConvitesPendentes(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarConvitesPendentesDoTime(timeId));
    }

    @GetMapping("/convites/agenda/time/{timeId}")
    public ResponseEntity<List<ConviteLigaDTO>> buscarConvitesParaAgenda(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarConvitesParaAgenda(timeId));
    }

    @GetMapping("/{idLiga}/convites/pendentes/times")
    public ResponseEntity<List<Long>> buscarIdsTimesComConvitePendente(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.buscarIdsTimesComConvitePendente(idLiga));
    }

    @GetMapping("/explorar/top/{meuTimeId}")
    public ResponseEntity<List<LigaExplorarDTO>> listarLigasEmAlta(@PathVariable Long meuTimeId) {
        return ResponseEntity.ok(ligaService.listarLigasEmAlta(meuTimeId));
    }

    @GetMapping("/explorar/busca/{nomeBusca}/{meuTimeId}")
    public ResponseEntity<List<LigaExplorarDTO>> buscarLigasPorNome(
            @PathVariable String nomeBusca,
            @PathVariable Long meuTimeId) {
        return ResponseEntity.ok(ligaService.buscarLigasPorNome(nomeBusca, meuTimeId));
    }

    @PostMapping("/{idLiga}/solicitar-entrada/{meuTimeId}")
    public ResponseEntity<Void> solicitarEntradaNaLiga(
            @PathVariable Long idLiga,
            @PathVariable Long meuTimeId) {
        ligaService.solicitarEntradaNaLiga(idLiga, meuTimeId);
        return ResponseEntity.ok().build();
    }
}
