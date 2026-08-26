package br.com.arenamatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.dto.ResumoNotificacoesDTO;
import br.com.arenamatch.service.NotificacaoClassificacaoService;
import br.com.arenamatch.service.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private NotificacaoClassificacaoService notificacaoClassificacaoService;

    @GetMapping("/time/{idTime}")
    public ResponseEntity<List<NotificacaoDTO>> buscarNotificacoes(@PathVariable Long idTime) {
        // O Controller agora só chama o Service, respeitando a arquitetura!
        List<NotificacaoDTO> notificacoes = notificacaoService.buscarNotificacoes(idTime);
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/time/{idTime}/resumo")
    public ResponseEntity<ResumoNotificacoesDTO> buscarResumo(@PathVariable Long idTime) {
        return ResponseEntity.ok(notificacaoClassificacaoService.calcularResumo(
                notificacaoService.buscarNotificacoes(idTime)));
    }
}
