package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.Modalidade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Data;

/**
 * Dados enviados para registrar uma partida externa.
 * O mando não é enviado: ele vem do cadastro do time (o adversário é sempre o oposto).
 */
@Data
public class RegistroPartidaExternaDTO implements Serializable {
    private UUID chaveSolicitacao;
    private String nomeAdversario;
    private Modalidade modalidade;
    private Categoria categoria;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String nomeCampo;
    private String endereco;
    private BigDecimal valorTaxa;
    private String observacoes;
    /** Placar opcional, aceito somente para jogo já realizado. Os dois valores vêm juntos ou nenhum. */
    private Integer golsMandante;
    private Integer golsVisitante;
}
