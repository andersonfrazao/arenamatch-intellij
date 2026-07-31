package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Liga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LigaRepository extends JpaRepository<Liga, Long> {
    
    // Buscar Ligas que o time é dono
    List<Liga> findByAdminId(Long adminId);
    
    // Buscar Ligas que o time faz parte (como membro)
    @Query("SELECT l FROM Liga l JOIN l.times t WHERE t.id = :timeId")
    List<Liga> buscarLigasDoTime(@Param("timeId") Long timeId);
    
 // 1. Busca as "Top Ligas" globais por movimentacao (times, anuncios abertos e jogos)
    @Query("""
            SELECT l FROM Liga l
            LEFT JOIN PublicacaoLiga pub
                ON pub.liga = l
               AND pub.status = br.com.arenamatch.enums.StatusPublicacaoLiga.ABERTO
            LEFT JOIN PartidaLiga pl ON pl.liga = l
            GROUP BY l
            ORDER BY (SIZE(l.times) + COUNT(DISTINCT pub.id) + COUNT(DISTINCT pl.id)) DESC, SIZE(l.times) DESC, l.nome ASC
        """)
    List<Liga> buscarLigasMaisMovimentadas();

    @Query("""
            SELECT COUNT(pub.id) FROM PublicacaoLiga pub
            WHERE pub.liga.id = :ligaId
              AND pub.status = br.com.arenamatch.enums.StatusPublicacaoLiga.ABERTO
        """)
    long contarPublicacoesAbertas(@Param("ligaId") Long ligaId);
    
    // 2. Busca ligas digitadas na barra de pesquisa
    @Query("SELECT l FROM Liga l WHERE LOWER(l.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY SIZE(l.times) DESC")
    List<Liga> buscarLigasPorNome(@Param("nome") String nome);
    
}
