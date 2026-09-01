package br.com.arenamatch.entity;

import br.com.arenamatch.enums.SituacaoAtleta;
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
@Table(name = "atleta")
@Getter
@Setter
@NoArgsConstructor
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_atleta_gen")
    @SequenceGenerator(name = "seq_atleta_gen", sequenceName = "seq_atleta", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_time", nullable = false)
    private Time time;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 80)
    private String apelido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAtleta situacao = SituacaoAtleta.ATIVO;

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
