package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.entity.BanimentoLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.BanimentoLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanimentoLigaServiceTest {

    @Mock
    private BanimentoLigaRepository banimentoLigaRepository;

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private BanimentoLigaMapper banimentoLigaMapper;

    private BanimentoLigaService banimentoLigaService;

    @BeforeEach
    void setUp() {
        banimentoLigaService = new BanimentoLigaService(
                banimentoLigaRepository,
                ligaRepository,
                timeRepository,
                banimentoLigaMapper);
    }

    @Test
    void deveBanirMembroComMotivoObrigatorioERemoverDaLiga() {
        Time admin = time(10L);
        Time membro = time(20L);
        Liga liga = liga(admin, membro);
        BanimentoLigaDTO dto = new BanimentoLigaDTO();
        dto.setAtivo(true);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(membro));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(banimentoLigaRepository.existsByLigaIdAndTimeBanidoIdAndAtivoTrue(1L, 20L)).thenReturn(false);
        when(banimentoLigaRepository.save(any(BanimentoLiga.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(banimentoLigaMapper.toDTO(any(BanimentoLiga.class))).thenReturn(dto);

        BanimentoLigaDTO resultado = banimentoLigaService.banirTime(1L, 20L, 10L, "Conduta violenta");

        assertTrue(resultado.isAtivo());
        assertEquals(1, liga.getTimes().size());
        verify(ligaRepository).save(liga);
        verify(banimentoLigaRepository).save(any(BanimentoLiga.class));
    }

    @Test
    void deveBloquearBanimentoSemMotivo() {
        Time admin = time(10L);
        Time membro = time(20L);
        Liga liga = liga(admin, membro);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(membro));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(admin));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> banimentoLigaService.banirTime(1L, 20L, 10L, " "));

        assertEquals("Informe o motivo do banimento.", erro.getMessage());
        verify(banimentoLigaRepository, never()).save(any());
    }

    @Test
    void deveBloquearBanimentoPeloNaoAdmin() {
        Time admin = time(10L);
        Time membro = time(20L);
        Time solicitante = time(99L);
        Liga liga = liga(admin, membro);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(membro));
        when(timeRepository.findById(99L)).thenReturn(Optional.of(solicitante));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> banimentoLigaService.banirTime(1L, 20L, 99L, "Conduta inadequada"));

        assertEquals("Apenas o responsavel pela liga pode banir membros.", erro.getMessage());
        verify(banimentoLigaRepository, never()).save(any());
    }

    @Test
    void deveBloquearAutoBanimentoDoAdmin() {
        Time admin = time(10L);
        Liga liga = liga(admin);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(admin));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> banimentoLigaService.banirTime(1L, 10L, 10L, "Teste"));

        assertEquals("O responsavel pela liga nao pode ser banido.", erro.getMessage());
        verify(banimentoLigaRepository, never()).save(any());
    }

    @Test
    void deveReverterBanimentoAtivo() {
        Time admin = time(10L);
        Liga liga = liga(admin);
        BanimentoLiga banimento = new BanimentoLiga();
        banimento.setAtivo(true);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(banimentoLigaRepository.findByLigaIdAndTimeBanidoIdAndAtivoTrue(1L, 20L))
                .thenReturn(Optional.of(banimento));

        banimentoLigaService.reverterBanimento(1L, 20L, 10L);

        assertEquals(false, banimento.isAtivo());
        verify(banimentoLigaRepository).save(banimento);
    }

    private Liga liga(Time admin, Time... membros) {
        Liga liga = new Liga();
        liga.setId(1L);
        liga.setAdmin(admin);
        liga.getTimes().add(admin);
        for (Time membro : membros) {
            liga.getTimes().add(membro);
        }
        return liga;
    }

    private Time time(Long id) {
        Time time = new Time();
        time.setId(id);
        time.setNome("Time " + id);
        return time;
    }
}
