package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CadastroServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TimeRepository timeRepository;
    @Mock private PartidaRepository partidaRepository;
    @Mock private CadastroValidacaoService cadastroValidacaoService;
    @Mock private UsuarioCadastroService usuarioCadastroService;
    @Mock private TimeCadastroService timeCadastroService;
    @Mock private CadastroAgendaService cadastroAgendaService;
    @Mock private CadastroMapper cadastroMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private CadastroService cadastroService;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveDesativarSomenteOUsuarioAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("time@arena.com", null, List.of()));

        Usuario usuario = new Usuario();
        usuario.setEmail("time@arena.com");
        usuario.setStatusUsuario(StatusUsuario.ATIVO);
        when(usuarioRepository.findByEmail("time@arena.com")).thenReturn(Optional.of(usuario));

        cadastroService.desativarContaAutenticada();

        assertEquals(StatusUsuario.INATIVO, usuario.getStatusUsuario());
        verify(usuarioRepository).save(usuario);
    }
}
