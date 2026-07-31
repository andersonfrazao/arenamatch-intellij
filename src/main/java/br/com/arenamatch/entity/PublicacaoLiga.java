package br.com.arenamatch.entity;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.StatusPublicacaoLiga;
import br.com.arenamatch.enums.TipoProcuraPublicacaoLiga;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "publicacao_liga")
public class PublicacaoLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_publicacao_liga_gen")
    @SequenceGenerator(name = "seq_publicacao_liga_gen", sequenceName = "publicacao_liga_id_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "liga_id")
    private Liga liga;

    @ManyToOne
    @JoinColumn(name = "time_autor_id", nullable = false)
    private Time timeAutor;

    @Column(name = "data_jogo", nullable = false)
    private LocalDateTime dataJogo;

    @Column(name = "hora_inicio", length = 5)
    private String horaInicio;

    @Column(name = "hora_fim", length = 5)
    private String horaFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_procura", nullable = false, length = 20)
    private TipoProcuraPublicacaoLiga tipoProcura;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Categoria categoria;

    @Column(length = 120)
    private String regiao;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPublicacaoLiga status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
}
