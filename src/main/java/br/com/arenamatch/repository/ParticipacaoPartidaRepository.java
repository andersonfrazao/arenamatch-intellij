package br.com.arenamatch.repository;

import br.com.arenamatch.entity.ParticipacaoPartida;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipacaoPartidaRepository extends JpaRepository<ParticipacaoPartida, Long> {
    List<ParticipacaoPartida> findByGestaoPartidaIdOrderByOrdemAsc(Long gestaoPartidaId);
}
