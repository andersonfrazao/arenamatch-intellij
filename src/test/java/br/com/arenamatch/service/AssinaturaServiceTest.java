package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AssinaturaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AssinaturaService assinaturaService;

    @Test
    void deveConverterTrialExpiradoParaBasico() {
        Usuario usuario = new Usuario();
        usuario.setPlanoAssinatura(PlanoAssinatura.TRIAL);
        usuario.setStatusPagamento(StatusPagamento.TRIAL);
        usuario.setStatusAssinatura(StatusAssinatura.TRIAL);
        usuario.setDataExpiracao(LocalDateTime.now().minusMinutes(1));

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario atualizado = assinaturaService.atualizarTrialExpirado(usuario);

        assertSame(usuario, atualizado);
        assertEquals(PlanoAssinatura.BASICO, atualizado.getPlanoAssinatura());
        assertEquals(StatusPagamento.EXPIRADO, atualizado.getStatusPagamento());
        assertEquals(StatusAssinatura.VENCIDO, atualizado.getStatusAssinatura());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveExecutarConversaoPeriodicaDeTrialsExpirados() {
        assinaturaService.converterTrialsExpiradosParaBasico();

        verify(usuarioRepository).converterTrialsExpiradosParaBasico(any(LocalDateTime.class));
    }
}
