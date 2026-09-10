package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusGestaoPartida;

import java.time.LocalDateTime;

/** DTO JavaBean compatível com o resolvedor de propriedades do Jakarta Faces. */
public final class ResumoHistoricoGestaoPartidaDTO {
    private final Long partidaId;
    private final LocalDateTime dataHora;
    private final String nomeTimeMandante;
    private final String escudoTimeMandante;
    private final String nomeTimeVisitante;
    private final String escudoTimeVisitante;
    private final Integer golsMandante;
    private final Integer golsVisitante;
    private final StatusGestaoPartida statusGestao;

    public ResumoHistoricoGestaoPartidaDTO(Long partidaId, LocalDateTime dataHora,
                                           String nomeTimeMandante, String escudoTimeMandante,
                                           String nomeTimeVisitante, String escudoTimeVisitante,
                                           Integer golsMandante, Integer golsVisitante,
                                           StatusGestaoPartida statusGestao) {
        this.partidaId = partidaId;
        this.dataHora = dataHora;
        this.nomeTimeMandante = nomeTimeMandante;
        this.escudoTimeMandante = escudoTimeMandante;
        this.nomeTimeVisitante = nomeTimeVisitante;
        this.escudoTimeVisitante = escudoTimeVisitante;
        this.golsMandante = golsMandante;
        this.golsVisitante = golsVisitante;
        this.statusGestao = statusGestao;
    }

    public Long getPartidaId() { return partidaId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getNomeTimeMandante() { return nomeTimeMandante; }
    public String getEscudoTimeMandante() { return escudoTimeMandante; }
    public String getNomeTimeVisitante() { return nomeTimeVisitante; }
    public String getEscudoTimeVisitante() { return escudoTimeVisitante; }
    public Integer getGolsMandante() { return golsMandante; }
    public Integer getGolsVisitante() { return golsVisitante; }
    public StatusGestaoPartida getStatusGestao() { return statusGestao; }

    // Preserva o contrato usado pelos consumidores Java quando este DTO era um record.
    public Long partidaId() { return partidaId; }
    public LocalDateTime dataHora() { return dataHora; }
    public String nomeTimeMandante() { return nomeTimeMandante; }
    public String escudoTimeMandante() { return escudoTimeMandante; }
    public String nomeTimeVisitante() { return nomeTimeVisitante; }
    public String escudoTimeVisitante() { return escudoTimeVisitante; }
    public Integer golsMandante() { return golsMandante; }
    public Integer golsVisitante() { return golsVisitante; }
    public StatusGestaoPartida statusGestao() { return statusGestao; }
}
