package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.CadastroResponsavelRequestDTO;
import br.com.arenamatch.dto.CadastroDisponibilidadeRequestDTO;
import br.com.arenamatch.dto.CadastroFinalizacaoRequestDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.service.CadastroFormularioService;
import br.com.arenamatch.service.CadastroEnderecoService;
import br.com.arenamatch.service.EscudoTimeService;
import java.io.IOException;
import br.com.arenamatch.service.CadastroService;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    @Autowired private CadastroService service;
    @Autowired private CadastroFormularioService formularioService;
    @Autowired private CadastroEnderecoService enderecoService;
    @Autowired private EscudoTimeService escudoTimeService;

    @PostMapping("/validacoes/responsavel")
    public ResponseEntity<Void> validarResponsavel(@RequestBody CadastroResponsavelRequestDTO request) {
        formularioService.validarAvancoResponsavel(request.getCadastro(), request.getConfirmarSenha(), request.isNovoCadastro());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validacoes/time")
    public ResponseEntity<Void> validarTime(@RequestBody CadastroDTO dto) {
        formularioService.validarAvancoTime(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disponibilidades")
    public ResponseEntity<DisponibilidadeDTO> criarDisponibilidade(@RequestBody CadastroDisponibilidadeRequestDTO request) {
        return ResponseEntity.ok(formularioService.criarDisponibilidade(request.getCadastro(), request.getAgenda(),
                request.getCategoria(), request.getDia(), request.getInicio(), request.getFim()));
    }

    @PostMapping("/validacoes/finalizacao")
    public ResponseEntity<Void> validarFinalizacao(@RequestBody CadastroFinalizacaoRequestDTO request) {
        formularioService.validarFinalizacao(request.getCadastro(), request.getAgenda(), request.isNovoCadastro());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/endereco/cep")
    public ResponseEntity<EnderecoDTO> buscarEnderecoPorCep(@RequestParam String cep) {
        return ResponseEntity.ok(enderecoService.buscarPorCep(cep));
    }

    @GetMapping("/endereco/coordenadas")
    public ResponseEntity<EnderecoDTO> buscarEnderecoPorCoordenadas(
            @RequestParam Double latitude, @RequestParam Double longitude) {
        return ResponseEntity.ok(enderecoService.buscarPorCoordenadas(latitude, longitude));
    }

    @PostMapping(value = "/escudo", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadEscudo(@RequestPart("arquivo") MultipartFile arquivo) throws IOException {
        return ResponseEntity.ok(escudoTimeService.salvar(arquivo.getOriginalFilename(), arquivo.getContentType(),
                arquivo.getSize(), arquivo.getInputStream()));
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody CadastroDTO dto) {
        try {
            service.criarConta(dto);
            return ResponseEntity.status(201).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{idUsuario}")
    public ResponseEntity<CadastroDTO> buscarDadosParaEdicao(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.buscarDadosParaEdicao(idUsuario));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<Void> atualizarConta(@PathVariable Long idUsuario, @RequestBody CadastroDTO dto) {
    	service.atualizarConta(idUsuario, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/minha-conta/desativar")
    public ResponseEntity<Void> desativarConta() {
        service.desativarContaAutenticada();
        return ResponseEntity.ok().build();
    }
}
