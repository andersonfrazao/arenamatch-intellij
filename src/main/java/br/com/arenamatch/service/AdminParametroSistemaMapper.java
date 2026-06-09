package br.com.arenamatch.service;

import br.com.arenamatch.dto.AdminParametroSistemaDTO;
import br.com.arenamatch.entity.ParametroSistema;
import org.springframework.stereotype.Component;

@Component
public class AdminParametroSistemaMapper {

    public AdminParametroSistemaDTO toDto(ParametroSistema parametro) {
        AdminParametroSistemaDTO dto = new AdminParametroSistemaDTO();
        dto.setChave(parametro.getChave());
        dto.setValor(parametro.getValor());
        dto.setDescricao(parametro.getDescricao());
        return dto;
    }
}
