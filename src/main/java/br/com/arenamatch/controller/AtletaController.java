package br.com.arenamatch.controller;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.AtletaRequestDTO;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.service.AtletaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atletas")
public class AtletaController {
    private final AtletaService service;
    public AtletaController(AtletaService service) { this.service = service; }

    @GetMapping public List<AtletaDTO> listar() { return service.listar(); }
    @PostMapping public ResponseEntity<AtletaDTO> criar(@RequestBody AtletaRequestDTO request) {
        return ResponseEntity.ok(service.criar(request));
    }
    @PutMapping("/{id}") public ResponseEntity<AtletaDTO> atualizar(@PathVariable Long id,
            @RequestBody AtletaRequestDTO request) { return ResponseEntity.ok(service.atualizar(id, request)); }
    @PatchMapping("/{id}/situacao/{situacao}") public ResponseEntity<AtletaDTO> alterarSituacao(
            @PathVariable Long id, @PathVariable SituacaoAtleta situacao) {
        return ResponseEntity.ok(service.alterarSituacao(id, situacao));
    }
}
