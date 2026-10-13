package br.com.arenamatch.service;

import br.com.arenamatch.dto.CampeonatoDTO;
import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.*;
import br.com.arenamatch.repository.CampeonatoRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.h2.tools.RunScript;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class CampeonatoPersistenceTest {
    private static SessionFactory factory;
    private static TransactionTemplate tx;
    private static UsuarioRepository usuarios;
    private static CampeonatoService service;
    private static final String URL = "jdbc:h2:mem:campeonato;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";

    @BeforeAll static void iniciar() throws Exception {
        factory = new Configuration().addAnnotatedClass(Usuario.class).addAnnotatedClass(Campeonato.class)
                .setProperty("hibernate.connection.url", URL).setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.username", "sa").setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop").buildSessionFactory();
        // Aplicar a migration real sobre usuario isolado; as tabelas de campeonato geradas pelo Hibernate sao substituidas.
        try (var connection = DriverManager.getConnection(URL, "sa", ""); var statement = connection.createStatement()) {
            statement.execute("DROP TABLE campeonato_desempate"); statement.execute("DROP TABLE campeonato"); statement.execute("DROP SEQUENCE seq_campeonato");
            try (var reader = new InputStreamReader(CampeonatoPersistenceTest.class.getResourceAsStream(
                    "/db/migration/V46__criar_configuracao_campeonato.sql"), StandardCharsets.UTF_8)) { RunScript.execute(connection, reader); }
        }
        EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(factory);
        JpaRepositoryFactory repositories = new JpaRepositoryFactory(em);
        usuarios = repositories.getRepository(UsuarioRepository.class);
        service = new CampeonatoService(repositories.getRepository(CampeonatoRepository.class), usuarios,
                new CampeonatoAuthorizationService(usuarios), new CampeonatoValidator());
        tx = new TransactionTemplate(new JpaTransactionManager(factory));
    }
    @AfterAll static void fechar() { if (factory != null) factory.close(); }
    @AfterEach void limparAutenticacao() { SecurityContextHolder.clearContext(); }

    @Test void migrationPersistenciaCorrecaoEHistorico() {
        criarUsuario("persistencia"); autenticar("persistencia");
        CampeonatoRequestDTO r = request(); r.setDataInicio(LocalDate.of(2026,12,1)); r.setDataFim(LocalDate.of(2027,3,1));
        r.setTaxaInscricao(new BigDecimal("1000.00")); r.setDesempates(List.of(CriterioDesempate.GOLS_PRO, CriterioDesempate.VITORIAS));
        CampeonatoDTO criado = tx.execute(s -> service.criar(r));
        CampeonatoDTO lido = tx.execute(s -> service.consultar(criado.getId()));
        assertEquals(2026, lido.getAnoReferencia()); assertEquals(r.getDesempates(), lido.getDesempates());
        assertEquals(new BigDecimal("1000.00"), lido.getTaxaInscricao());
        lido.setNome("Copa corrigida"); CampeonatoDTO editado = tx.execute(s -> service.atualizar(lido.getId(), lido));
        assertTrue(editado.getVersao() > criado.getVersao());
        assertThrows(ResponseStatusException.class, () -> tx.execute(s -> service.atualizar(criado.getId(), criado)));
        tx.execute(s -> service.cancelarRascunho(editado.getId(), editado.getVersao()));
        assertEquals(StatusCampeonato.CANCELADO, tx.execute(s -> service.consultar(editado.getId())).getStatus());
        assertEquals(1, tx.execute(s -> service.listar()).size());
    }

    @Test void quartaCriacaoConcorrenteNaoUltrapassaTres() throws Exception {
        criarUsuario("concorrencia"); autenticar("concorrencia");
        tx.execute(s -> service.criar(request())); tx.execute(s -> service.criar(request()));
        CountDownLatch largada = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> criar = () -> {
                autenticar("concorrencia");
                try { largada.await(); tx.execute(s -> service.criar(request())); return true; }
                catch (ResponseStatusException e) { assertEquals(409, e.getStatusCode().value()); return false; }
                finally { SecurityContextHolder.clearContext(); }
            };
            Future<Boolean> a = executor.submit(criar); Future<Boolean> b = executor.submit(criar); largada.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
        autenticar("concorrencia"); assertEquals(3, tx.execute(s -> service.listar()).size());
        CampeonatoDTO cancelar = tx.execute(s -> service.listar()).getFirst();
        tx.execute(s -> service.cancelarRascunho(cancelar.getId(), cancelar.getVersao()));
        assertNotNull(tx.execute(s -> service.criar(request())).getId());
    }

    private void criarUsuario(String email) {
        tx.executeWithoutResult(s -> {
            Usuario u = new Usuario(); u.setNome("Organizador"); u.setEmail(email); u.setCpf(email);
            u.setPerfil(Perfil.REPRESENTANTE); u.setPlanoAssinatura(PlanoAssinatura.PREMIUM); u.setStatusPagamento(StatusPagamento.PAGO);
            usuarios.saveAndFlush(u);
        });
    }
    private static void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email,null,List.of()));
    }
    private static CampeonatoRequestDTO request() { CampeonatoRequestDTO r = new CampeonatoRequestDTO(); r.setNome("Copa"); return r; }
}
