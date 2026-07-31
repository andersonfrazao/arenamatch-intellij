package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NovaPublicacaoLigaDTO {
    private Long idLiga;
    private Long idTimeAutor;
    private LocalDateTime dataJogo;
    private String horaInicio;
    private String horaFim;
    private TipoProcuraPublicacaoLiga tipoProcura;
    private Categoria categoria;
    private String regiao;
    private String observacao;
    private LocalDateTime dataExpiracao;
}
