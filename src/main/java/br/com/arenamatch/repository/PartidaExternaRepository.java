package br.com.arenamatch.repository;

import br.com.arenamatch.entity.PartidaExterna;
import br.com.arenamatch.enums.Modalidade;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaExternaRepository extends JpaRepository<PartidaExterna, Long> {
    Optional<PartidaExterna> findByTimeIdAndChaveSolicitacao(Long timeId, UUID chaveSolicitacao);
    Optional<PartidaExterna> findByIdAndTimeId(Long id, Long timeId);

    List<PartidaExterna> findByTimeIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(
            Long timeId, LocalDateTime inicio, LocalDateTime fim);

    List<PartidaExterna> findByTimeIdAndDataHoraInicioGreaterThanEqualOrderByDataHoraInicioAsc(
            Long timeId, LocalDateTime inicio);

    boolean existsByTimeIdAndModalidadeAndDataHoraInicioBetween(
            Long timeId, Modalidade modalidade, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select count(p) > 0 from PartidaExterna p
            where p.time.id = :timeId and p.modalidade = :modalidade
              and p.dataHoraInicio < :fim and p.dataHoraFim > :inicio
            """)
    boolean existeConflito(@Param("timeId") Long timeId, @Param("modalidade") Modalidade modalidade,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
