package br.com.arenamatch.controller;

import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.OpcoesPartidaExternaDTO;
import br.com.arenamatch.dto.RegistroPartidaExternaDTO;
import br.com.arenamatch.service.PartidaExternaService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidas-externas")
public class PartidaExternaController {
    private final PartidaExternaService service;

    public PartidaExternaController(PartidaExternaService service) {
        this.service = service;
    }

    @GetMapping("/opcoes")
    public OpcoesPartidaExternaDTO buscarOpcoes() {
        return service.buscarOpcoes();
    }

    @PostMapping
    public ResponseEntity<EventoAgendaDTO> registrar(@RequestBody RegistroPartidaExternaDTO dto) {
        EventoAgendaDTO evento = service.registrar(dto);
        return ResponseEntity.created(URI.create("/api/partidas-externas/" + evento.getIdPartidaExterna())).body(evento);
    }

    @GetMapping("/{id}")
    public EventoAgendaDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
