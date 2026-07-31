package br.com.arenamatch.repository;

import br.com.arenamatch.entity.BanimentoLiga;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanimentoLigaRepository extends JpaRepository<BanimentoLiga, Long> {

    boolean existsByLigaIdAndTimeBanidoIdAndAtivoTrue(Long ligaId, Long timeBanidoId);

    Optional<BanimentoLiga> findByLigaIdAndTimeBanidoIdAndAtivoTrue(Long ligaId, Long timeBanidoId);

    List<BanimentoLiga> findByLigaIdAndAtivoTrueOrderByDataBanimentoDesc(Long ligaId);
}
