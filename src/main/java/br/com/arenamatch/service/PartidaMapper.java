package br.com.arenamatch.service;

import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import org.springframework.stereotype.Component;

@Component
public class PartidaMapper {

    public PartidaDTO toDTO(Partida partida) {
        PartidaDTO dto = new PartidaDTO();
        dto.setId(partida.getId());
        dto.setDataHora(partida.getDataHora());
        dto.setStatus(partida.getStatus());
        dto.setGolsMandante(partida.getGolsMandante());
        dto.setGolsVisitante(partida.getGolsVisitante());
        dto.setStatusPlacar(partida.getStatusPlacar());
        dto.setMotivoCancelamento(partida.getMotivoCancelamento());
        dto.setDataSolicitacao(partida.getDataSolicitacao());

        dto.setMandante(toResumo(partida.getMandante()));
        dto.setVisitante(toResumo(partida.getVisitante()));
        dto.setSolicitanteCancelamento(toResumo(partida.getSolicitanteCancelamento()));

        return dto;
    }

    private TimeResumoDTO toResumo(Time time) {
        if (time == null) {
            return null;
        }

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
