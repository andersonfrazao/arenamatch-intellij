package br.com.arenamatch.repository;

import br.com.arenamatch.entity.*;
import br.com.arenamatch.enums.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=${ARENA67_TEST_DB_URL}",
        "spring.datasource.username=${ARENA67_TEST_DB_USER:postgres}",
        "spring.datasource.password=${ARENA67_TEST_DB_PASSWORD}",
        "spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=true",
        "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "ARENA67_TEST_DB_URL", matches = ".+")
class ModalidadeRepositoryTest {
    @Autowired private TimeRepository times;
    @Autowired private PartidaRepository partidas;
    @Autowired private EntityManager em;

    @Test void buscaRetornaSomenteModalidadeCategoriaEDiaCompativeis() {
        Time meu = time("Meu time", false, Modalidade.FUTSAL, Modalidade.FUT_7);
        Time campo = time("Campo", true, Modalidade.FUTEBOL_CAMPO);
        Time futsal = time("Futsal", true, Modalidade.FUTSAL);
        Time fut7 = time("Fut7", true, Modalidade.FUT_7);
        agenda(meu, Modalidade.FUTSAL, "Sábado");
        agenda(meu, Modalidade.FUT_7, "Domingo");
        agenda(campo, Modalidade.FUTEBOL_CAMPO, "Sábado");
        agenda(futsal, Modalidade.FUTSAL, "Sábado");
        agenda(fut7, Modalidade.FUT_7, "Sábado");
        em.flush();
        List<Tuple> resultado = times.buscarPorLocalizacaoEDia(meu.getLatitude(), meu.getLongitude(), 100d, null, meu.getId(), LocalDate.now().plusDays(14), "Sábado", false, null, "FUTSAL");
        assertEquals(List.of(futsal.getId()), resultado.stream().map(t -> ((Number)t.get("id")).longValue()).toList());
        assertTrue(times.buscarPorLocalizacaoEDia(meu.getLatitude(), meu.getLongitude(), 100d, null, meu.getId(), LocalDate.now().plusDays(14), "Sábado", false, null, "FUT_7").isEmpty());
    }

    @Test void rankingSeparaResultadosDoMesmoTimePorModalidade() {
        Time a = time("A", true, Modalidade.FUTSAL, Modalidade.FUT_7);
        Time b = time("B", false, Modalidade.FUTSAL, Modalidade.FUT_7);
        partida(a, b, Modalidade.FUTSAL, 7, 2, StatusPlacar.CONFIRMADO);
        partida(a, b, Modalidade.FUT_7, 1, 4, StatusPlacar.CONFIRMADO);
        partida(a, b, Modalidade.FUTSAL, 99, 0, StatusPlacar.AGUARDANDO_CONFIRMACAO);
        em.flush();
        List<Tuple> futsal = partidas.buscarRankingPorModalidade("FUTSAL");
        List<Tuple> fut7 = partidas.buscarRankingPorModalidade("FUT_7");
        assertEquals(a.getId().longValue(), ((Number)futsal.get(0).get("id")).longValue());
        assertEquals(b.getId().longValue(), ((Number)fut7.get(0).get("id")).longValue());
        assertEquals(7, ((Number)futsal.get(0).get("golsPro")).intValue());
        assertEquals(1, ((Number)futsal.get(0).get("jogos")).intValue());
        assertEquals(3, ((Number)futsal.get(0).get("pontos")).intValue());
        assertEquals(4, ((Number)fut7.get(0).get("golsPro")).intValue());
        assertTrue(partidas.buscarRankingPorModalidade("FUTEBOL_CAMPO").stream().noneMatch(t -> ((Number)t.get("id")).longValue() == a.getId()));
        assertEquals(1L, ((Number)partidas.buscarResumoConfrontosPorModalidade(a.getId(), List.of(b.getId()), "FUTSAL").get(0).get("jogos")).longValue());
    }

    @Test void rankingExigePeloMenosUmJogoConfirmadoNaModalidadeConsultada() {
        Time a = time("Participante A", true, Modalidade.FUTSAL);
        Time b = time("Participante B", false, Modalidade.FUTSAL);
        time("Sem jogos", true, Modalidade.FUTSAL);
        Time pendenteA = time("Apenas pendente A", true, Modalidade.FUTSAL);
        Time pendenteB = time("Apenas pendente B", false, Modalidade.FUTSAL);
        partida(a, b, Modalidade.FUTSAL, 0, 0, StatusPlacar.CONFIRMADO);
        partida(pendenteA, pendenteB, Modalidade.FUTSAL, 2, 1, StatusPlacar.AGUARDANDO_CONFIRMACAO);
        em.flush();

        List<Tuple> ranking = partidas.buscarRankingPorModalidade("FUTSAL");
        assertEquals(Set.of(a.getId(), b.getId()), ranking.stream()
                .map(t -> ((Number)t.get("id")).longValue()).collect(java.util.stream.Collectors.toSet()));
        assertTrue(ranking.stream().allMatch(t -> ((Number)t.get("jogos")).intValue() >= 1));
        assertTrue(ranking.stream().allMatch(t -> ((Number)t.get("empates")).intValue() == 1));
        assertTrue(partidas.buscarRankingPorModalidade("FUT_7").isEmpty());
    }

    @Test void podeAcrescentarModalidadeSemPerderVinculosReferenciadosPelaAgenda() {
        Time time = time("Equipe", true, Modalidade.FUTEBOL_CAMPO);
        agenda(time, Modalidade.FUTEBOL_CAMPO, "Sábado"); em.flush(); em.clear();
        Time salvo = times.findById(time.getId()).orElseThrow();
        salvo.getModalidades().add(Modalidade.FUTSAL); em.flush(); em.clear();
        assertEquals(Set.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL), times.findById(time.getId()).orElseThrow().getModalidades());
    }

    @Test void bancoRejeitaAlteracaoDaModalidadeDeUmaPartida() {
        Time a = time("A", true, Modalidade.FUTSAL, Modalidade.FUT_7);
        Time b = time("B", false, Modalidade.FUTSAL, Modalidade.FUT_7);
        Partida p = partida(a, b, Modalidade.FUTSAL, 2, 1, StatusPlacar.CONFIRMADO); em.flush();
        assertThrows(RuntimeException.class, () -> em.createNativeQuery("UPDATE partida SET modalidade='FUT_7' WHERE id=:id").setParameter("id", p.getId()).executeUpdate());
    }

    @Test void carregaUsuarioComPremiumGravadoPorOutraFuncionalidade() {
        Time equipe = time("Equipe Premium", true, Modalidade.FUTEBOL_CAMPO);
        Long usuarioId = equipe.getResponsavel().getId();
        em.createNativeQuery("UPDATE usuario SET plano_assinatura='PREMIUM', status_pagamento='PAGO' WHERE id=:id")
                .setParameter("id", usuarioId).executeUpdate();
        em.clear();
        Usuario usuario = em.find(Usuario.class, usuarioId);
        assertEquals(PlanoAssinatura.PREMIUM, usuario.getPlanoAssinatura());
        assertEquals(StatusPagamento.PAGO, usuario.getStatusPagamento());
    }

    private Time time(String nome, boolean casa, Modalidade... modalidades) {
        Usuario u = new Usuario(); u.setNome(nome); u.setEmail(UUID.randomUUID()+"@local.test"); u.setSenha("teste");
        u.setCpf(UUID.randomUUID().toString().replace("-", "").substring(0,11)); u.setPerfil(Perfil.REPRESENTANTE); em.persist(u);
        Time t = new Time(); t.setNome(nome); t.setResponsavel(u); t.setMandoCampo(casa);
        t.getModalidades().addAll(List.of(modalidades)); t.setLatitude(-23.5 + (casa ? .001 : 0)); t.setLongitude(-46.6);
        t.setValorTaxa(100d); em.persist(t); em.flush(); return t;
    }
    private void agenda(Time t, Modalidade m, String dia) {
        Agenda a = new Agenda(); a.setTime(t); a.setModalidade(m); a.setCategoria(Categoria.ESPORTE); a.setDiaSemana(dia); a.setHoraInicio("09:00"); a.setHoraFim("11:00"); em.persist(a);
    }
    private Partida partida(Time a, Time b, Modalidade m, int pro, int contra, StatusPlacar status) {
        Partida p = new Partida(); p.setMandante(a); p.setVisitante(b); p.setModalidade(m); p.setStatus(StatusPartida.AGENDADO);
        p.setDataHora(LocalDateTime.now().minusDays(1)); p.setGolsMandante(pro); p.setGolsVisitante(contra); p.setStatusPlacar(status); em.persist(p); return p;
    }
}
