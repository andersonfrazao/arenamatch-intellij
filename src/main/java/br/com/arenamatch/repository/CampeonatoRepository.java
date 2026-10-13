package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.enums.StatusCampeonato;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {
    List<Campeonato> findByOrganizadorIdOrderByDataCriacaoDesc(Long organizadorId);
    long countByOrganizadorIdAndStatusNotIn(Long organizadorId, Collection<StatusCampeonato> status);
}
