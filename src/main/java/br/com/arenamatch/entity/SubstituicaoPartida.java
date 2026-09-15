package br.com.arenamatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "substituicao_partida")
@Getter @Setter @NoArgsConstructor
public class SubstituicaoPartida {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_substituicao_partida_gen")
    @SequenceGenerator(name = "seq_substituicao_partida_gen", sequenceName = "seq_substituicao_partida", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_gestao_partida", nullable = false)
    private GestaoPartida gestaoPartida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_participacao_saiu", nullable = false)
    private ParticipacaoPartida participacaoSaiu;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_participacao_entrou", nullable = false)
    private ParticipacaoPartida participacaoEntrou;

    private Integer minuto;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        dataCriacao = dataCriacao == null ? agora : dataCriacao;
        dataAlteracao = agora;
    }
    @PreUpdate void aoAtualizar() { dataAlteracao = LocalDateTime.now(); }
}
