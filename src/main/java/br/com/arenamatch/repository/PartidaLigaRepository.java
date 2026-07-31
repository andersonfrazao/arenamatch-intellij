package br.com.arenamatch.repository;

import br.com.arenamatch.entity.PartidaLiga;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaLigaRepository extends JpaRepository<PartidaLiga, Long> {

    Optional<PartidaLiga> findByPartidaId(Long partidaId);

    boolean existsByPartidaId(Long partidaId);

    @Query("""
            SELECT pl FROM PartidaLiga pl
            JOIN FETCH pl.partida p
            JOIN FETCH p.mandante
            JOIN FETCH p.visitante
            WHERE pl.liga.id = :ligaId
            ORDER BY p.dataHora ASC
        """)
    List<PartidaLiga> buscarPorLiga(@Param("ligaId") Long ligaId);

    @Query("""
            SELECT pl FROM PartidaLiga pl
            JOIN FETCH pl.liga l
            JOIN FETCH pl.partida p
            JOIN FETCH p.mandante
            JOIN FETCH p.visitante
            WHERE p.status = br.com.arenamatch.enums.StatusPartida.FINALIZADO
            ORDER BY p.dataHora DESC, p.id DESC
        """)
    List<PartidaLiga> buscarJogosRecentes();

    long countByLigaId(Long ligaId);
}
