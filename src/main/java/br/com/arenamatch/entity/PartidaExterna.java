package br.com.arenamatch.entity;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.MandoPartida;
import br.com.arenamatch.enums.Modalidade;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "partida_externa")
@Getter
@Setter
public class PartidaExterna {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_partida_externa_gen")
    @SequenceGenerator(name = "seq_partida_externa_gen", sequenceName = "seq_partida_externa", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_time", nullable = false, updatable = false)
    private Time time;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_adversario", nullable = false)
    private AdversarioExterno adversario;

    @Column(name = "nome_adversario", nullable = false, length = 120)
    private String nomeAdversario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MandoPartida mando;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Column(name = "nome_campo", nullable = false, length = 160)
    private String nomeCampo;

    @Column(length = 300)
    private String endereco;

    @Column(name = "valor_taxa", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTaxa;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "gols_mandante")
    private Integer golsMandante;

    @Column(name = "gols_visitante")
    private Integer golsVisitante;

    @Column(nullable = false, updatable = false, length = 20)
    private String origem = "MANUAL";

    @Column(name = "chave_solicitacao", nullable = false, updatable = false)
    private UUID chaveSolicitacao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor", nullable = false, updatable = false)
    private Usuario autor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    void registrarCriacao() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    void registrarAtualizacao() {
        atualizadoEm = LocalDateTime.now();
    }
}
