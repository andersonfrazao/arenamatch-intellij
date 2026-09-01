package br.com.arenamatch.dto;

import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.TipoEventoSumula;
import java.math.BigDecimal;
import java.util.List;

public record GestaoPartidaRequestDTO(
        Long versao,
        EtapaGestaoPartida etapa,
        String formacao,
        String formacaoPersonalizada,
        List<ParticipacaoRequestDTO> participacoes,
        List<EventoRequestDTO> eventos) {

    public record ParticipacaoRequestDTO(
            Long atletaId,
            PapelParticipacao papel,
            Integer numeroCamisa,
            String posicao,
            String slotTatico,
            BigDecimal coordenadaX,
            BigDecimal coordenadaY,
            Integer ordem) {
    }

    public record EventoRequestDTO(
            Long atletaId,
            TipoEventoSumula tipo,
            Integer minuto) {
    }
}
