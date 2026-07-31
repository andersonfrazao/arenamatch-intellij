package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.StatusPublicacaoLiga;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PublicacaoLigaDTO {
    private Long id;
    private Long idLiga;
    private String nomeLiga;
    private TimeSimplesDTO timeAutor;
    private LocalDateTime dataJogo;
    private String horaInicio;
    private String horaFim;
    private TipoProcuraPublicacaoLiga tipoProcura;
    private Categoria categoria;
    private String regiao;
    private String observacao;
    private LocalDateTime dataExpiracao;
    private StatusPublicacaoLiga status;
    private LocalDateTime dataCriacao;
    private boolean semLiga;
    private boolean podeDesafiarDireto;
    private boolean precisaSolicitarEntrada;
    private boolean precisaConvidarAutor;
}
