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
    /** Dias e horários da agenda do time. O jogo só pode ser registrado num desses dias da semana. */
    private List<HorarioAgendaTimeDTO> horarios;
    /** Dados do cadastro, preenchidos somente quando o time é mandante (o jogo é no campo dele). */
    private BigDecimal valorTaxa;
    private String nomeCampo;
    private String endereco;
}
