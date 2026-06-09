package br.com.arenamatch.controller;

import br.com.arenamatch.dto.AdminParametroSistemaDTO;
import br.com.arenamatch.dto.AdminParametroSistemaEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioResumoDTO;
import br.com.arenamatch.service.AdminParametroSistemaService;
import br.com.arenamatch.service.AdminUsuarioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUsuarioService adminUsuarioService;
    private final AdminParametroSistemaService adminParametroSistemaService;

    public AdminController(AdminUsuarioService adminUsuarioService,
                           AdminParametroSistemaService adminParametroSistemaService) {
        this.adminUsuarioService = adminUsuarioService;
        this.adminParametroSistemaService = adminParametroSistemaService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioResumoDTO>> buscarUsuarios(@RequestParam("termo") String termo) {
        return ResponseEntity.ok(adminUsuarioService.buscarUsuarios(termo));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<AdminUsuarioEdicaoDTO> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminUsuarioService.buscarUsuario(id));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<AdminUsuarioEdicaoDTO> atualizarUsuario(@PathVariable Long id,
                                                                  @RequestBody AdminUsuarioEdicaoDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(adminUsuarioService.atualizarUsuario(dto));
    }

    @GetMapping("/parametros")
    public ResponseEntity<List<AdminParametroSistemaDTO>> listarParametros() {
        return ResponseEntity.ok(adminParametroSistemaService.listarParametros());
    }

    @PutMapping("/parametros/{chave}")
    public ResponseEntity<AdminParametroSistemaDTO> atualizarParametro(@PathVariable String chave,
                                                                       @RequestBody AdminParametroSistemaEdicaoDTO dto) {
        dto.setChave(chave);
        return ResponseEntity.ok(adminParametroSistemaService.atualizarParametro(dto));
    }
}
