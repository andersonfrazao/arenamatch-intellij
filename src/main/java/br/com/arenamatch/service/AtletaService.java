package br.com.arenamatch.service;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.AtletaRequestDTO;
import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.AtletaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;

    public AtletaService(AtletaRepository atletaRepository, UsuarioRepository usuarioRepository,
                         TimeRepository timeRepository) {
        this.atletaRepository = atletaRepository;
        this.usuarioRepository = usuarioRepository;
        this.timeRepository = timeRepository;
    }

    @Transactional(readOnly = true)
    public List<AtletaDTO> listar() {
        return atletaRepository.findByTimeIdOrderByNomeAsc(timeAutenticado().getId()).stream()
                .map(this::converter).toList();
    }

    @Transactional
    public AtletaDTO criar(AtletaRequestDTO request) {
        validar(request);
        Atleta atleta = new Atleta();
        atleta.setTime(timeAutenticado());
        atleta.setNome(request.nome().trim());
        atleta.setApelido(normalizar(request.apelido()));
        return converter(atletaRepository.save(atleta));
    }

    @Transactional
    public AtletaDTO atualizar(Long id, AtletaRequestDTO request) {
        validar(request);
        Atleta atleta = atletaDoTime(id);
        atleta.setNome(request.nome().trim());
        atleta.setApelido(normalizar(request.apelido()));
        return converter(atletaRepository.save(atleta));
    }

    @Transactional
    public AtletaDTO alterarSituacao(Long id, SituacaoAtleta situacao) {
        if (situacao == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Situacao obrigatoria.");
        Atleta atleta = atletaDoTime(id);
        atleta.setSituacao(situacao);
        return converter(atletaRepository.save(atleta));
    }

    private Atleta atletaDoTime(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta nao encontrado."));
        if (!atleta.getTime().getId().equals(timeAutenticado().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Atleta nao pertence ao time autenticado.");
        return atleta;
    }

    private Time timeAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null || "anonymousUser".equals(principal.toString()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        Usuario usuario = usuarioRepository.findByEmail(principal.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado."));
        if (usuario.getPlanoAssinatura() != PlanoAssinatura.PRO
                || usuario.getStatusPagamento() != StatusPagamento.PAGO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Assine o plano PRO para acessar a gestao do seu time!");
        }
        return timeRepository.findByResponsavel(usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time nao encontrado."));
    }

    private void validar(AtletaRequestDTO request) {
        if (request == null || request.nome() == null || request.nome().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do atleta e obrigatorio.");
        if (request.nome().trim().length() > 150)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do atleta deve ter ate 150 caracteres.");
    }

    private String normalizar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private AtletaDTO converter(Atleta atleta) {
        return new AtletaDTO(atleta.getId(), atleta.getNome(), atleta.getApelido(), atleta.getSituacao());
    }
}
