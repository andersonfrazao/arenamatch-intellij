package br.com.arenamatch.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "ARENA67_TEST_DB_URL", matches = ".+")
class ModalidadeMigracaoTest {
    @Test void migraHistoricoExistenteParaCampoSemApagarDados() throws Exception {
        String url = System.getenv("ARENA67_TEST_DB_URL");
        String usuario = System.getenv().getOrDefault("ARENA67_TEST_DB_USER", "postgres");
        String senha = System.getenv("ARENA67_TEST_DB_PASSWORD");
        String schema = "arena67_" + UUID.randomUUID().toString().replace("-", "");
        Flyway.configure().dataSource(url, usuario, senha).schemas(schema).target("39").load().migrate();
        try (Connection c = DriverManager.getConnection(url, usuario, senha); Statement s = c.createStatement()) {
            c.setSchema(schema);
            s.executeUpdate("INSERT INTO time(id,nome,id_responsavel,pontos) VALUES(101,'Time legado A',1,3),(102,'Time legado B',1,0)");
            s.executeUpdate("INSERT INTO agenda(id,id_time,dia_semana,hora_inicio,hora_fim,categoria) VALUES(101,101,'Sábado','09:00','11:00','ESPORTE')");
            s.executeUpdate("INSERT INTO partida(id,id_mandante,id_visitante,data_hora,status,status_placar,gols_mandante,gols_visitante) VALUES(101,101,102,'2026-01-01 09:00','AGENDADO','CONFIRMADO',3,1)");
        }
        Flyway flyway = Flyway.configure().dataSource(url, usuario, senha).schemas(schema).load();
        flyway.migrate();
        assertTrue(java.util.Arrays.stream(flyway.info().applied()).anyMatch(m ->
                "V67.0__ARENA_67_adicionar_modalidades_agenda_partida.sql".equals(m.getScript())
                        && org.flywaydb.core.api.MigrationVersion.fromVersion("67.0").equals(m.getVersion())));
        assertFalse(java.util.Arrays.stream(flyway.info().all()).anyMatch(m ->
                "V40__adicionar_modalidades_agenda_partida.sql".equals(m.getScript())));
        try (Connection c = DriverManager.getConnection(url, usuario, senha); Statement s = c.createStatement()) {
            c.setSchema(schema);
            try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM time_modalidade WHERE modalidade='FUTEBOL_CAMPO'")) { r.next(); assertEquals(2, r.getInt(1)); }
            try (ResultSet r = s.executeQuery("SELECT modalidade,hora_inicio FROM agenda WHERE id=101")) { assertTrue(r.next()); assertEquals("FUTEBOL_CAMPO",r.getString(1)); assertEquals("09:00",r.getString(2)); }
            try (ResultSet r = s.executeQuery("SELECT modalidade,gols_mandante,gols_visitante,status_placar FROM partida WHERE id=101")) {
                assertTrue(r.next()); assertEquals("FUTEBOL_CAMPO",r.getString(1)); assertEquals(3,r.getInt(2)); assertEquals(1,r.getInt(3)); assertEquals("CONFIRMADO",r.getString(4));
            }
            try (ResultSet r = s.executeQuery("SELECT pontos FROM time WHERE id=101")) { r.next(); assertEquals(3,r.getInt(1)); }
        }
    }
}
