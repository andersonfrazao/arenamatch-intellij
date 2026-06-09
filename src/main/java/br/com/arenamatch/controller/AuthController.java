package br.com.arenamatch.controller;

import br.com.arenamatch.dto.AtivacaoContaDTO;
import br.com.arenamatch.dto.ConfirmacaoAcessoAdminDTO;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import br.com.arenamatch.dto.RedefinirSenhaDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.repository.UsuarioRepository;
import br.com.arenamatch.service.AuthService;
import br.com.arenamatch.service.JwtService;
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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        UsuarioDTO usuario = service.autenticar(loginDTO);

        LoginResponseDTO response = new LoginResponseDTO();
        if (Perfil.ADMIN.equals(usuario.getPerfil())) {
            response.setRequerCodigoAdmin(true);
            response.setDesafioAdmin(service.iniciarAcessoAdmin(usuario.getEmail()));
            response.setEmailMascarado(service.mascararEmail(usuario.getEmail()));
            return ResponseEntity.ok(response);
        }

        var usuarioEntity = usuarioRepository.findByEmail(usuario.getEmail()).orElseThrow();
        response.setUsuario(usuario);
        response.setToken(jwtService.gerarToken(usuarioEntity));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/confirmar")
    public ResponseEntity<LoginResponseDTO> confirmarAcessoAdmin(@RequestBody ConfirmacaoAcessoAdminDTO dto) {
        UsuarioDTO usuario = service.confirmarAcessoAdmin(dto.getDesafio(), dto.getCodigo());
        var usuarioEntity = usuarioRepository.findByEmail(usuario.getEmail()).orElseThrow();

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsuario(usuario);
        response.setToken(jwtService.gerarToken(usuarioEntity));
        return ResponseEntity.ok(response);
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
