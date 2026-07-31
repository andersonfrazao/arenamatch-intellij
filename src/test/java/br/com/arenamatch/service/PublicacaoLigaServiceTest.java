package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.CandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.PublicacaoLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPublicacaoLiga;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PublicacaoLigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicacaoLigaServiceTest {

    @Mock
    private PublicacaoLigaRepository publicacaoLigaRepository;

    @Mock
    private LigaRepository ligaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private PublicacaoLigaMapper publicacaoLigaMapper;

    @Mock
    private AssinaturaService assinaturaService;

    @Mock
    private DesafioPartidaService desafioPartidaService;

    @Mock
    private PartidaLigaService partidaLigaService;

    @Mock
    private SolicitacaoEntradaLigaService solicitacaoEntradaLigaService;

    @Mock
    private ConviteLigaService conviteLigaService;

    private PublicacaoLigaService publicacaoLigaService;

    @BeforeEach
    void setUp() {
        publicacaoLigaService = new PublicacaoLigaService(
                publicacaoLigaRepository,
                ligaRepository,
                timeRepository,
                publicacaoLigaMapper,
                assinaturaService,
                desafioPartidaService,
                partidaLigaService,
                solicitacaoEntradaLigaService,
                conviteLigaService);
    }

    @Test
    void deveCriarPublicacaoAbertaQuandoAutorEMembroDaLiga() {
        Liga liga = criarLiga(1L, 10L);
        Time autor = criarTime(10L);
        NovaPublicacaoLigaDTO nova = criarNovaPublicacao(1L, 10L);
        PublicacaoLigaDTO dto = new PublicacaoLigaDTO();
        dto.setStatus(StatusPublicacaoLiga.ABERTO);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(publicacaoLigaRepository.save(any(PublicacaoLiga.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicacaoLigaMapper.toDTO(any(PublicacaoLiga.class))).thenReturn(dto);

        PublicacaoLigaDTO resultado = publicacaoLigaService.criarPublicacao(nova);

        assertEquals(StatusPublicacaoLiga.ABERTO, resultado.getStatus());
        verify(publicacaoLigaRepository).save(any(PublicacaoLiga.class));
    }

    @Test
    void deveBloquearPublicacaoQuandoAutorNaoEMembroDaLiga() {
        Liga liga = criarLiga(1L, 10L);
        Time autor = criarTime(20L);
        NovaPublicacaoLigaDTO nova = criarNovaPublicacao(1L, 20L);

        when(ligaRepository.findById(1L)).thenReturn(Optional.of(liga));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(autor));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> publicacaoLigaService.criarPublicacao(nova));

        assertEquals("Apenas membros da liga podem publicar no mural.", erro.getMessage());
        verify(publicacaoLigaRepository, never()).save(any());
    }

    @Test
    void deveCriarPublicacaoSemLigaQuandoAutorTemAcessoCompleto() {
        Time autor = criarTime(10L);
        NovaPublicacaoLigaDTO nova = criarNovaPublicacao(null, 10L);
        PublicacaoLigaDTO dto = new PublicacaoLigaDTO();
        dto.setSemLiga(true);
        dto.setStatus(StatusPublicacaoLiga.ABERTO);

        when(timeRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(publicacaoLigaRepository.save(any(PublicacaoLiga.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicacaoLigaMapper.toDTO(any(PublicacaoLiga.class))).thenReturn(dto);

        PublicacaoLigaDTO resultado = publicacaoLigaService.criarPublicacao(nova);

        assertEquals(StatusPublicacaoLiga.ABERTO, resultado.getStatus());
        assertEquals(true, resultado.isSemLiga());
        verify(ligaRepository, never()).findById(any());
        verify(publicacaoLigaRepository).save(any(PublicacaoLiga.class));
    }

    @Test
    void deveListarMuralGlobalComPublicacoesComESemLiga() {
        PublicacaoLiga semLiga = criarPublicacaoExistente(10L, 30L);
        semLiga.setId(101L);
        semLiga.setLiga(null);
        PublicacaoLiga comLiga = criarPublicacaoExistente(20L, 30L);
        comLiga.setId(102L);

        PublicacaoLigaDTO dtoSemLiga = new PublicacaoLigaDTO();
        dtoSemLiga.setSemLiga(true);
        PublicacaoLigaDTO dtoComLiga = new PublicacaoLigaDTO();
        dtoComLiga.setIdLiga(1L);

        when(publicacaoLigaRepository.buscarMuralGlobalAberto()).thenReturn(List.of(semLiga, comLiga));
        when(publicacaoLigaMapper.toDTO(semLiga)).thenReturn(dtoSemLiga);
        when(publicacaoLigaMapper.toDTO(comLiga)).thenReturn(dtoComLiga);

        List<PublicacaoLigaDTO> resultado = publicacaoLigaService.listarPublicacoesGlobais(99L);

        assertEquals(2, resultado.size());
        assertEquals(true, resultado.get(0).isSemLiga());
        verify(publicacaoLigaRepository).buscarMuralGlobalAberto();
    }

    @Test
    void candidaturaEmPublicacaoSemLigaCriaDesafioNormal() {
        PublicacaoLiga publicacao = criarPublicacaoExistente(10L, 30L);
        publicacao.setLiga(null);
        CandidaturaPublicacaoLigaDTO dto = new CandidaturaPublicacaoLigaDTO();
        dto.setIdTimeCandidato(20L);

        when(publicacaoLigaRepository.findById(100L)).thenReturn(Optional.of(publicacao));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(criarTime(20L)));

        assertEquals("DESAFIO_NORMAL", publicacaoLigaService.candidatar(100L, dto).getAcao());

        verify(desafioPartidaService).criarDesafio(any());
        verify(partidaLigaService, never()).criarJogoDaLiga(any(), any());
    }

    @Test
    void candidaturaEmPublicacaoComLigaSemParticipacaoSolicitaEntrada() {
        PublicacaoLiga publicacao = criarPublicacaoExistente(10L, 30L);
        CandidaturaPublicacaoLigaDTO dto = new CandidaturaPublicacaoLigaDTO();
        dto.setIdTimeCandidato(20L);

        when(publicacaoLigaRepository.findById(100L)).thenReturn(Optional.of(publicacao));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(criarTime(20L)));

        assertEquals("SOLICITACAO_ENTRADA", publicacaoLigaService.candidatar(100L, dto).getAcao());

        verify(solicitacaoEntradaLigaService).solicitarEntradaNaLiga(1L, 20L);
        verify(partidaLigaService, never()).criarJogoDaLiga(any(), any());
    }

    @Test
    void candidaturaEmPublicacaoComLigaQuandoCandidatoParticipaCriaDesafioDaLiga() {
        PublicacaoLiga publicacao = criarPublicacaoExistente(10L, 30L);
        publicacao.getLiga().getTimes().add(criarTime(20L));
        CandidaturaPublicacaoLigaDTO dto = new CandidaturaPublicacaoLigaDTO();
        dto.setIdTimeCandidato(20L);

        when(publicacaoLigaRepository.findById(100L)).thenReturn(Optional.of(publicacao));
        when(timeRepository.findById(20L)).thenReturn(Optional.of(criarTime(20L)));

        assertEquals("DESAFIO_LIGA", publicacaoLigaService.candidatar(100L, dto).getAcao());

        verify(partidaLigaService).criarJogoDaLiga(any(), any());
        verify(solicitacaoEntradaLigaService, never()).solicitarEntradaNaLiga(any(), any());
    }

    @Test
    void devePermitirCancelamentoPeloAutor() {
        PublicacaoLiga publicacao = criarPublicacaoExistente(10L, 30L);
        when(publicacaoLigaRepository.findById(100L)).thenReturn(Optional.of(publicacao));

        publicacaoLigaService.cancelarPublicacao(100L, 10L);

        assertEquals(StatusPublicacaoLiga.CANCELADO, publicacao.getStatus());
        verify(publicacaoLigaRepository).save(publicacao);
    }

    @Test
    void deveBloquearCancelamentoPorTimeSemPermissao() {
        PublicacaoLiga publicacao = criarPublicacaoExistente(10L, 30L);
        when(publicacaoLigaRepository.findById(100L)).thenReturn(Optional.of(publicacao));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> publicacaoLigaService.cancelarPublicacao(100L, 99L));

        assertEquals("Apenas o autor ou o admin da liga podem cancelar esta publicacao.", erro.getMessage());
        verify(publicacaoLigaRepository, never()).save(any());
    }

    private NovaPublicacaoLigaDTO criarNovaPublicacao(Long ligaId, Long autorId) {
        NovaPublicacaoLigaDTO dto = new NovaPublicacaoLigaDTO();
        dto.setIdLiga(ligaId);
        dto.setIdTimeAutor(autorId);
        dto.setDataJogo(LocalDateTime.of(2026, 7, 20, 15, 0));
        dto.setTipoProcura(TipoProcuraPublicacaoLiga.AMBOS);
        return dto;
    }

    private PublicacaoLiga criarPublicacaoExistente(Long autorId, Long adminId) {
        Liga liga = criarLiga(1L, autorId);
        liga.setAdmin(criarTime(adminId));

        PublicacaoLiga publicacao = new PublicacaoLiga();
        publicacao.setLiga(liga);
        publicacao.setTimeAutor(criarTime(autorId));
        publicacao.setStatus(StatusPublicacaoLiga.ABERTO);
        return publicacao;
    }

    private Liga criarLiga(Long id, Long membroId) {
        Liga liga = new Liga();
        liga.setId(id);
        liga.setTimes(new ArrayList<>());
        liga.getTimes().add(criarTime(membroId));
        return liga;
    }

    private Time criarTime(Long id) {
        Time time = new Time();
        time.setId(id);
        time.setNome("Time " + id);
        return time;
    }
}
