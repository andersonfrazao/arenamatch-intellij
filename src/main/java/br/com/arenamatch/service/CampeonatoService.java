package br.com.arenamatch.service;

import br.com.arenamatch.dto.CampeonatoDTO;
import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.StatusCampeonato;
import br.com.arenamatch.repository.CampeonatoRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampeonatoService {
    public static final int LIMITE_SIMULTANEOS = 3;
    private final CampeonatoRepository repository;
    private final UsuarioRepository usuarios;
    private final CampeonatoAuthorizationService authorization;
    private final CampeonatoValidator validator;

    public CampeonatoService(CampeonatoRepository repository, UsuarioRepository usuarios,
                             CampeonatoAuthorizationService authorization, CampeonatoValidator validator) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.authorization = authorization;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<CampeonatoDTO> listar() {
        Usuario usuario = authorization.usuarioAutenticado();
        return repository.findByOrganizadorIdOrderByDataCriacaoDesc(usuario.getId()).stream()
                .map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public CampeonatoDTO consultar(Long id) {
        return converter(doOrganizador(id, authorization.usuarioAutenticado()));
    }

    @Transactional
    public CampeonatoDTO criar(CampeonatoRequestDTO request) {
        Usuario usuario = authorization.exigirPremium();
        // Serializa a contagem/criacao por conta, inclusive em requisicoes concorrentes.
        Usuario organizador = usuarios.bloquearParaCriarCampeonato(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!CampeonatoAuthorizationService.possuiPremiumPago(organizador.getPlanoAssinatura(), organizador.getStatusPagamento())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    CampeonatoAuthorizationService.mensagemBloqueio(organizador.getPlanoAssinatura()));
        }
        validator.validar(request);
        if (repository.countByOrganizadorIdAndStatusNotIn(usuario.getId(),
                List.of(StatusCampeonato.FINALIZADO, StatusCampeonato.CANCELADO)) >= LIMITE_SIMULTANEOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você já possui três campeonatos abertos. Finalize ou cancele um para criar outro.");
        }
        Campeonato campeonato = new Campeonato();
        campeonato.setOrganizador(organizador);
        aplicar(request, campeonato);
        return converter(repository.saveAndFlush(campeonato));
    }

    @Transactional
    public CampeonatoDTO atualizar(Long id, CampeonatoRequestDTO request) {
        Campeonato campeonato = doOrganizador(id, authorization.exigirPremium());
        if (!campeonato.getStatus().ocupaVaga()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Campeonato encerrado não pode ser editado.");
        }
        validator.validar(request);
        if (request.getVersao() == null || !Objects.equals(request.getVersao(), campeonato.getVersao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O campeonato foi alterado. Atualize a página antes de editar.");
        }
        validator.validarAlteracaoPublicada(campeonato, request);
        aplicar(request, campeonato);
        return converter(repository.saveAndFlush(campeonato));
    }

    @Transactional
    public CampeonatoDTO cancelarRascunho(Long id, Long versao) {
        Campeonato campeonato = doOrganizador(id, authorization.exigirPremium());
        if (campeonato.getStatus() != StatusCampeonato.RASCUNHO || versao == null
                || !Objects.equals(versao, campeonato.getVersao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente um rascunho atualizado pode ser cancelado.");
        }
        campeonato.setStatus(StatusCampeonato.CANCELADO);
        return converter(repository.saveAndFlush(campeonato));
    }

    private Campeonato doOrganizador(Long id, Usuario usuario) {
        Campeonato campeonato = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campeonato não encontrado."));
        if (!Objects.equals(campeonato.getOrganizador().getId(), usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campeonato não encontrado.");
        }
        return campeonato;
    }

    private void aplicar(CampeonatoRequestDTO request, Campeonato campeonato) {
        BeanUtils.copyProperties(request, campeonato, "versao", "desempates");
        campeonato.setNome(request.getNome().trim());
        campeonato.setCategoria(request.getCategoria().trim());
        campeonato.getDesempates().clear();
        campeonato.getDesempates().addAll(request.getDesempates());
    }

    private CampeonatoDTO converter(Campeonato campeonato) {
        CampeonatoDTO dto = new CampeonatoDTO();
        BeanUtils.copyProperties(campeonato, dto, "desempates");
        dto.setDesempates(new ArrayList<>(campeonato.getDesempates()));
        dto.setAnoReferencia(campeonato.getDataInicio() == null ? null : campeonato.getDataInicio().getYear());
        return dto;
    }
}
