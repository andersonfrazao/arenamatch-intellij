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
        Integer duracaoMinutos,
        List<ParticipacaoRequestDTO> participacoes,
        List<EventoRequestDTO> eventos,
        List<SubstituicaoRequestDTO> substituicoes) {

    public GestaoPartidaRequestDTO(Long versao, EtapaGestaoPartida etapa, String formacao,
                                   String formacaoPersonalizada, List<ParticipacaoRequestDTO> participacoes,
                                   List<EventoRequestDTO> eventos) {
        this(versao, etapa, formacao, formacaoPersonalizada, null, participacoes, eventos, List.of());
    }

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

    public record SubstituicaoRequestDTO(
            Long atletaSaiuId,
            Long atletaEntrouId,
            Integer minuto,
            Integer ordem) {
    }
}
