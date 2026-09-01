package br.com.arenamatch.entity;

import br.com.arenamatch.enums.PapelParticipacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participacao_partida")
@Getter
@Setter
@NoArgsConstructor
public class ParticipacaoPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_participacao_partida_gen")
    @SequenceGenerator(name = "seq_participacao_partida_gen", sequenceName = "seq_participacao_partida", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_gestao_partida", nullable = false)
    private GestaoPartida gestaoPartida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atleta", nullable = false)
    private Atleta atleta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PapelParticipacao papel;

    @Column(name = "numero_camisa")
    private Integer numeroCamisa;

    @Column(length = 80)
    private String posicao;

    @Column(name = "slot_tatico", length = 40)
    private String slotTatico;

    @Column(name = "coordenada_x", precision = 5, scale = 2)
    private BigDecimal coordenadaX;

    @Column(name = "coordenada_y", precision = 5, scale = 2)
    private BigDecimal coordenadaY;

    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        dataCriacao = dataCriacao == null ? agora : dataCriacao;
        dataAlteracao = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        dataAlteracao = LocalDateTime.now();
    }
}
