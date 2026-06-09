package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);
    Optional<Usuario> findByTokenDesafioAdmin(String tokenDesafioAdmin);

    @Query("""
            select u
              from Usuario u
             where lower(u.nome) like lower(concat('%', :termo, '%'))
                or lower(u.email) like lower(concat('%', :termo, '%'))
             order by u.nome
            """)
    Page<Usuario> buscarPorNomeOuEmail(String termo, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
            update Usuario u
               set u.planoAssinatura = br.com.arenamatch.enums.PlanoAssinatura.BASICO,
                   u.statusPagamento = br.com.arenamatch.enums.StatusPagamento.EXPIRADO,
                   u.statusAssinatura = br.com.arenamatch.enums.StatusAssinatura.VENCIDO
             where u.planoAssinatura = br.com.arenamatch.enums.PlanoAssinatura.TRIAL
               and u.dataExpiracao is not null
               and u.dataExpiracao < :agora
            """)
    int converterTrialsExpiradosParaBasico(LocalDateTime agora);
}
