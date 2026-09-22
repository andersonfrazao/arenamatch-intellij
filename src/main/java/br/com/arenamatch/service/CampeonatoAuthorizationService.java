package br.com.arenamatch.service;

import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampeonatoAuthorizationService {
    private final UsuarioRepository usuarioRepository;

    public CampeonatoAuthorizationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public static boolean possuiPremiumPago(PlanoAssinatura plano, StatusPagamento pagamento) {
        return plano == PlanoAssinatura.PREMIUM && pagamento == StatusPagamento.PAGO;
    }

    public static String mensagemBloqueio(PlanoAssinatura plano) {
        return plano == PlanoAssinatura.PREMIUM
                ? "Regularize o pagamento do PREMIUM para organizar campeonatos."
                : "O plano PREMIUM permite criar e administrar campeonatos e inclui todos os recursos PRO.";
    }

    public Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }
        // Sempre consultar o cadastro: um token emitido antes do downgrade nao concede acesso.
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado nao encontrado."));
    }

    public Usuario exigirPremium() {
        Usuario usuario = usuarioAutenticado();
        if (!possuiPremiumPago(usuario.getPlanoAssinatura(), usuario.getStatusPagamento())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    mensagemBloqueio(usuario.getPlanoAssinatura()));
        }
        return usuario;
    }

    public Usuario exigirOrganizador(Long organizadorId) {
        Usuario usuario = exigirPremium();
        if (organizadorId == null || !Objects.equals(usuario.getId(), organizadorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campeonato nao encontrado.");
        }
        return usuario;
    }
}
