package br.com.arenamatch.service;

import br.com.arenamatch.dto.AdminUsuarioEdicaoDTO;
import br.com.arenamatch.dto.AdminUsuarioResumoDTO;
import br.com.arenamatch.entity.Usuario;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class AdminUsuarioMapper {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminUsuarioResumoDTO toResumo(Usuario usuario) {
        AdminUsuarioResumoDTO dto = new AdminUsuarioResumoDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setPerfil(usuario.getPerfil());
        dto.setStatusUsuario(usuario.getStatusUsuario());
        dto.setPlanoAssinatura(usuario.getPlanoAssinatura());
        dto.setStatusPagamento(usuario.getStatusPagamento());
        dto.setDataExpiracao(formatar(usuario.getDataExpiracao()));
        return dto;
    }

    public AdminUsuarioEdicaoDTO toEdicao(Usuario usuario) {
        AdminUsuarioEdicaoDTO dto = new AdminUsuarioEdicaoDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setPerfil(usuario.getPerfil());
        dto.setStatusUsuario(usuario.getStatusUsuario());
        dto.setPlanoAssinatura(usuario.getPlanoAssinatura());
        dto.setStatusPagamento(usuario.getStatusPagamento());
        dto.setDataExpiracao(formatar(usuario.getDataExpiracao()));
        return dto;
    }

    public LocalDateTime parseDataExpiracao(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(valor.trim(), DATA_HORA);
    }

    private String formatar(LocalDateTime data) {
        return data != null ? data.format(DATA_HORA) : "";
    }
}
