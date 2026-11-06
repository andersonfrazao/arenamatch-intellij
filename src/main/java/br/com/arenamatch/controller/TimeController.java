package br.com.arenamatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.PaginaJogosRealizadosDTO;
import br.com.arenamatch.dto.AdversarioDetalhesDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.service.TimeService;

@RestController
@RequestMapping("/api/times") // ISSO AQUI PRECISA BATER COM O CLIENT!
public class TimeController {

	@Autowired
    private TimeService timeService;

    @GetMapping("/buscar-por-nome")
    public ResponseEntity<List<TimeSimplesDTO>> buscarTimesPorNome(@RequestParam String nome) {
    		
    	var dtos = timeService.buscarTimesPorNome(nome);
                
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/ranking")
    public ResponseEntity<List<TimeDTO>> buscarRanking(@RequestParam(defaultValue = "FUTEBOL_CAMPO") br.com.arenamatch.enums.Modalidade modalidade) {
        List<TimeDTO> ranking = timeService.buscarRankingGeral(modalidade);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/scout")
    public ResponseEntity<TimeDTO> buscarScout(@RequestParam(defaultValue = "FUTEBOL_CAMPO") br.com.arenamatch.enums.Modalidade modalidade) {
        return ResponseEntity.ok(timeService.buscarScoutDoUsuarioAutenticado(modalidade));
    }

    @GetMapping("/scout/jogos")
    public ResponseEntity<PaginaJogosRealizadosDTO> buscarJogosRealizadosDoScout(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "FUTEBOL_CAMPO") br.com.arenamatch.enums.Modalidade modalidade) {
        return ResponseEntity.ok(timeService.buscarJogosRealizadosDoUsuarioAutenticado(pagina, modalidade));
    }

    @GetMapping("/{id}/adversario-detalhes")
    public ResponseEntity<AdversarioDetalhesDTO> buscarDetalhesAdversario(@PathVariable Long id,
            @RequestParam(defaultValue = "FUTEBOL_CAMPO") br.com.arenamatch.enums.Modalidade modalidade) {
        return ResponseEntity.ok(timeService.buscarDetalhesAdversario(id, modalidade));
    }
}
