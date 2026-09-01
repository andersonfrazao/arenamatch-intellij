package br.com.arenamatch.entity;

import br.com.arenamatch.enums.TipoEventoSumula;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evento_sumula")
@Getter
@Setter
@NoArgsConstructor
public class EventoSumula {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_evento_sumula_gen")
    @SequenceGenerator(name = "seq_evento_sumula_gen", sequenceName = "seq_evento_sumula", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_gestao_partida", nullable = false)
    private GestaoPartida gestaoPartida;

    @ManyToOne
    @JoinColumn(name = "id_participacao")
    private ParticipacaoPartida participacao;

    @ManyToOne
    @JoinColumn(name = "id_adversario")
    private Time adversario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoSumula tipo;

    private Integer minuto;

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
