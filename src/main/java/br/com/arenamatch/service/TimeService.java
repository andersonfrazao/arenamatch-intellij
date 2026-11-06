package br.com.arenamatch.service;

import java.util.List;
import br.com.arenamatch.enums.Modalidade;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.JogoRealizadoDTO;
import br.com.arenamatch.dto.AdversarioDetalhesDTO;
import br.com.arenamatch.dto.ConfrontoResumoDTO;
import br.com.arenamatch.dto.PaginaJogosRealizadosDTO;
import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class TimeService {

    private static final int TAMANHO_PAGINA_JOGOS = 10;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AssinaturaService assinaturaService;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private DistanciaService distanciaService;

    public Optional<TimeResumoDTO> buscarResumoPorResponsavel(Long idResponsavel) {
        return timeRepository.findByResponsavelId(idResponsavel)
                .map(this::converterParaResumoDTO);
    }
    
    public List<TimeSimplesDTO> buscarTimesPorNome(String nome){
	    List<br.com.arenamatch.entity.Time> resultados = timeRepository.buscarAtivosPorNome(nome);
	    
	    List<TimeSimplesDTO> dtos = resultados.stream()
	            .map(t -> new TimeSimplesDTO(t.getId(), t.getNome()))
	            .toList();
		
	    return dtos;
    }
    
    public List<TimeDTO> buscarRankingGeral() {
        return buscarRankingGeral(Modalidade.FUTEBOL_CAMPO);
    }

    public List<TimeDTO> buscarRankingGeral(Modalidade modalidade) {
        buscarUsuarioAutenticadoComAcessoDesempenho();
        return carregarRanking(modalidade);
    }

    private List<TimeDTO> carregarRanking(Modalidade modalidade) {
        if (modalidade == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione a modalidade.");
        return partidaRepository.buscarRankingPorModalidade(modalidade.name()).stream().map(t -> {
            TimeDTO dto = new TimeDTO();
            dto.setId(numeroLong(t.get("id")));
            dto.setNome(t.get("nome", String.class));
            dto.setEscudo(t.get("escudo", String.class));
            dto.setModalidade(modalidade);
            dto.setPontos(numeroLong(t.get("pontos")).intValue());
            dto.setPartidasJogadas(numeroLong(t.get("jogos")).intValue());
            dto.setVitorias(numeroLong(t.get("vitorias")).intValue());
            dto.setEmpates(numeroLong(t.get("empates")).intValue());
            dto.setDerrotas(numeroLong(t.get("derrotas")).intValue());
            dto.setGolsPro(numeroLong(t.get("golsPro")).intValue());
            dto.setGolsContra(numeroLong(t.get("golsContra")).intValue());
            return dto;
        }).toList();
    }

    private TimeDTO buscarEstatisticas(Time time, Modalidade modalidade) {
        return carregarRanking(modalidade).stream().filter(t -> t.getId().equals(time.getId())).findFirst()
                .orElseGet(() -> {
                    TimeDTO dto = new TimeDTO();
                    dto.setId(time.getId()); dto.setNome(time.getNome()); dto.setEscudo(time.getEscudo());
                    dto.setModalidade(modalidade); dto.setPontos(0); dto.setPartidasJogadas(0);
                    dto.setVitorias(0); dto.setEmpates(0); dto.setDerrotas(0); dto.setGolsPro(0); dto.setGolsContra(0);
                    return dto;
                });
    }

    public TimeDTO buscarScoutDoUsuarioAutenticado() { return buscarScoutDoUsuarioAutenticado(Modalidade.FUTEBOL_CAMPO); }

    public TimeDTO buscarScoutDoUsuarioAutenticado(Modalidade modalidade) {
        var usuario = buscarUsuarioAutenticadoComAcessoDesempenho();

        return timeRepository.findByResponsavel(usuario)
                .map(time -> buscarEstatisticas(time, modalidade))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time do usuario nao encontrado."));
    }

    public PaginaJogosRealizadosDTO buscarJogosRealizadosDoUsuarioAutenticado(int pagina) {
        return buscarJogosRealizadosDoUsuarioAutenticado(pagina, Modalidade.FUTEBOL_CAMPO);
    }

    public PaginaJogosRealizadosDTO buscarJogosRealizadosDoUsuarioAutenticado(int pagina, Modalidade modalidade) {
        var usuario = buscarUsuarioAutenticadoComAcessoDesempenho();
        Time meuTime = timeRepository.findByResponsavel(usuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time do usuario nao encontrado."));

        int paginaNormalizada = Math.max(pagina, 0);
        var resultado = partidaRepository.buscarJogosConfirmadosPorModalidade(
                meuTime.getId(), modalidade,
                PageRequest.of(paginaNormalizada, TAMANHO_PAGINA_JOGOS));

        List<JogoRealizadoDTO> jogos = resultado.getContent().stream()
                .map(partida -> converterParaJogoRealizado(partida, meuTime))
                .toList();

        return new PaginaJogosRealizadosDTO(jogos, resultado.hasNext());
    }

    public AdversarioDetalhesDTO buscarDetalhesAdversario(Long adversarioId) {
        return buscarDetalhesAdversario(adversarioId, Modalidade.FUTEBOL_CAMPO);
    }

    public AdversarioDetalhesDTO buscarDetalhesAdversario(Long adversarioId, Modalidade modalidade) {
        Usuario usuario = buscarUsuarioAutenticadoComAcessoDesempenho();
        Time meuTime = timeRepository.findByResponsavel(usuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time do usuario nao encontrado."));
        Time adversario = timeRepository.findById(adversarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Adversario nao encontrado."));

        if (meuTime.getId().equals(adversario.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O time informado nao e um adversario.");
        }
        if (adversario.getResponsavel() == null
                || adversario.getResponsavel().getStatusUsuario() != StatusUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Adversario nao encontrado.");
        }

        AdversarioDetalhesDTO dto = converterParaDetalhes(adversario, meuTime, modalidade);
        dto.setConfronto(buscarResumoConfronto(meuTime.getId(), adversario.getId(), modalidade));

        var recentes = partidaRepository.buscarJogosConfirmadosPorModalidade(
                adversario.getId(), modalidade, PageRequest.of(0, 5));
        dto.setResultadosRecentes(recentes.getContent().stream()
                .map(partida -> converterParaJogoRealizado(partida, adversario))
                .toList());

        List<TimeDTO> ranking = carregarRanking(modalidade);
        for (int indice = 0; indice < ranking.size(); indice++) {
            if (ranking.get(indice).getId().equals(adversario.getId())) {
                dto.setPosicaoRanking(indice + 1);
                break;
            }
        }
        return dto;
    }

    private Usuario buscarUsuarioAutenticadoComAcessoDesempenho() {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                : null;

        if (email == null || email.isBlank() || "null".equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado."));

        if (Perfil.ADMIN.equals(usuario.getPerfil())) {
            return usuario;
        }

        usuario = assinaturaService.atualizarTrialExpirado(usuario);
        if (!assinaturaService.temAcessoCompleto(usuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Ranking Geral e Meu Scout estao disponiveis no plano PRO.");
        }

        return usuario;
    }
    
    private AdversarioDetalhesDTO converterParaDetalhes(Time adversario, Time meuTime, Modalidade modalidade) {
        AdversarioDetalhesDTO dto = new AdversarioDetalhesDTO();
        dto.setId(adversario.getId());
        dto.setNome(adversario.getNome());
        dto.setEscudo(adversario.getEscudo());
        dto.setCategoria(adversario.getCategoria());
        dto.setModalidade(modalidade);
        dto.setCidade(adversario.getCidade());
        dto.setUf(adversario.getUf());
        dto.setRegiao(adversario.getRegiao());
        dto.setMandoCampo(adversario.isMandoCampo());
        dto.setDistanciaKm(distanciaService.calcularDistancia(
                meuTime.getLatitude(), meuTime.getLongitude(),
                adversario.getLatitude(), adversario.getLongitude()));
        TimeDTO estatisticas = buscarEstatisticas(adversario, modalidade);
        dto.setPartidasJogadas(estatisticas.getPartidasJogadas());
        dto.setVitorias(estatisticas.getVitorias());
        dto.setEmpates(estatisticas.getEmpates());
        dto.setDerrotas(estatisticas.getDerrotas());
        dto.setGolsPro(estatisticas.getGolsPro());
        dto.setGolsContra(estatisticas.getGolsContra());
        return dto;
    }

    private ConfrontoResumoDTO buscarResumoConfronto(Long meuTimeId, Long adversarioId, Modalidade modalidade) {
        return partidaRepository.buscarResumoConfrontosPorModalidade(meuTimeId, List.of(adversarioId), modalidade.name()).stream()
                .findFirst()
                .map(tuple -> new ConfrontoResumoDTO(
                        numeroLong(tuple.get("jogos")),
                        numeroLong(tuple.get("vitorias")),
                        numeroLong(tuple.get("empates")),
                        numeroLong(tuple.get("derrotas")),
                        numeroLong(tuple.get("golsMeuTime")),
                        numeroLong(tuple.get("golsAdversario"))))
                .orElseGet(() -> new ConfrontoResumoDTO(0L, 0L, 0L, 0L, 0L, 0L));
    }

    private Long numeroLong(Object valor) {
        return valor instanceof Number numero ? numero.longValue() : 0L;
    }

    private JogoRealizadoDTO converterParaJogoRealizado(Partida partida, Time meuTime) {
        boolean souMandante = partida.getMandante().getId().equals(meuTime.getId());
        Time adversario = souMandante ? partida.getVisitante() : partida.getMandante();

        JogoRealizadoDTO dto = new JogoRealizadoDTO();
        dto.setDataHora(partida.getDataHora());
        dto.setModalidade(partida.getModalidade());
        dto.setNomeMeuTime(meuTime.getNome());
        dto.setGolsMeuTime(souMandante ? partida.getGolsMandante() : partida.getGolsVisitante());
        dto.setGolsAdversario(souMandante ? partida.getGolsVisitante() : partida.getGolsMandante());
        dto.setNomeAdversario(adversario.getNome());
        return dto;
    }

    private TimeResumoDTO converterParaResumoDTO(br.com.arenamatch.entity.Time time) {
        TimeResumoDTO dto = new TimeResumoDTO(
                time.getId(),
                time.getNome(),
                time.getCidade(),
                time.getUf(),
                time.getRegiao(),
                time.isMandoCampo()
        );
        dto.setEscudo(time.getEscudo());
        dto.setModalidades(new java.util.ArrayList<>(time.getModalidades()));
        return dto;
    }

}
