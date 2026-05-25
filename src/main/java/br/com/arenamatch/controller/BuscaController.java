package br.com.arenamatch.controller;

import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.service.BuscaService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    private final BuscaService service;

    public BuscaController(BuscaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<List<TimeResumoDTO>> buscar(@RequestBody FiltroBuscaDTO filtro) {
        return ResponseEntity.ok(service.buscar(filtro));
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeResumoDTO>> buscarAdversarios(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(value = "raio", defaultValue = "20") Double raio,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "liga", required = false) String liga,
            @RequestParam("idMeuTime") Long idMeuTime,
            @RequestParam(value = "categoria", required = false) Categoria categoria) {
        return ResponseEntity.ok(service.buscarTimesDisponiveis(data, raio, cidade, nome, idMeuTime, categoria));
    }
}
