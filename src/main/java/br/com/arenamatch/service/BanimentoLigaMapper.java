package br.com.arenamatch.service;

import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.BanimentoLiga;
import org.springframework.stereotype.Component;

@Component
public class BanimentoLigaMapper {

    public BanimentoLigaDTO toDTO(BanimentoLiga banimento) {
        BanimentoLigaDTO dto = new BanimentoLigaDTO();
        dto.setId(banimento.getId());
        dto.setIdLiga(banimento.getLiga().getId());
        dto.setTimeBanido(new TimeSimplesDTO(banimento.getTimeBanido().getId(), banimento.getTimeBanido().getNome()));
        dto.setAdmin(new TimeSimplesDTO(banimento.getAdmin().getId(), banimento.getAdmin().getNome()));
        dto.setMotivo(banimento.getMotivo());
        dto.setDataBanimento(banimento.getDataBanimento());
        dto.setAtivo(banimento.isAtivo());
        return dto;
    }
}
