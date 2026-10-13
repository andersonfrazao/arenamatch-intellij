package br.com.arenamatch.controller;

import br.com.arenamatch.dto.CampeonatoDTO;
import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.service.CampeonatoService;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/campeonatos/administracao")
public class CampeonatoController {
    private final CampeonatoService service;
    public CampeonatoController(CampeonatoService service) { this.service = service; }
    @GetMapping public List<CampeonatoDTO> listar() { return service.listar(); }
    @GetMapping("/{id}") public CampeonatoDTO consultar(@PathVariable Long id) { return service.consultar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CampeonatoDTO criar(@RequestBody CampeonatoRequestDTO dto) { return service.criar(dto); }
    @PutMapping("/{id}") public CampeonatoDTO atualizar(@PathVariable Long id, @RequestBody CampeonatoRequestDTO dto) {
        return service.atualizar(id, dto);
    }
    @PatchMapping("/{id}/cancelar-rascunho")
    public CampeonatoDTO cancelar(@PathVariable Long id, @RequestParam Long versao) { return service.cancelarRascunho(id, versao); }
    @ExceptionHandler(OptimisticLockingFailureException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public String conflito() { return "O campeonato foi alterado. Atualize a página antes de editar."; }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> regra(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(), e.getReason() == null ? "Operação não permitida." : e.getReason()));
    }
}
