package br.com.arenamatch.repository;

import br.com.arenamatch.entity.AdversarioExterno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdversarioExternoRepository extends JpaRepository<AdversarioExterno, Long> {
    Optional<AdversarioExterno> findByTimeIdAndNomeNormalizado(Long timeId, String nomeNormalizado);
    List<AdversarioExterno> findByTimeIdOrderByNomeAsc(Long timeId);
}
