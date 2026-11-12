package br.com.arenamatch.service;

import br.com.arenamatch.dto.RegistroPartidaExternaDTO;
import br.com.arenamatch.entity.*;
import br.com.arenamatch.enums.*;
import br.com.arenamatch.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidaExternaServiceTest {
    @Mock private PartidaExternaRepository partidas;
    @Mock private AdversarioExternoRepository adversarios;
    @Mock private TimeRepository times;
    @Mock private UsuarioRepository usuarios;
    @Mock private PartidaRepository partidasOficiais;
    @Mock private EntityManager entityManager;
    @InjectMocks private PartidaExternaService service;

    private Time time;
    private Usuario usuario;
    private RegistroPartidaExternaDTO dto;

    @BeforeEach
    void prepararDados() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("representante@teste.local");
        usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
        time = new Time();
        time.setId(10L);
        time.setNome("Mocidade FC");
        time.setResponsavel(usuario);
        time.getModalidades().add(Modalidade.FUTEBOL_CAMPO);
        dto = new RegistroPartidaExternaDTO();
        dto.setChaveSolicitacao(UUID.randomUUID());
        dto.setNomeAdversario("União da Vila");
        dto.setModalidade(Modalidade.FUTEBOL_CAMPO);
        dto.setCategoria(Categoria.ESPORTE);
        dto.setMando(MandoPartida.VISITANTE);
        dto.setData(LocalDate.now().plusDays(3));
        dto.setHoraInicio(LocalTime.NOON);
        dto.setHoraFim(LocalTime.of(14, 0));
        dto.setNomeCampo("Campo do União");
        dto.setValorTaxa(new BigDecimal("150.00"));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of()));
        when(usuarios.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(times.findByResponsavel(usuario)).thenReturn(Optional.of(time));
    }

    private void permitirPersistencia() {
        when(adversarios.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(partidas.save(any())).thenAnswer(invocacao -> {
            PartidaExterna partida = invocacao.getArgument(0);
            partida.setId(50L);
            return partida;
        });
    }

    @Test
    void registraVisitanteBasicoComCampoTaxaEOrigemSemCriarTimeOficial() {
        autenticar();
        permitirPersistencia();
        var evento = service.registrar(dto);
        assertEquals("União da Vila", evento.getNomeTimeMandante());
        assertEquals("Mocidade FC", evento.getNomeTimeVisitante());
        assertEquals(new BigDecimal("150.00"), evento.getTaxaRegistroManual());
        assertTrue(evento.isRegistroManual());
        assertNull(evento.getIdPartida());
        assertEquals(50L, evento.getIdPartidaExterna());
        assertEquals("Campo do União", evento.getNomeCampo());
        assertFalse(evento.isPassouDaHora());
        var captor = ArgumentCaptor.forClass(PartidaExterna.class);
        verify(partidas).save(captor.capture());
        assertSame(time, captor.getValue().getTime());
        assertSame(usuario, captor.getValue().getAutor());
        assertEquals("MANUAL", captor.getValue().getOrigem());
        verify(times, never()).save(any());
        verify(partidasOficiais, never()).save(any());
    }

    @Test
    void aceitaRetroativoGratuitoComPlacarVazio() {
        autenticar();
        permitirPersistencia();
        dto.setData(LocalDate.now().minusYears(1));
        dto.setValorTaxa(BigDecimal.ZERO);
        dto.setMando(MandoPartida.MANDANTE);
        var evento = service.registrar(dto);
        assertTrue(evento.isPassouDaHora());
        assertEquals("PENDENTE", evento.getStatusPlacar());
        assertNull(evento.getGolsMandante());
        assertNull(evento.getGolsVisitante());
        assertEquals("Mocidade FC", evento.getNomeTimeMandante());
    }

    @Test
    void reenvioDaMesmaChaveRetornaMesmoRegistro() {
        autenticar();
        permitirPersistencia();
        service.registrar(dto);
        var captor = ArgumentCaptor.forClass(PartidaExterna.class);
        verify(partidas).save(captor.capture());
        when(partidas.findByTimeIdAndChaveSolicitacao(time.getId(), dto.getChaveSolicitacao()))
                .thenReturn(Optional.of(captor.getValue()));
        assertEquals(50L, service.registrar(dto).getIdPartidaExterna());
        verify(partidas, times(1)).save(any());
        dto.setNomeCampo("Outro campo");
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.registrar(dto)).getStatusCode());
    }

    @Test
    void reutilizaAdversarioSomenteDoProprioTime() {
        autenticar();
        AdversarioExterno adversario = new AdversarioExterno();
        adversario.setTime(time);
        when(adversarios.findByTimeIdAndNomeNormalizado(10L, "união da vila"))
                .thenReturn(Optional.of(adversario));
        when(partidas.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        dto.setNomeAdversario("  União   da Vila ");
        service.registrar(dto);
        verify(adversarios, never()).save(any());
        var captor = ArgumentCaptor.forClass(PartidaExterna.class);
        verify(partidas).save(captor.capture());
        assertSame(adversario, captor.getValue().getAdversario());
    }

    @Test
    void protegeConsultaPorProprietarioENegaAgendaDeOutroTime() {
        autenticar();
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.buscar(99L)).getStatusCode());
        verify(partidas).findByIdAndTimeId(99L, 10L);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> service.buscarEventos(20L, LocalDate.now(), LocalDate.now())).getStatusCode());
        verify(partidas, never()).findByTimeIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(any(), any(), any());
    }

    @Test
    void rejeitaRegistroSemAutenticacao() {
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                () -> service.registrar(dto)).getStatusCode());
        verifyNoInteractions(partidas, adversarios);
    }

    @Test
    void rejeitaModalidadeNaoCadastradaNoTime() {
        autenticar();
        dto.setModalidade(Modalidade.FUTSAL);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.registrar(dto)).getStatusCode());
        verify(partidas, never()).save(any());
    }

    @Test
    void rejeitaConflitoDeHorarioExternoEJogoOficialConfirmado() {
        autenticar();
        when(partidas.existeConflito(any(), any(), any(), any())).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.registrar(dto)).getStatusCode());
        when(partidas.existeConflito(any(), any(), any(), any())).thenReturn(false);
        Partida oficial = new Partida();
        oficial.setStatus(StatusPartida.AGENDADO);
        oficial.setModalidade(Modalidade.FUTEBOL_CAMPO);
        when(partidasOficiais.buscarPartidasPorIntervalo(any(), any(), any())).thenReturn(List.of(oficial));
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.registrar(dto)).getStatusCode());
        verifyNoInteractions(adversarios);
    }

    @Test
    void validaObrigatoriosTaxaEIntervaloAntesDePersistir() {
        dto.setValorTaxa(new BigDecimal("-1"));
        assertThrows(ResponseStatusException.class, () -> service.registrar(dto));
        dto.setValorTaxa(new BigDecimal("1.001"));
        assertThrows(ResponseStatusException.class, () -> service.registrar(dto));
        dto.setValorTaxa(BigDecimal.ZERO);
        dto.setHoraFim(dto.getHoraInicio());
        assertThrows(ResponseStatusException.class, () -> service.registrar(dto));
        dto.setHoraFim(LocalTime.of(14, 0));
        dto.setCategoria(null);
        assertThrows(ResponseStatusException.class, () -> service.registrar(dto));
        verifyNoInteractions(partidas, adversarios);
    }
}
