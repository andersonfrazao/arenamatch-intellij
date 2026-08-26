package br.com.arenamatch.controller;

import br.com.arenamatch.dto.AtivacaoContaDTO;
import br.com.arenamatch.dto.ConfirmacaoAcessoAdminDTO;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import br.com.arenamatch.dto.RedefinirSenhaDTO;
import br.com.arenamatch.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/autenticacao")
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(service.realizarLogin(loginDTO));
    }

    @PostMapping("/admin/confirmar")
    public ResponseEntity<LoginResponseDTO> confirmarAcessoAdmin(@RequestBody ConfirmacaoAcessoAdminDTO dto) {
        return ResponseEntity.ok(service.confirmarLoginAdmin(dto.getDesafio(), dto.getCodigo()));
    }

    @PostMapping("/admin/reenviar")
    public ResponseEntity<Void> reenviarCodigoAcessoAdmin(@RequestBody String desafio) {
        service.reenviarCodigoAcessoAdmin(desafio);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperar-senha/solicitar")
    public ResponseEntity<Void> solicitarRecuperacao(@RequestBody String email) {
        service.solicitarCodigoRecuperacao(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperar-senha/redefinir")
    public ResponseEntity<Void> redefinirSenha(@RequestBody RedefinirSenhaDTO dto) {
        service.redefinirSenha(dto.getEmail(), dto.getCodigo(), dto.getNovaSenha());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ativacao/reenviar")
    public ResponseEntity<Void> reenviarCodigoAtivacao(@RequestBody String email) {
        service.solicitarCodigoAtivacao(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ativacao/confirmar")
    public ResponseEntity<Void> confirmarAtivacao(@RequestBody AtivacaoContaDTO dto) {
        service.ativarConta(dto.getEmail(), dto.getCodigo());
        return ResponseEntity.ok().build();
    }
}
