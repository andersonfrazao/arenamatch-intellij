package br.com.arenamatch.service;

import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GestaoTimeAuthorizationService {

    public static final String MENSAGEM_UPGRADE =
            "Assine o plano PRO para acessar a gestao do seu time!";
    public static final String MENSAGEM_REGULARIZACAO =
            "Regularize o pagamento do plano PRO para reativar a gestao do seu time.";

    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;

    public GestaoTimeAuthorizationService(UsuarioRepository usuarioRepository, TimeRepository timeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.timeRepository = timeRepository;
    }

    public ContextoAcesso consultarContexto() {
        Usuario usuario = usuarioAutenticado();
        Time time = timeRepository.findByResponsavel(usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Time do usuario autenticado nao encontrado."));
        return new ContextoAcesso(usuario, time, possuiProPago(usuario));
    }

    public ContextoAcesso exigirAcessoPro() {
        ContextoAcesso contexto = consultarContexto();
        if (!contexto.acessoPro()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    mensagemBloqueio(contexto.usuario().getPlanoAssinatura(),
                            contexto.usuario().getStatusPagamento()));
        }
        return contexto;
    }

    public void exigirRecursoDoTime(Long timeIdDoRecurso, Time timeAutenticado, String recurso) {
        exigirAcessoAoRecurso(timeAutenticado != null
                && Objects.equals(timeIdDoRecurso, timeAutenticado.getId()), recurso);
    }

    public void exigirAcessoAoRecurso(boolean permitido, String recurso) {
        if (!permitido) throw new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " nao encontrado.");
    }

    public boolean possuiProPago(Usuario usuario) {
        return usuario != null && possuiProPago(usuario.getPlanoAssinatura(), usuario.getStatusPagamento());
    }

    public boolean possuiProPago(UsuarioDTO usuario) {
        return usuario != null && possuiProPago(usuario.getPlanoAssinatura(), usuario.getStatusPagamento());
    }

    public boolean possuiProPago(PlanoAssinatura plano, StatusPagamento pagamento) {
        return plano == PlanoAssinatura.PRO && pagamento == StatusPagamento.PAGO;
    }

    public String mensagemBloqueio(PlanoAssinatura plano, StatusPagamento pagamento) {
        return precisaRegularizar(plano, pagamento)
                ? MENSAGEM_REGULARIZACAO : MENSAGEM_UPGRADE;
    }

    public boolean precisaRegularizar(PlanoAssinatura plano, StatusPagamento pagamento) {
        return plano == PlanoAssinatura.PRO && pagamento != StatusPagamento.PAGO;
    }

    private Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal == null || principal.toString().isBlank() || "anonymousUser".equals(principal.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }
        return usuarioRepository.findByEmail(principal.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado nao encontrado."));
    }

    public record ContextoAcesso(Usuario usuario, Time time, boolean acessoPro) {
    }
}
