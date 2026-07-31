package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private PlacarPendenteService placarPendenteService;

    @Mock
    private LigaMapper ligaMapper;

    @Mock
    private ConviteLigaService conviteLigaService;

    @Mock
    private LigaConsultaService ligaConsultaService;

    @Mock
    private SolicitacaoEntradaLigaService solicitacaoEntradaLigaService;

    @Mock
    private PartidaLigaService partidaLigaService;

    @Mock
    private PublicacaoLigaService publicacaoLigaService;

    @Mock
    private RankingLigaService rankingLigaService;

    @Mock
    private ScoutLigaService scoutLigaService;

    @Mock
    private BanimentoLigaService banimentoLigaService;

    private LigaService ligaService;

    @BeforeEach
    void setUp() {
        ligaService = new LigaService(
                ligaRepository,
                timeRepository,
                placarPendenteService,
                ligaMapper,
                conviteLigaService,
                ligaConsultaService,
                solicitacaoEntradaLigaService,
                partidaLigaService,
                publicacaoLigaService,
                rankingLigaService,
                scoutLigaService,
                banimentoLigaService);
    }

    @Test
    void removerMembroDevePermitirSomenteAdminDaLiga() {
        Time admin = time(10L);
        Time membro = time(20L);
        Liga liga = liga(admin, membro);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(membro));

        ligaService.removerMembro(1L, 20L, 10L);

        verify(ligaRepository).save(liga);
    }

    @Test
    void removerMembroDeveBloquearSolicitanteQueNaoEAdmin() {
        Time admin = time(10L);
        Time membro = time(20L);
        Liga liga = liga(admin, membro);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(membro));

        assertThrows(RuntimeException.class, () -> ligaService.removerMembro(1L, 20L, 99L));

        verify(ligaRepository, never()).save(liga);
    }

    private Liga liga(Time admin, Time membro) {
        Liga liga = new Liga();
        liga.setId(1L);
        liga.setAdmin(admin);
        liga.getTimes().add(admin);
        liga.getTimes().add(membro);
        return liga;
    }

    private Time time(Long id) {
        Time time = new Time();
        time.setId(id);
        return time;
    }
}
