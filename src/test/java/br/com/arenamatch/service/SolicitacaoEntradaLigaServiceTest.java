package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolicitacaoEntradaLigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private ConviteLigaRepository conviteLigaRepository;

    @Mock
    private PlacarPendenteService placarPendenteService;

    @Mock
    private BanimentoLigaService banimentoLigaService;

    private SolicitacaoEntradaLigaService solicitacaoEntradaLigaService;

    @BeforeEach
    void setUp() {
        solicitacaoEntradaLigaService = new SolicitacaoEntradaLigaService(
                ligaRepository,
                conviteLigaRepository,
                placarPendenteService,
                banimentoLigaService);
    }

    @Test
    void deveBloquearSolicitacaoDeEntradaPorTimeBanido() {
        Liga liga = new Liga();
        liga.setId(1L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        org.mockito.Mockito.doThrow(new RuntimeException("Este time esta banido desta liga."))
                .when(banimentoLigaService).validarTimeNaoBanido(1L, 20L);

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> solicitacaoEntradaLigaService.solicitarEntradaNaLiga(1L, 20L));

        assertEquals("Este time esta banido desta liga.", erro.getMessage());
        verify(conviteLigaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
