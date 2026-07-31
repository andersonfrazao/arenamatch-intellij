package br.com.arenamatch.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BanimentoLigaDTO {
    private Long id;
    private Long idLiga;
    private TimeSimplesDTO timeBanido;
    private TimeSimplesDTO admin;
    private String motivo;
    private LocalDateTime dataBanimento;
    private boolean ativo;
}
