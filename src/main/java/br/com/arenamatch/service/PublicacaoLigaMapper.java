package br.com.arenamatch.service;

import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.PublicacaoLiga;
import org.springframework.stereotype.Component;

@Component
public class PublicacaoLigaMapper {

    public PublicacaoLigaDTO toDTO(PublicacaoLiga publicacao) {
        PublicacaoLigaDTO dto = new PublicacaoLigaDTO();
        dto.setId(publicacao.getId());
        if (publicacao.getLiga() != null) {
            dto.setIdLiga(publicacao.getLiga().getId());
            dto.setNomeLiga(publicacao.getLiga().getNome());
        }
        dto.setSemLiga(publicacao.getLiga() == null);
        dto.setTimeAutor(new TimeSimplesDTO(publicacao.getTimeAutor().getId(), publicacao.getTimeAutor().getNome()));
        dto.setDataJogo(publicacao.getDataJogo());
        dto.setHoraInicio(publicacao.getHoraInicio());
        dto.setHoraFim(publicacao.getHoraFim());
        dto.setTipoProcura(publicacao.getTipoProcura());
        dto.setCategoria(publicacao.getCategoria());
        dto.setRegiao(publicacao.getRegiao());
        dto.setObservacao(publicacao.getObservacao());
        dto.setDataExpiracao(publicacao.getDataExpiracao());
        dto.setStatus(publicacao.getStatus());
        dto.setDataCriacao(publicacao.getDataCriacao());
        return dto;
    }
}
