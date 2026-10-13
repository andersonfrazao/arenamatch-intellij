package br.com.arenamatch.entity;

import br.com.arenamatch.enums.FormatoCampeonato;
import br.com.arenamatch.enums.StatusCampeonato;
import br.com.arenamatch.enums.CriterioDesempate;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "campeonato") @Getter @Setter
public class Campeonato {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_campeonato_gen")
    @SequenceGenerator(name = "seq_campeonato_gen", sequenceName = "seq_campeonato", allocationSize = 1)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizador", nullable = false)
    private Usuario organizador;
    @Version private Long versao;
    @Column(nullable = false, length = 150) private String nome;
    @Column(length = 4000) private String descricao;
    @Column(nullable = false, length = 80) private String categoria;
    @Column(name = "idade_minima") private Integer idadeMinima;
    @Column(length = 10000) private String regulamento;
    @Column(name = "data_inicio") private LocalDate dataInicio;
    @Column(name = "data_fim") private LocalDate dataFim;
    @Column(name = "dias_sugeridos", length = 200) private String diasSugeridos;
    @Column(name = "horario_inicio_sugerido") private LocalTime horarioInicioSugerido;
    @Column(name = "taxa_inscricao", nullable = false, precision = 12, scale = 2) private BigDecimal taxaInscricao;
    @Column(length = 4000) private String premiacoes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private FormatoCampeonato formato;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StatusCampeonato status = StatusCampeonato.RASCUNHO;
    @Column(name = "ida_volta_classificatoria", nullable = false) private boolean idaVoltaClassificatoria;
    @Column(name = "ida_volta_eliminatoria", nullable = false) private boolean idaVoltaEliminatoria;
    @Column(name = "quantidade_times") private Integer quantidadeTimes;
    @Column(name = "quantidade_grupos") private Integer quantidadeGrupos;
    @Column(name = "times_por_grupo") private Integer timesPorGrupo;
    @Column(name = "classificados_por_grupo") private Integer classificadosPorGrupo;
    @Column(name = "pontos_vitoria", nullable = false) private Integer pontosVitoria;
    @Column(name = "pontos_empate", nullable = false) private Integer pontosEmpate;
    @Column(name = "pontos_derrota", nullable = false) private Integer pontosDerrota;
    @ElementCollection @CollectionTable(name = "campeonato_desempate", joinColumns = @JoinColumn(name = "id_campeonato"))
    @OrderColumn(name = "ordem") @Enumerated(EnumType.STRING) @Column(name = "criterio", nullable = false, length = 30)
    private List<CriterioDesempate> desempates = new ArrayList<>();
    @Column(name = "tabela_publicada", nullable = false) private boolean tabelaPublicada;
    @Column(name = "data_criacao", nullable = false, updatable = false) private LocalDateTime dataCriacao;
    @Column(name = "data_alteracao", nullable = false) private LocalDateTime dataAlteracao;
    @PrePersist void criar() { dataCriacao = LocalDateTime.now(); dataAlteracao = dataCriacao; }
    @PreUpdate void atualizar() { dataAlteracao = LocalDateTime.now(); }
}
