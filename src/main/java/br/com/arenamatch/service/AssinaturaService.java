package br.com.arenamatch.service;

import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssinaturaService {

    private final UsuarioRepository usuarioRepository;

    public AssinaturaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario atualizarTrialExpirado(Usuario usuario) {
        if (usuario.getPlanoAssinatura() == null) {
            sincronizarPlanoPeloPagamento(usuario);
        }

        if (usuario.getPlanoAssinatura() == PlanoAssinatura.TRIAL
                && usuario.getDataExpiracao() != null
                && LocalDateTime.now().isAfter(usuario.getDataExpiracao())) {
            usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
            usuario.setStatusPagamento(StatusPagamento.EXPIRADO);
            return usuarioRepository.save(usuario);
        }

        return usuario;
    }

    public boolean temAcessoCompleto(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (Perfil.ADMIN.equals(usuario.getPerfil())) {
            return true;
        }

        boolean trialValido = usuario.getPlanoAssinatura() == PlanoAssinatura.TRIAL
                && usuario.getStatusPagamento() == StatusPagamento.TRIAL
                && usuario.getDataExpiracao() != null
                && LocalDateTime.now().isBefore(usuario.getDataExpiracao());
        boolean proValido = usuario.getPlanoAssinatura() == PlanoAssinatura.PRO
                && usuario.getStatusPagamento() == StatusPagamento.PAGO;

        return trialValido || proValido;
    }

    public void validarAcessoCompleto(Usuario usuario) {
        usuario = atualizarTrialExpirado(usuario);
        if (!temAcessoCompleto(usuario)) {
            throw new RuntimeException("Recurso disponivel no plano PRO.");
        }
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    @Transactional
    public void converterTrialsExpiradosParaBasico() {
        usuarioRepository.converterTrialsExpiradosParaBasico(LocalDateTime.now());
    }

    private void sincronizarPlanoPeloPagamento(Usuario usuario) {
        if (usuario.getStatusPagamento() == StatusPagamento.PAGO) {
            usuario.setPlanoAssinatura(PlanoAssinatura.PRO);
        } else if (usuario.getStatusPagamento() == StatusPagamento.TRIAL) {
            usuario.setPlanoAssinatura(PlanoAssinatura.TRIAL);
        } else {
            usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
        }
    }
}
