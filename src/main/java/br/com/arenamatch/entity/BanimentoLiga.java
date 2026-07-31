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
@Table(name = "banimento_liga")
public class BanimentoLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_banimento_liga_gen")
    @SequenceGenerator(name = "seq_banimento_liga_gen", sequenceName = "banimento_liga_id_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    @ManyToOne
    @JoinColumn(name = "time_banido_id", nullable = false)
    private Time timeBanido;

    @ManyToOne
    @JoinColumn(name = "time_admin_id", nullable = false)
    private Time admin;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "data_banimento", nullable = false)
    private LocalDateTime dataBanimento;

    @Column(nullable = false)
    private boolean ativo = true;
}
