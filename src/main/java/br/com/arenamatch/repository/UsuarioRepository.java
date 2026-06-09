package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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
}
