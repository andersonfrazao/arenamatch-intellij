package br.com.arenamatch.service;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Liga;
import org.springframework.stereotype.Component;

@Component
public class LigaMapper {

    public LigaDetalheDTO toDetalheDTO(Liga liga) {
        LigaDetalheDTO dto = new LigaDetalheDTO();
        dto.setId(liga.getId());
        dto.setNome(liga.getNome());
        dto.setDescricao(liga.getDescricao());

        if (liga.getAdmin() != null) {
            dto.setAdmin(new TimeSimplesDTO(liga.getAdmin().getId(), liga.getAdmin().getNome()));
        }

        dto.setTimes(liga.getTimes().stream()
                .map(time -> new TimeSimplesDTO(time.getId(), time.getNome()))
                .toList());
        return dto;
    }

    public ConviteLigaDTO toConvitePendenteDTO(ConviteLiga convite) {
        ConviteLigaDTO dto = toConviteAgendaDTO(convite);
        LigaDetalheDTO ligaDTO = new LigaDetalheDTO();
        ligaDTO.setId(convite.getLiga().getId());
        ligaDTO.setNome(convite.getLiga().getNome());
        ligaDTO.setAdmin(new TimeSimplesDTO(convite.getLiga().getAdmin().getId(), convite.getLiga().getAdmin().getNome()));
        dto.setLiga(ligaDTO);
        return dto;
    }

    public ConviteLigaDTO toConviteAgendaDTO(ConviteLiga convite) {
        ConviteLigaDTO dto = new ConviteLigaDTO();
        dto.setId(convite.getId());
        dto.setMensagem(convite.getMensagem());
        dto.setStatus(convite.getStatus());
        dto.setDataConvite(convite.getDataConvite());

        LigaDetalheDTO ligaDTO = new LigaDetalheDTO();
        ligaDTO.setId(convite.getLiga().getId());
        ligaDTO.setNome(convite.getLiga().getNome());
        dto.setLiga(ligaDTO);
        return dto;
    }

    public LigaExplorarDTO toExplorarDTO(Liga liga, Long meuTimeId, boolean convitePendente) {
        return toExplorarDTO(liga, meuTimeId, convitePendente, 0, 0);
    }

    public LigaExplorarDTO toExplorarDTO(
            Liga liga,
            Long meuTimeId,
            boolean convitePendente,
            long qtdPublicacoesAbertas,
            long qtdJogos) {
        LigaExplorarDTO dto = new LigaExplorarDTO();
        dto.setId(liga.getId());
        dto.setNome(liga.getNome());

        if (liga.getAdmin() != null) {
            dto.setNomeTimeAdmin(liga.getAdmin().getNome());
            dto.setSouAdmin(liga.getAdmin().getId().equals(meuTimeId));
        }

        dto.setQtdTimes(liga.getTimes() != null ? liga.getTimes().size() : 0);
        dto.setJaParticipa(liga.getTimes().stream().anyMatch(time -> time.getId().equals(meuTimeId)));
        dto.setConvitePendente(convitePendente);
        dto.setQtdPublicacoesAbertas(qtdPublicacoesAbertas);
        dto.setQtdJogos(qtdJogos);
        dto.setMovimentacao(dto.getQtdTimes() + qtdPublicacoesAbertas + qtdJogos);
        return dto;
    }
}
