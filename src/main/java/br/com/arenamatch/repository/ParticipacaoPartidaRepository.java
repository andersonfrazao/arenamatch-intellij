package br.com.arenamatch.repository;

import br.com.arenamatch.entity.ParticipacaoPartida;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipacaoPartidaRepository extends JpaRepository<ParticipacaoPartida, Long> {
    List<ParticipacaoPartida> findByGestaoPartidaIdOrderByOrdemAsc(Long gestaoPartidaId);

    @Query("""
            SELECT pp FROM ParticipacaoPartida pp
            JOIN pp.gestaoPartida gp
            JOIN gp.partida p
            WHERE pp.atleta.id = :atletaId
              AND gp.time.id = :timeId
              AND gp.status = br.com.arenamatch.enums.StatusGestaoPartida.PUBLICADO
              AND p.statusPlacar = br.com.arenamatch.enums.StatusPlacar.CONFIRMADO
              AND p.status NOT IN (br.com.arenamatch.enums.StatusPartida.CANCELADO,
                                   br.com.arenamatch.enums.StatusPartida.EXPIRADO)
              AND p.dataHora >= :inicio AND p.dataHora < :fim
            ORDER BY p.dataHora DESC, p.id DESC
            """)
    Page<ParticipacaoPartida> buscarHistoricoEstatistico(
            @Param("timeId") Long timeId, @Param("atletaId") Long atletaId,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
            Pageable pageable);
}
