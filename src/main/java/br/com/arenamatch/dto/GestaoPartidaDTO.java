package br.com.arenamatch.dto;

import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.StatusGestaoPartida;
import br.com.arenamatch.enums.TipoEventoSumula;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GestaoPartidaDTO(
        Long id,
        Long partidaId,
        Long timeId,
        StatusGestaoPartida status,
        EtapaGestaoPartida etapa,
        String formacao,
        String formacaoPersonalizada,
        Long versao,
        LocalDateTime dataAlteracao,
        LocalDateTime dataPublicacao,
        List<ParticipacaoDTO> participacoes,
        List<EventoDTO> eventos) {

    public record ParticipacaoDTO(
            Long id,
            Long atletaId,
            String nomeAtleta,
            PapelParticipacao papel,
            Integer numeroCamisa,
            String posicao,
            String slotTatico,
            BigDecimal coordenadaX,
            BigDecimal coordenadaY,
            Integer ordem) {
    }

    public record EventoDTO(
            Long id,
            Long atletaId,
            TipoEventoSumula tipo,
            Integer minuto) {
    }
}
