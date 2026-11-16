package br.com.arenamatch.dto;

import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.Modalidade;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OpcoesPartidaExternaDTO {
    private String nomeTime;
    private List<Modalidade> modalidades;
    private List<String> adversarios;
    /** Mando do time logado, definido no cadastro. O adversário joga com o mando oposto. */
    private MandoPartida mandoTime;
    /** Taxa do cadastro do time, preenchida somente quando ele é mandante. */
    private BigDecimal valorTaxa;
}
