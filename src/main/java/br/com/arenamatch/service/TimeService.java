package br.com.arenamatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class TimeService {
	
    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AssinaturaService assinaturaService;

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
    
    public List<TimeDTO> buscarRankingGeral(){
        buscarUsuarioAutenticadoComAcessoDesempenho();

    	var times =  timeRepository.buscarRankingGeral();
    	
    	var dtos = times.stream().map(this::converterParaDTO).collect(Collectors.toList());
    	
    	return dtos;
    }

    public TimeDTO buscarScoutDoUsuarioAutenticado() {
        var usuario = buscarUsuarioAutenticadoComAcessoDesempenho();

        return timeRepository.findByResponsavel(usuario)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Time do usuario nao encontrado."));
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
    
 // Método auxiliar para fazer a conversão de forma limpa
    private TimeDTO converterParaDTO(br.com.arenamatch.entity.Time time) {
        TimeDTO dto = new TimeDTO();
        dto.setId(time.getId());
        dto.setNome(time.getNome());
        dto.setEscudo(time.getEscudo());
        
        // Dados do Ranking (usando os campos que criamos na V20)
        dto.setPontos(time.getPontos());
        dto.setPartidasJogadas(time.getPartidasJogadas());
        dto.setVitorias(time.getVitorias());
        dto.setEmpates(time.getEmpates());
        dto.setDerrotas(time.getDerrotas());
        dto.setGolsPro(time.getGolsPro());
        dto.setGolsContra(time.getGolsContra());
        
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
        return dto;
    }

}
