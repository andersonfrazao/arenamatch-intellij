package br.com.arenamatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "partida_liga")
public class PartidaLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_partida_liga_gen")
    @SequenceGenerator(name = "seq_partida_liga_gen", sequenceName = "partida_liga_id_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    @Column(name = "data_vinculo", nullable = false)
    private LocalDateTime dataVinculo;

    @Column(name = "conta_ranking_liga", nullable = false)
    private boolean contaRankingLiga = true;
}
