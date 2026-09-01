package br.com.arenamatch.repository;

import br.com.arenamatch.entity.GestaoPartida;
import br.com.arenamatch.enums.StatusGestaoPartida;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GestaoPartidaRepository extends JpaRepository<GestaoPartida, Long> {

    Optional<GestaoPartida> findByPartidaIdAndTimeId(Long partidaId, Long timeId);

    List<GestaoPartida> findByTimeIdAndStatusOrderByDataAlteracaoDesc(Long timeId, StatusGestaoPartida status);
}
