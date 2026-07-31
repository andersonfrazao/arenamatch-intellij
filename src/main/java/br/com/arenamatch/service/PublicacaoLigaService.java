package br.com.arenamatch.service;

import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.CandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.ResultadoCandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.PublicacaoLiga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPublicacaoLiga;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.PublicacaoLigaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicacaoLigaService {

    private final PublicacaoLigaRepository publicacaoLigaRepository;
    private final LigaRepository ligaRepository;
    private final TimeRepository timeRepository;
    private final PublicacaoLigaMapper publicacaoLigaMapper;
    private final AssinaturaService assinaturaService;
    private final DesafioPartidaService desafioPartidaService;
    private final PartidaLigaService partidaLigaService;
    private final SolicitacaoEntradaLigaService solicitacaoEntradaLigaService;
    private final ConviteLigaService conviteLigaService;

    public PublicacaoLigaService(
            PublicacaoLigaRepository publicacaoLigaRepository,
            LigaRepository ligaRepository,
            TimeRepository timeRepository,
            PublicacaoLigaMapper publicacaoLigaMapper,
            AssinaturaService assinaturaService,
            DesafioPartidaService desafioPartidaService,
            PartidaLigaService partidaLigaService,
            SolicitacaoEntradaLigaService solicitacaoEntradaLigaService,
            ConviteLigaService conviteLigaService) {
        this.publicacaoLigaRepository = publicacaoLigaRepository;
        this.ligaRepository = ligaRepository;
        this.timeRepository = timeRepository;
        this.publicacaoLigaMapper = publicacaoLigaMapper;
        this.assinaturaService = assinaturaService;
        this.desafioPartidaService = desafioPartidaService;
        this.partidaLigaService = partidaLigaService;
        this.solicitacaoEntradaLigaService = solicitacaoEntradaLigaService;
        this.conviteLigaService = conviteLigaService;
    }

    @Transactional
    public PublicacaoLigaDTO criarPublicacao(NovaPublicacaoLigaDTO dto) {
        Time autor = timeRepository.findById(dto.getIdTimeAutor())
                .orElseThrow(() -> new RuntimeException("Time autor nao encontrado."));
        assinaturaService.validarAcessoCompleto(autor.getResponsavel());

        Liga liga = null;
        if (dto.getIdLiga() != null) {
            liga = ligaRepository.findById(dto.getIdLiga())
                    .orElseThrow(() -> new RuntimeException("Liga nao encontrada."));
            validarMembroDaLiga(liga, autor.getId(), "Apenas membros da liga podem publicar no mural.");
        }

        validarCamposObrigatorios(dto);

        PublicacaoLiga publicacao = new PublicacaoLiga();
        publicacao.setLiga(liga);
        publicacao.setTimeAutor(autor);
        publicacao.setDataJogo(dto.getDataJogo());
        publicacao.setHoraInicio(normalizarTexto(dto.getHoraInicio()));
        publicacao.setHoraFim(normalizarTexto(dto.getHoraFim()));
        publicacao.setTipoProcura(dto.getTipoProcura());
        publicacao.setCategoria(dto.getCategoria());
        publicacao.setRegiao(normalizarTexto(dto.getRegiao()));
        publicacao.setObservacao(normalizarTexto(dto.getObservacao()));
        publicacao.setDataExpiracao(dto.getDataExpiracao());
        publicacao.setStatus(StatusPublicacaoLiga.ABERTO);
        publicacao.setDataCriacao(LocalDateTime.now());

        return publicacaoLigaMapper.toDTO(publicacaoLigaRepository.save(publicacao));
    }

    @Transactional(readOnly = true)
    public List<PublicacaoLigaDTO> listarPublicacoesDaLiga(Long ligaId) {
        if (!ligaRepository.existsById(ligaId)) {
            throw new RuntimeException("Liga nao encontrada.");
        }

        return publicacaoLigaRepository.buscarPorLiga(ligaId).stream()
                .map(publicacaoLigaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicacaoLigaDTO> listarPublicacoesGlobais(Long meuTimeId) {
        return publicacaoLigaRepository.buscarMuralGlobalAberto().stream()
                .map(publicacao -> preencherFlags(publicacaoLigaMapper.toDTO(publicacao), publicacao, meuTimeId))
                .toList();
    }

    @Transactional
    public ResultadoCandidaturaPublicacaoLigaDTO candidatar(Long publicacaoId, CandidaturaPublicacaoLigaDTO dto) {
        if (dto == null || dto.getIdTimeCandidato() == null) {
            throw new RuntimeException("Informe o time candidato.");
        }

        PublicacaoLiga publicacao = publicacaoLigaRepository.findById(publicacaoId)
                .orElseThrow(() -> new RuntimeException("Publicacao nao encontrada."));
        if (publicacao.getStatus() != StatusPublicacaoLiga.ABERTO) {
            throw new RuntimeException("Esta publicacao nao esta aberta para candidatura.");
        }

        Long candidatoId = dto.getIdTimeCandidato();
        Long autorId = publicacao.getTimeAutor().getId();
        if (Objects.equals(candidatoId, autorId)) {
            throw new RuntimeException("O time autor nao pode se candidatar na propria publicacao.");
        }

        Time candidato = timeRepository.findById(candidatoId)
                .orElseThrow(() -> new RuntimeException("Time candidato nao encontrado."));
        assinaturaService.validarAcessoCompleto(candidato.getResponsavel());

        if (dto.getIdLigaCandidato() != null && !Objects.equals(dto.getIdLigaCandidato(), idLiga(publicacao))) {
            return candidatarComLigaDoCandidato(publicacao, dto.getIdLigaCandidato(), candidatoId, autorId);
        }

        if (publicacao.getLiga() != null) {
            if (!ehMembro(publicacao.getLiga(), candidatoId)) {
                solicitacaoEntradaLigaService.solicitarEntradaNaLiga(publicacao.getLiga().getId(), candidatoId);
                return new ResultadoCandidaturaPublicacaoLigaDTO(
                        "SOLICITACAO_ENTRADA",
                        "Solicitacao enviada. Aguarde aprovacao para disputar este jogo pela liga.",
                        publicacao.getLiga().getId());
            }

            partidaLigaService.criarJogoDaLiga(publicacao.getLiga().getId(), criarDesafio(publicacao, candidatoId));
            return new ResultadoCandidaturaPublicacaoLigaDTO(
                    "DESAFIO_LIGA",
                    "Candidatura enviada como desafio vinculado a liga.",
                    publicacao.getLiga().getId());
        }

        desafioPartidaService.criarDesafio(criarDesafio(publicacao, candidatoId));
        return new ResultadoCandidaturaPublicacaoLigaDTO(
                "DESAFIO_NORMAL",
                "Candidatura enviada como desafio comum.",
                null);
    }

    @Transactional
    public void cancelarPublicacao(Long publicacaoId, Long timeSolicitanteId) {
        PublicacaoLiga publicacao = publicacaoLigaRepository.findById(publicacaoId)
                .orElseThrow(() -> new RuntimeException("Publicacao nao encontrada."));

        Long adminId = publicacao.getLiga() != null ? publicacao.getLiga().getAdmin().getId() : null;
        Long autorId = publicacao.getTimeAutor().getId();
        boolean podeCancelar = autorId.equals(timeSolicitanteId) || Objects.equals(adminId, timeSolicitanteId);

        if (!podeCancelar) {
            throw new RuntimeException("Apenas o autor ou o admin da liga podem cancelar esta publicacao.");
        }

        publicacao.setStatus(StatusPublicacaoLiga.CANCELADO);
        publicacaoLigaRepository.save(publicacao);
    }

    private void validarCamposObrigatorios(NovaPublicacaoLigaDTO dto) {
        if (dto.getDataJogo() == null) {
            throw new RuntimeException("Informe a data do jogo.");
        }

        if (dto.getTipoProcura() == null) {
            throw new RuntimeException("Informe o tipo de adversario procurado.");
        }
    }

    private void validarMembroDaLiga(Liga liga, Long timeId, String mensagemErro) {
        if (!ehMembro(liga, timeId)) {
            throw new RuntimeException(mensagemErro);
        }
    }

    private ResultadoCandidaturaPublicacaoLigaDTO candidatarComLigaDoCandidato(
            PublicacaoLiga publicacao,
            Long ligaCandidatoId,
            Long candidatoId,
            Long autorId) {
        Liga ligaCandidato = ligaRepository.findById(ligaCandidatoId)
                .orElseThrow(() -> new RuntimeException("Liga do candidato nao encontrada."));
        validarMembroDaLiga(ligaCandidato, candidatoId, "Apenas membros da liga podem usar esta liga na candidatura.");

        if (!ehMembro(ligaCandidato, autorId)) {
            conviteLigaService.enviarConvite(
                    ligaCandidatoId,
                    autorId,
                    "Convite enviado a partir de candidatura no mural global.");
            return new ResultadoCandidaturaPublicacaoLigaDTO(
                    "CONVITE_AUTOR",
                    "Convite enviado ao autor para entrar na sua liga antes do jogo valer pela liga.",
                    ligaCandidatoId);
        }

        partidaLigaService.criarJogoDaLiga(ligaCandidatoId, criarDesafio(publicacao, candidatoId));
        return new ResultadoCandidaturaPublicacaoLigaDTO(
                "DESAFIO_LIGA",
                "Candidatura enviada como desafio vinculado a liga selecionada.",
                ligaCandidatoId);
    }

    private PublicacaoLigaDTO preencherFlags(PublicacaoLigaDTO dto, PublicacaoLiga publicacao, Long meuTimeId) {
        dto.setSemLiga(publicacao.getLiga() == null);
        dto.setPodeDesafiarDireto(publicacao.getLiga() == null
                || (meuTimeId != null && ehMembro(publicacao.getLiga(), meuTimeId)));
        dto.setPrecisaSolicitarEntrada(publicacao.getLiga() != null
                && meuTimeId != null
                && !ehMembro(publicacao.getLiga(), meuTimeId));
        dto.setPrecisaConvidarAutor(false);
        return dto;
    }

    private DesafioDTO criarDesafio(PublicacaoLiga publicacao, Long candidatoId) {
        DesafioDTO desafio = new DesafioDTO();
        desafio.setIdTimeDesafiante(candidatoId);
        desafio.setIdTimeDesafiado(publicacao.getTimeAutor().getId());
        desafio.setCategoria(publicacao.getCategoria());
        desafio.setMensagem(publicacao.getObservacao());
        desafio.setDataHoraPartida(dataHoraDaPublicacao(publicacao));
        return desafio;
    }

    private LocalDateTime dataHoraDaPublicacao(PublicacaoLiga publicacao) {
        LocalDateTime dataJogo = publicacao.getDataJogo();
        if (publicacao.getHoraInicio() == null) {
            return dataJogo;
        }

        try {
            return LocalDateTime.of(dataJogo.toLocalDate(), LocalTime.parse(publicacao.getHoraInicio()));
        } catch (RuntimeException e) {
            return dataJogo;
        }
    }

    private boolean ehMembro(Liga liga, Long timeId) {
        return liga != null && timeId != null && liga.getTimes().stream().anyMatch(time -> timeId.equals(time.getId()));
    }

    private Long idLiga(PublicacaoLiga publicacao) {
        return publicacao.getLiga() != null ? publicacao.getLiga().getId() : null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return texto.trim();
    }
}
