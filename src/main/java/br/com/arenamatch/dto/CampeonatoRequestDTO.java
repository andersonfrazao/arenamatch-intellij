package br.com.arenamatch.dto;

import br.com.arenamatch.enums.FormatoCampeonato;
import br.com.arenamatch.enums.CriterioDesempate;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CampeonatoRequestDTO implements Serializable {
    private String nome;
    private String descricao;
    private String categoria = "Livre";
    private Integer idadeMinima;
    private String regulamento;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String diasSugeridos;
    private LocalTime horarioInicioSugerido;
    private BigDecimal taxaInscricao = BigDecimal.ZERO;
    private String premiacoes;
    private FormatoCampeonato formato = FormatoCampeonato.LIGA;
    private boolean idaVoltaClassificatoria;
    private boolean idaVoltaEliminatoria;
    private Integer quantidadeTimes;
    private Integer quantidadeGrupos;
    private Integer timesPorGrupo;
    private Integer classificadosPorGrupo;
    private Integer pontosVitoria = 3;
    private Integer pontosEmpate = 1;
    private Integer pontosDerrota = 0;
    private List<CriterioDesempate> desempates = new ArrayList<>(List.of(
            CriterioDesempate.VITORIAS, CriterioDesempate.SALDO_GOLS, CriterioDesempate.GOLS_PRO));
    private Long versao;
}
