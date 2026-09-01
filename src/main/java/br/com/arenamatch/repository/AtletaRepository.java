package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Atleta;
import br.com.arenamatch.enums.SituacaoAtleta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    List<Atleta> findByTimeIdOrderByNomeAsc(Long timeId);

    List<Atleta> findByTimeIdAndSituacaoOrderByNomeAsc(Long timeId, SituacaoAtleta situacao);
}
