package br.com.arenamatch.entity;

import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.StatusGestaoPartida;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestao_partida")
@Getter
@Setter
@NoArgsConstructor
public class GestaoPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gestao_partida_gen")
    @SequenceGenerator(name = "seq_gestao_partida_gen", sequenceName = "seq_gestao_partida", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_time", nullable = false)
    private Time time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusGestaoPartida status = StatusGestaoPartida.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EtapaGestaoPartida etapa = EtapaGestaoPartida.ESCALACAO;

    @Column(length = 30)
    private String formacao;

    @Column(name = "formacao_personalizada", length = 60)
    private String formacaoPersonalizada;

    @ManyToOne
    @JoinColumn(name = "id_criado_por")
    private Usuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "id_alterado_por")
    private Usuario alteradoPor;

    @ManyToOne
    @JoinColumn(name = "id_publicado_por")
    private Usuario publicadoPor;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    @Version
    @Column(nullable = false)
    private Long versao;

    @OneToMany(mappedBy = "gestaoPartida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacaoPartida> participacoes = new ArrayList<>();

    @OneToMany(mappedBy = "gestaoPartida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoSumula> eventos = new ArrayList<>();

    public void substituirParticipacoes(List<ParticipacaoPartida> novas) {
        participacoes.clear();
        novas.forEach(this::adicionarParticipacao);
    }

    public void adicionarParticipacao(ParticipacaoPartida participacao) {
        participacao.setGestaoPartida(this);
        participacoes.add(participacao);
    }

    public void substituirEventos(List<EventoSumula> novos) {
        eventos.clear();
        novos.forEach(this::adicionarEvento);
    }

    public void adicionarEvento(EventoSumula evento) {
        evento.setGestaoPartida(this);
        eventos.add(evento);
    }

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
