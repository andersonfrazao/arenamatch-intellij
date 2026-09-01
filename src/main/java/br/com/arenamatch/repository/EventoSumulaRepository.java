package br.com.arenamatch.repository;

import br.com.arenamatch.entity.EventoSumula;
import br.com.arenamatch.enums.TipoEventoSumula;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoSumulaRepository extends JpaRepository<EventoSumula, Long> {
    List<EventoSumula> findByGestaoPartidaIdOrderByMinutoAscIdAsc(Long gestaoPartidaId);

    long countByGestaoPartidaIdAndTipo(Long gestaoPartidaId, TipoEventoSumula tipo);
}
