package br.com.arenamatch.repository;

import br.com.arenamatch.entity.PublicacaoLiga;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacaoLigaRepository extends JpaRepository<PublicacaoLiga, Long> {

    @Query("""
            SELECT p FROM PublicacaoLiga p
            JOIN FETCH p.timeAutor
            LEFT JOIN FETCH p.liga
            WHERE p.liga.id = :ligaId
            ORDER BY p.dataCriacao DESC, p.id DESC
        """)
    List<PublicacaoLiga> buscarPorLiga(@Param("ligaId") Long ligaId);

    @Query("""
            SELECT p FROM PublicacaoLiga p
            JOIN FETCH p.timeAutor
            LEFT JOIN FETCH p.liga l
            WHERE p.status = br.com.arenamatch.enums.StatusPublicacaoLiga.ABERTO
              AND (p.dataExpiracao IS NULL OR p.dataExpiracao > CURRENT_TIMESTAMP)
            ORDER BY p.dataCriacao DESC, p.id DESC
        """)
    List<PublicacaoLiga> buscarMuralGlobalAberto();
}
