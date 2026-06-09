package br.com.arenamatch.service;

import br.com.arenamatch.dto.AdminUsuarioEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioResumoDTO;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.repository.UsuarioRepository;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUsuarioService {

    private static final int LIMITE_BUSCA = 20;

    private final UsuarioRepository usuarioRepository;
    private final AdminUsuarioMapper adminUsuarioMapper;

    public AdminUsuarioService(UsuarioRepository usuarioRepository, AdminUsuarioMapper adminUsuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.adminUsuarioMapper = adminUsuarioMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminUsuarioResumoDTO> buscarUsuarios(String termo) {
        validarAdminAutenticado();

        String termoNormalizado = termo != null ? termo.trim() : "";
        if (termoNormalizado.length() < 3) {
            return List.of();
        }

        Pageable limite = PageRequest.of(0, LIMITE_BUSCA);
        return usuarioRepository.buscarPorNomeOuEmail(termoNormalizado, limite)
                .stream()
                .map(adminUsuarioMapper::toResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUsuarioEdicaoDTO buscarUsuario(Long id) {
        validarAdminAutenticado();
        return adminUsuarioMapper.toEdicao(buscarUsuarioObrigatorio(id));
    }

    @Transactional
    public AdminUsuarioEdicaoDTO atualizarUsuario(AdminUsuarioEdicaoDTO dto) {
        Usuario admin = validarAdminAutenticado();
        Usuario usuario = buscarUsuarioObrigatorio(dto.getId());

        if (usuario.getId().equals(admin.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e permitido alterar o proprio usuario pela tela administrativa.");
        }

        try {
            if (dto.getPerfil() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil do usuario deve ser informado.");
            }

            usuario.setPerfil(dto.getPerfil());
            usuario.setStatusUsuario(dto.getStatusUsuario());
            usuario.setPlanoAssinatura(dto.getPlanoAssinatura());
            usuario.setStatusAssinatura(dto.getStatusAssinatura());
            usuario.setStatusPagamento(dto.getStatusPagamento());
            usuario.setDataExpiracao(adminUsuarioMapper.parseDataExpiracao(dto.getDataExpiracao()));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de expiracao invalida. Use o formato dd/MM/yyyy HH:mm.");
        }

        return adminUsuarioMapper.toEdicao(usuarioRepository.save(usuario));
    }

    private Usuario validarAdminAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                : null;

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado."));

        if (!Perfil.ADMIN.equals(usuario.getPerfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido somente para administradores.");
        }

        return usuario;
    }

    private Usuario buscarUsuarioObrigatorio(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario nao informado.");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
    }
}
