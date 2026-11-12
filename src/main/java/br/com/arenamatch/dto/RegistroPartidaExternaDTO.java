package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.Modalidade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Data;

@Data
public class RegistroPartidaExternaDTO implements Serializable {
    private UUID chaveSolicitacao;
    private String nomeAdversario;
    private Modalidade modalidade;
    private Categoria categoria;
    private MandoPartida mando;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String nomeCampo;
    private String endereco;
    private BigDecimal valorTaxa;
    private String observacoes;
}
