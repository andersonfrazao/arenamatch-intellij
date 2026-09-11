package br.com.arenamatch.repository;

import br.com.arenamatch.entity.GestaoPartida;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface EstatisticasRepository extends Repository<GestaoPartida, Long> {

    interface ResumoTimeProjection {
        Number getJogos();
        Number getVitorias();
        Number getEmpates();
        Number getDerrotas();
        Number getGolsPro();
        Number getGolsContra();
    }

    interface ResumoJogadorProjection {
        Long getAtletaId();
        Number getPartidas();
        Number getGols();
        Number getCartoesAmarelos();
        Number getCartoesVermelhos();
    }

    @Query(value = """
            SELECT COUNT(*) AS jogos,
                   SUM(CASE WHEN x.gols_pro > x.gols_contra THEN 1 ELSE 0 END) AS vitorias,
                   SUM(CASE WHEN x.gols_pro = x.gols_contra THEN 1 ELSE 0 END) AS empates,
                   SUM(CASE WHEN x.gols_pro < x.gols_contra THEN 1 ELSE 0 END) AS derrotas,
                   COALESCE(SUM(x.gols_pro), 0) AS "golsPro",
                   COALESCE(SUM(x.gols_contra), 0) AS "golsContra"
              FROM (
                    SELECT CASE WHEN p.id_mandante = :timeId THEN p.gols_mandante ELSE p.gols_visitante END AS gols_pro,
                           CASE WHEN p.id_mandante = :timeId THEN p.gols_visitante ELSE p.gols_mandante END AS gols_contra
                      FROM partida p
                     WHERE (p.id_mandante = :timeId OR p.id_visitante = :timeId)
                       AND p.status_placar = 'CONFIRMADO'
                       AND p.status NOT IN ('CANCELADO', 'EXPIRADO')
                       AND p.data_hora >= :inicio AND p.data_hora < :fim
                   ) x
            """, nativeQuery = true)
    ResumoTimeProjection resumirTime(@Param("timeId") Long timeId,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);

    @Query(value = """
            SELECT pp.id_atleta AS "atletaId",
                   COUNT(DISTINCT gp.id) AS partidas,
                   SUM(CASE WHEN es.tipo = 'GOL' THEN 1 ELSE 0 END) AS gols,
                   SUM(CASE WHEN es.tipo = 'CARTAO_AMARELO' THEN 1 ELSE 0 END) AS "cartoesAmarelos",
                   SUM(CASE WHEN es.tipo = 'CARTAO_VERMELHO' THEN 1 ELSE 0 END) AS "cartoesVermelhos"
              FROM participacao_partida pp
              JOIN gestao_partida gp ON gp.id = pp.id_gestao_partida
              JOIN partida p ON p.id = gp.id_partida
              LEFT JOIN evento_sumula es ON es.id_participacao = pp.id
             WHERE gp.id_time = :timeId
               AND gp.status = 'PUBLICADO'
               AND p.status_placar = 'CONFIRMADO'
               AND p.status NOT IN ('CANCELADO', 'EXPIRADO')
               AND p.data_hora >= :inicio AND p.data_hora < :fim
             GROUP BY pp.id_atleta
            """, nativeQuery = true)
    List<ResumoJogadorProjection> resumirJogadores(@Param("timeId") Long timeId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim);
}
