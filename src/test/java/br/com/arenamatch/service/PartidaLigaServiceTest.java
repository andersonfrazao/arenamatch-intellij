package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.PartidaLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PartidaLigaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartidaLigaServiceTest {

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private PartidaLigaRepository partidaLigaRepository;

    @Mock
    private DesafioPartidaService desafioPartidaService;

    @Mock
    private PartidaMapper partidaMapper;

    private PartidaLigaService partidaLigaService;

    @BeforeEach
    void setUp() {
        partidaLigaService = new PartidaLigaService(
                ligaRepository,
                partidaLigaRepository,
                desafioPartidaService,
                partidaMapper);
    }

    @Test
    void deveCriarJogoDaLigaQuandoOsDoisTimesSaoMembros() {
        Liga liga = criarLigaComTimes(1L, 10L, 20L);
        DesafioDTO desafio = criarDesafio(10L, 20L);
        Partida partida = new Partida();
        partida.setId(100L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(desafioPartidaService.criarDesafio(desafio)).thenReturn(partida);
        when(partidaLigaRepository.existsByPartidaId(100L)).thenReturn(false);
        when(partidaLigaRepository.save(any(PartidaLiga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartidaLiga resultado = partidaLigaService.criarJogoDaLiga(1L, desafio);

        assertEquals(liga, resultado.getLiga());
        assertEquals(partida, resultado.getPartida());
        assertTrue(resultado.isContaRankingLiga());
        verify(desafioPartidaService).criarDesafio(desafio);
    }

    @Test
    void deveBloquearJogoDaLigaQuandoDesafiadoNaoEMembro() {
        Liga liga = criarLigaComTimes(1L, 10L);
        DesafioDTO desafio = criarDesafio(10L, 20L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> partidaLigaService.criarJogoDaLiga(1L, desafio));

        assertEquals("O time desafiado nao pertence a liga.", erro.getMessage());
        verify(desafioPartidaService, never()).criarDesafio(any());
        verify(partidaLigaRepository, never()).save(any());
    }

    @Test
    void deveBloquearVinculoDuplicadoDePartidaComLiga() {
        Liga liga = criarLigaComTimes(1L, 10L, 20L);
        Partida partida = new Partida();
        partida.setId(100L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(partidaLigaRepository.existsByPartidaId(100L)).thenReturn(true);

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> partidaLigaService.vincularPartida(1L, partida));

        assertEquals("Esta partida ja esta vinculada a uma liga.", erro.getMessage());
        verify(partidaLigaRepository, never()).save(any());
    }

    @Test
    void deveSalvarVinculoComDataEFlagDeRanking() {
        Liga liga = criarLigaComTimes(1L, 10L, 20L);
        Partida partida = new Partida();
        partida.setId(100L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(partidaLigaRepository.existsByPartidaId(100L)).thenReturn(false);
        when(partidaLigaRepository.save(any(PartidaLiga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        partidaLigaService.vincularPartida(1L, partida);

        ArgumentCaptor<PartidaLiga> captor = ArgumentCaptor.forClass(PartidaLiga.class);
        verify(partidaLigaRepository).save(captor.capture());
        assertEquals(liga, captor.getValue().getLiga());
        assertEquals(partida, captor.getValue().getPartida());
        assertTrue(captor.getValue().isContaRankingLiga());
        assertTrue(captor.getValue().getDataVinculo() != null);
    }

    @Test
    void deveListarSomenteJogosVinculadosALiga() {
        Partida partida = new Partida();
        partida.setId(100L);
        partida.setStatus(StatusPartida.AGENDADO);

        PartidaLiga vinculo = new PartidaLiga();
        vinculo.setPartida(partida);

        PartidaDTO dto = new PartidaDTO();
        dto.setId(100L);
        dto.setStatus(StatusPartida.AGENDADO);

        when(ligaRepository.existsById(1L)).thenReturn(true);
        when(partidaLigaRepository.buscarPorLiga(1L)).thenReturn(List.of(vinculo));
        when(partidaMapper.toDTO(partida)).thenReturn(dto);

        List<PartidaDTO> jogos = partidaLigaService.listarJogosDaLiga(1L);

        assertEquals(1, jogos.size());
        assertEquals(100L, jogos.get(0).getId());
        verify(partidaLigaRepository).buscarPorLiga(1L);
    }

    @Test
    void deveListarJogosRecentesDoMuralSomenteComVinculoDeLigaERespeitarLimite() {
        Liga liga = criarLigaComTimes(1L, 10L, 20L);
        liga.setNome("Liga Sabado");

        Partida primeira = new Partida();
        primeira.setId(100L);
        primeira.setStatus(StatusPartida.FINALIZADO);
        Partida segunda = new Partida();
        segunda.setId(101L);
        segunda.setStatus(StatusPartida.FINALIZADO);

        PartidaLiga vinculoUm = new PartidaLiga();
        vinculoUm.setLiga(liga);
        vinculoUm.setPartida(primeira);
        PartidaLiga vinculoDois = new PartidaLiga();
        vinculoDois.setLiga(liga);
        vinculoDois.setPartida(segunda);

        PartidaDTO dtoPrimeira = new PartidaDTO();
        dtoPrimeira.setId(100L);
        dtoPrimeira.setStatus(StatusPartida.FINALIZADO);
        dtoPrimeira.setMandante(new TimeResumoDTO(10L, "A", null, null, null, true));
        dtoPrimeira.setVisitante(new TimeResumoDTO(20L, "B", null, null, null, false));

        when(partidaLigaRepository.buscarJogosRecentes()).thenReturn(List.of(vinculoUm, vinculoDois));
        when(partidaMapper.toDTO(primeira)).thenReturn(dtoPrimeira);

        List<JogoRecenteLigaDTO> jogos = partidaLigaService.listarJogosRecentesDoMural(1);

        assertEquals(1, jogos.size());
        assertEquals(100L, jogos.get(0).getIdPartida());
        assertEquals("Liga Sabado", jogos.get(0).getNomeLiga());
        verify(partidaLigaRepository).buscarJogosRecentes();
        verify(partidaMapper, never()).toDTO(segunda);
    }

    private Liga criarLigaComTimes(Long ligaId, Long... timesIds) {
        Liga liga = new Liga();
        liga.setId(ligaId);
        liga.setTimes(List.of(timesIds).stream().map(this::criarTime).toList());
        return liga;
    }

    private Time criarTime(Long id) {
        Time time = new Time();
        time.setId(id);
        return time;
    }

    private DesafioDTO criarDesafio(Long desafianteId, Long desafiadoId) {
        DesafioDTO desafio = new DesafioDTO();
        desafio.setIdTimeDesafiante(desafianteId);
        desafio.setIdTimeDesafiado(desafiadoId);
        return desafio;
    }
}
