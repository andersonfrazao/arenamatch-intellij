package br.com.arenamatch.service;

import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.enums.StatusCampeonato;
import br.com.arenamatch.repository.CampeonatoRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CampeonatoServiceTest {
    private final CampeonatoRepository repo = mock(CampeonatoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final CampeonatoAuthorizationService auth = mock(CampeonatoAuthorizationService.class);
    private final CampeonatoService service = new CampeonatoService(repo, usuarios, auth, new CampeonatoValidator());

    @Test void semPremiumNaoChegaAContagemOuPersistencia() {
        when(auth.exigirPremium()).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));
        assertThrows(ResponseStatusException.class, () -> service.criar(request()));
        verifyNoInteractions(repo, usuarios);
    }
    @Test void limiteDeTresEVerificadoDepoisDoLock() {
        Usuario u = usuario(1L);
        when(auth.exigirPremium()).thenReturn(u);
        when(usuarios.bloquearParaCriarCampeonato(1L)).thenReturn(Optional.of(u));
        when(repo.countByOrganizadorIdAndStatusNotIn(eq(1L), any())).thenReturn(3L);
        var erro = assertThrows(ResponseStatusException.class, () -> service.criar(request()));
        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
        var ordem = inOrder(usuarios, repo);
        ordem.verify(usuarios).bloquearParaCriarCampeonato(1L);
        ordem.verify(repo).countByOrganizadorIdAndStatusNotIn(1L, List.of(StatusCampeonato.FINALIZADO, StatusCampeonato.CANCELADO));
        verify(repo, never()).saveAndFlush(any());
    }
    @Test void outroOrganizadorRecebe404SemAlterarDados() {
        when(auth.exigirPremium()).thenReturn(usuario(1L));
        Campeonato c = campeonato(usuario(2L));
        when(repo.findById(99L)).thenReturn(Optional.of(c));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.atualizar(99L, request())).getStatusCode());
        verify(repo, never()).saveAndFlush(any());
    }
    @Test void consultaPermaneceDisponivelAposDowngradeMasSomenteDoProprietario() {
        Usuario u = usuario(1L); u.setPlanoAssinatura(PlanoAssinatura.BASICO);
        when(auth.usuarioAutenticado()).thenReturn(u);
        when(repo.findById(99L)).thenReturn(Optional.of(campeonato(u)));
        assertEquals("Copa", service.consultar(99L).getNome());
        verify(auth, never()).exigirPremium();
    }
    @Test void versaoDesatualizadaImpedeEdicao() {
        Usuario u = usuario(1L); when(auth.exigirPremium()).thenReturn(u);
        Campeonato c = campeonato(u); c.setVersao(2L);
        when(repo.findById(99L)).thenReturn(Optional.of(c));
        CampeonatoRequestDTO request = request(); request.setVersao(1L);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.atualizar(99L, request)).getStatusCode());
        assertEquals("Copa", c.getNome()); verify(repo, never()).saveAndFlush(any());
    }
    @Test void cancelamentoMantemRegistroELiberaVaga() {
        Usuario u = usuario(1L); when(auth.exigirPremium()).thenReturn(u);
        Campeonato c = campeonato(u); when(repo.findById(99L)).thenReturn(Optional.of(c));
        when(repo.saveAndFlush(c)).thenReturn(c);
        assertEquals(StatusCampeonato.CANCELADO, service.cancelarRascunho(99L, 0L).getStatus());
        assertEquals("Copa", c.getNome()); verify(repo, never()).delete(any());
    }
    private Usuario usuario(Long id) {
        Usuario u = new Usuario(); u.setId(id); u.setPlanoAssinatura(PlanoAssinatura.PREMIUM); u.setStatusPagamento(StatusPagamento.PAGO); return u;
    }
    private Campeonato campeonato(Usuario u) {
        Campeonato c = new Campeonato(); c.setOrganizador(u); c.setNome("Copa"); c.setVersao(0L); return c;
    }
    private CampeonatoRequestDTO request() { CampeonatoRequestDTO r = new CampeonatoRequestDTO(); r.setNome("Copa"); return r; }
}
