package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LigaConsultaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private ConviteLigaRepository conviteLigaRepository;

    @Mock
    private PartidaLigaRepository partidaLigaRepository;

    private LigaConsultaService ligaConsultaService;

    @BeforeEach
    void setUp() {
        ligaConsultaService = new LigaConsultaService(
                ligaRepository,
                conviteLigaRepository,
                partidaLigaRepository,
                new LigaMapper());
    }

    @Test
    void deveListarLigasEmAltaGlobaisComContadoresDeMovimentacao() {
        Liga liga = criarLiga(1L, "Liga Sabado", 10L, 20L, 30L);

        when(ligaRepository.buscarLigasMaisMovimentadas()).thenReturn(List.of(liga));
        when(conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(1L, 20L, StatusConviteLiga.PENDENTE))
                .thenReturn(true);
        when(ligaRepository.contarPublicacoesAbertas(1L)).thenReturn(2L);
        when(partidaLigaRepository.countByLigaId(1L)).thenReturn(4L);

        List<LigaExplorarDTO> resultado = ligaConsultaService.listarLigasEmAlta(20L);

        assertEquals(1, resultado.size());
        assertEquals("Liga Sabado", resultado.get(0).getNome());
        assertEquals(3, resultado.get(0).getQtdTimes());
        assertEquals(2L, resultado.get(0).getQtdPublicacoesAbertas());
        assertEquals(4L, resultado.get(0).getQtdJogos());
        assertEquals(9L, resultado.get(0).getMovimentacao());
        assertEquals(true, resultado.get(0).isJaParticipa());
        assertEquals(true, resultado.get(0).isConvitePendente());
        verify(ligaRepository).buscarLigasMaisMovimentadas();
    }

    private Liga criarLiga(Long id, String nome, Long... timeIds) {
        Liga liga = new Liga();
        liga.setId(id);
        liga.setNome(nome);
        liga.setAdmin(criarTime(timeIds[0]));
        liga.setTimes(new ArrayList<>());
        for (Long timeId : timeIds) {
            liga.getTimes().add(criarTime(timeId));
        }
        return liga;
    }

    private Time criarTime(Long id) {
        Time time = new Time();
        time.setId(id);
        time.setNome("Time " + id);
        return time;
    }
}
