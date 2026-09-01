package br.com.arenamatch.controller;

import br.com.arenamatch.dto.DisponibilidadeGestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import br.com.arenamatch.service.GestaoPartidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestao-partidas")
public class GestaoPartidaController {

    private final GestaoPartidaService service;

    public GestaoPartidaController(GestaoPartidaService service) {
        this.service = service;
    }

    @GetMapping("/{partidaId}/disponibilidade")
    public ResponseEntity<DisponibilidadeGestaoPartidaDTO> consultarDisponibilidade(
            @PathVariable Long partidaId) {
        return ResponseEntity.ok(service.consultarDisponibilidade(partidaId));
    }

    @GetMapping("/{partidaId}")
    public ResponseEntity<GestaoPartidaDTO> buscar(@PathVariable Long partidaId) {
        GestaoPartidaDTO gestao = service.buscar(partidaId);
        return gestao == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(gestao);
    }

    @PutMapping("/{partidaId}/rascunho")
    public ResponseEntity<GestaoPartidaDTO> salvarRascunho(
            @PathVariable Long partidaId,
            @RequestBody GestaoPartidaRequestDTO request) {
        return ResponseEntity.ok(service.salvarRascunho(partidaId, request));
    }

    @PostMapping("/{partidaId}/publicar")
    public ResponseEntity<GestaoPartidaDTO> publicar(
            @PathVariable Long partidaId,
            @RequestBody GestaoPartidaRequestDTO request) {
        return ResponseEntity.ok(service.publicar(partidaId, request));
    }
}
