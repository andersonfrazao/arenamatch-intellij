package br.com.arenamatch.controller;

import br.com.arenamatch.dto.AcessoCampeonatoDTO;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.service.CampeonatoAuthorizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campeonatos")
public class CampeonatoAcessoController {
    private final CampeonatoAuthorizationService authorization;

    public CampeonatoAcessoController(CampeonatoAuthorizationService authorization) {
        this.authorization = authorization;
    }

    @GetMapping("/acesso")
    public AcessoCampeonatoDTO consultar() {
        Usuario usuario = authorization.usuarioAutenticado();
        boolean permitido = CampeonatoAuthorizationService.possuiPremiumPago(
                usuario.getPlanoAssinatura(), usuario.getStatusPagamento());
        return new AcessoCampeonatoDTO(permitido, permitido ? "Acesso PREMIUM ativo."
                : CampeonatoAuthorizationService.mensagemBloqueio(usuario.getPlanoAssinatura()));
    }

    @GetMapping("/administracao/acesso")
    public AcessoCampeonatoDTO consultarAdministracao() {
        authorization.exigirPremium();
        return new AcessoCampeonatoDTO(true, "Acesso PREMIUM ativo.");
    }
}
