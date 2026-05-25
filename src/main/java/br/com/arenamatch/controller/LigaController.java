package br.com.arenamatch.controller;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.EnviarConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.NovaLigaDTO;
import br.com.arenamatch.dto.ResponderConviteLigaDTO;
import br.com.arenamatch.service.LigaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<Void> removerMembro(@PathVariable Long idLiga, @PathVariable Long idTime) {
        ligaService.removerMembro(idLiga, idTime);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LigaDetalheDTO> buscarLigaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ligaService.buscarLigaDetalhePorId(id));
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
