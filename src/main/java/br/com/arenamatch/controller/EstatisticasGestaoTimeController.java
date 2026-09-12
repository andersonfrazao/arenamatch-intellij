package br.com.arenamatch.controller;

import br.com.arenamatch.dto.DetalheEstatisticaJogadorDTO;
import br.com.arenamatch.dto.PainelEstatisticasJogadoresDTO;
import br.com.arenamatch.dto.ResumoEstatisticasTimeDTO;
import br.com.arenamatch.service.EstatisticasGestaoTimeService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestao-time/estatisticas")
public class EstatisticasGestaoTimeController {
    private final EstatisticasGestaoTimeService service;

    public EstatisticasGestaoTimeController(EstatisticasGestaoTimeService service) {
        this.service = service;
    }

    @GetMapping("/time")
    public ResponseEntity<ResumoEstatisticasTimeDTO> resumirTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.resumirTime(inicio, fim));
    }

    @GetMapping("/anos")
    public ResponseEntity<List<Integer>> listarAnos() {
        return ResponseEntity.ok(service.listarAnosDisponiveis());
    }

    @GetMapping("/jogadores")
    public ResponseEntity<PainelEstatisticasJogadoresDTO> resumirJogadores(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "") String busca,
            @RequestParam(defaultValue = "nome") String ordenacao) {
        return ResponseEntity.ok(service.resumirJogadores(inicio, fim, busca, ordenacao));
    }

    @GetMapping("/jogadores/{atletaId}")
    public ResponseEntity<DetalheEstatisticaJogadorDTO> detalharJogador(
            @PathVariable Long atletaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(service.detalharJogador(atletaId, inicio, fim, pagina));
    }
}
