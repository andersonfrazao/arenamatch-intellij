package br.com.arenamatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "adversario_externo")
@Getter
@Setter
public class AdversarioExterno {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_adversario_externo_gen")
    @SequenceGenerator(name = "seq_adversario_externo_gen", sequenceName = "seq_adversario_externo", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_time", nullable = false, updatable = false)
    private Time time;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false, length = 120)
    private String nomeNormalizado;
}
