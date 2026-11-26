package br.com.arenamatch.repository;

import br.com.arenamatch.dto.RegistroPartidaExternaDTO;
import br.com.arenamatch.entity.*;
import br.com.arenamatch.enums.*;
import br.com.arenamatch.service.PartidaExternaService;
import br.com.arenamatch.service.AgendaService;
import br.com.arenamatch.service.HorarioJogoService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=${ARENA70_TEST_DB_URL}",
        "spring.datasource.username=${ARENA70_TEST_DB_USER:postgres}",
        "spring.datasource.password=${ARENA70_TEST_DB_PASSWORD}",
        "spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=true", "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({PartidaExternaService.class, AgendaService.class, HorarioJogoService.class})
@EnabledIfEnvironmentVariable(named = "ARENA70_TEST_DB_URL", matches = ".+")
class PartidaExternaRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private PartidaExternaService service;
    @Autowired private AgendaService agendaService;
    @Autowired private PartidaExternaRepository partidas;
    @Autowired private AdversarioExternoRepository adversarios;
    @Autowired private PartidaRepository oficiais;
    @Autowired private TimeRepository times;
    @Autowired private PlatformTransactionManager transactionManager;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void persisteRegistroPrivadoSemPontuarERespeitaLimitesDoHorario() {
        Time time = criarTimeComAgenda();
        autenticar(time.getResponsavel().getEmail());
        var dto = dados();
        var evento = service.registrar(dto);
        em.flush();
        em.clear();
        PartidaExterna partida = partidas.findById(evento.getIdPartidaExterna()).orElseThrow();
        assertEquals(OrigemPartida.MANUAL, partida.getOrigem());
        assertNotNull(partida.getCriadoEm());
        assertNotNull(partida.getAtualizadoEm());
        assertEquals(new BigDecimal("150.00"), partida.getValorTaxa());
        assertEquals(time.getResponsavel().getId(), partida.getAutor().getId());
        assertTrue(oficiais.buscarRankingPorModalidade("FUTEBOL_CAMPO").isEmpty());
        assertTrue(partidas.existeConflito(time.getId(), Modalidade.FUTEBOL_CAMPO,
                dto.getData().atTime(13, 0), dto.getData().atTime(15, 0)));
        assertFalse(partidas.existeConflito(time.getId(), Modalidade.FUTEBOL_CAMPO,
                dto.getData().atTime(14, 0), dto.getData().atTime(16, 0)));
        assertFalse(partidas.existeConflito(time.getId(), Modalidade.FUTSAL,
                dto.getData().atTime(12, 0), dto.getData().atTime(14, 0)));
        assertEquals(evento.getIdPartidaExterna(), service.registrar(dto).getIdPartidaExterna());
    }

    @Test
    void agendaMostraJogosExternosFuturosERetroativosDoProprietario() {
        Time time = criarTimeComAgenda();
        autenticar(time.getResponsavel().getEmail());
        var futuro = dados();
        service.registrar(futuro);
        var passado = dados();
        passado.setData(LocalDate.now().minusDays(7));
        service.registrar(passado);
        var calendarioFuturo = agendaService.montarCalendario(time.getId(), futuro.getData(), 1).getFirst();
        assertTrue(calendarioFuturo.isTemJogoConfirmado());
        assertFalse(calendarioFuturo.isTemPlacarPendente());
        var calendarioPassado = agendaService.montarCalendario(time.getId(), passado.getData(), 1).getFirst();
        assertTrue(calendarioPassado.isTemPlacarPendente());
        assertFalse(calendarioPassado.isTemJogoRealizado());
        var realizado = dados();
        realizado.setData(LocalDate.now().minusDays(9));
        realizado.setGolsMandante(3);
        realizado.setGolsVisitante(2);
        service.registrar(realizado);
        var calendarioRealizado = agendaService.montarCalendario(time.getId(), realizado.getData(), 1).getFirst();
        assertTrue(calendarioRealizado.isTemJogoRealizado());
        assertFalse(calendarioRealizado.isTemPlacarPendente());
        assertFalse(calendarioRealizado.isTemJogoConfirmado());
        var detalheRealizado = agendaService.buscarDetalhesDoDia(time.getId(), realizado.getData()).getFirst();
        assertEquals(SituacaoPartidaExterna.REALIZADO, detalheRealizado.getSituacaoRegistroManual());
        assertEquals(3, detalheRealizado.getGolsMandante());
        assertEquals(2, detalheRealizado.getGolsVisitante());
        var detalhes = agendaService.buscarDetalhesDoDia(time.getId(), futuro.getData());
        assertEquals(1, detalhes.size());
        assertTrue(detalhes.getFirst().isRegistroManual());
        assertEquals("Visitante", detalhes.getFirst().getPosicaoMeuTime());
        assertEquals(1, agendaService.buscarJogosAgendadosFuturos(time.getId()).size());
    }

    @Test
    void buscaExcluiAdversarioOcupadoSomenteNaModalidadeRegistrada() {
        Time meu = criarTime();
        Time adversario = criarTime();
        meu.setLatitude(-23.5);
        meu.setLongitude(-46.6);
        adversario.setLatitude(-23.5);
        adversario.setLongitude(-46.6);
        adversario.setMandoCampo(true);
        LocalDate data = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.SATURDAY));
        for (Time time : List.of(meu, adversario)) {
            for (Modalidade modalidade : List.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL)) {
                Agenda agenda = new Agenda();
                agenda.setTime(time);
                agenda.setModalidade(modalidade);
                agenda.setCategoria(Categoria.ESPORTE);
                agenda.setDiaSemana("Sábado");
                agenda.setHoraInicio("12:00");
                agenda.setHoraFim("14:00");
                em.persist(agenda);
            }
        }
        autenticar(adversario.getResponsavel().getEmail());
        var dto = dados();
        dto.setData(data);
        service.registrar(dto);
        em.flush();
        assertTrue(times.buscarPorLocalizacaoEDia(-23.5, -46.6, 10d, null, meu.getId(), data,
                "Sábado", false, "ESPORTE", "FUTEBOL_CAMPO").isEmpty());
        var futsal = times.buscarPorLocalizacaoEDia(-23.5, -46.6, 10d, null, meu.getId(), data,
                "Sábado", false, "ESPORTE", "FUTSAL");
        assertEquals(1, futsal.size());
        assertEquals(adversario.getId().longValue(), ((Number) futsal.getFirst().get("id")).longValue());
    }

    @Test
    void bancoRejeitaPlacarIncompleto() {
        Time time = criarTimeComAgenda();
        autenticar(time.getResponsavel().getEmail());
        var passado = dados();
        passado.setData(LocalDate.now().minusDays(3));
        var evento = service.registrar(passado);
        em.flush();
        assertThrows(RuntimeException.class, () -> em.createNativeQuery(
                "update partida_externa set gols_mandante = 1 where id = :id")
                .setParameter("id", evento.getIdPartidaExterna()).executeUpdate());
    }

    @Test
    void rejeitaAdversarioPertencenteAOutroTimeNoBanco() {
        Time dono = criarTimeComAgenda();
        Time outro = criarTime();
        autenticar(dono.getResponsavel().getEmail());
        var evento = service.registrar(dados());
        em.flush();
        assertThrows(RuntimeException.class, () -> em.createNativeQuery(
                "update partida_externa set id_time = :outro where id = :id")
                .setParameter("outro", outro.getId()).setParameter("id", evento.getIdPartidaExterna()).executeUpdate());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void requisicoesConcorrentesComMesmaChaveCriamUmaUnicaPartidaEAdversario() throws Exception {
        Time time = new TransactionTemplate(transactionManager).execute(status -> criarTimeComAgenda());
        String email = time.getResponsavel().getEmail();
        UUID chave = UUID.randomUUID();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tarefa = (java.util.concurrent.Callable<Long>) () -> {
                autenticar(email);
                try {
                    var dto = dados();
                    dto.setChaveSolicitacao(chave);
                    return service.registrar(dto).getIdPartidaExterna();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
            var primeiro = executor.submit(tarefa);
            var segundo = executor.submit(tarefa);
            assertEquals(primeiro.get(20, TimeUnit.SECONDS), segundo.get(20, TimeUnit.SECONDS));
        }
        assertEquals(1, adversarios.findByTimeIdOrderByNomeAsc(time.getId()).size());
        assertEquals(1, partidas.findByTimeIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(
                time.getId(), LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(30).atStartOfDay()).size());
    }

    @Test
    void mandanteGravaHorarioLocalETaxaDoCadastroEVisitantePodeDeixarEmBranco() {
        Time mandante = criarTime();
        mandante.setMandoCampo(true);
        mandante.setNomeCampo("Arena de teste");
        mandante.setLogradouro("Rua A");
        mandante.setCidade("Campinas");
        mandante.setUf("SP");
        mandante.setValorTaxa(80.0);
        LocalDate sexta = LocalDate.now().plusDays(1).with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.FRIDAY));
        agendar(mandante, "Sexta", "20:00", "22:00");
        autenticar(mandante.getResponsavel().getEmail());
        var dto = dados();
        dto.setData(sexta);
        dto.setHoraInicio(null);
        dto.setHoraFim(null);
        dto.setNomeCampo(null);
        dto.setValorTaxa(null);
        var evento = service.registrar(dto);
        em.flush();
        em.clear();
        PartidaExterna partida = partidas.findById(evento.getIdPartidaExterna()).orElseThrow();
        assertEquals(sexta.atTime(20, 0), partida.getDataHoraInicio());
        assertEquals(sexta.atTime(22, 0), partida.getDataHoraFim());
        assertEquals("Arena de teste", partida.getNomeCampo());
        assertEquals("Rua A, Campinas/SP", partida.getEndereco());
        assertEquals(new BigDecimal("80.00"), partida.getValorTaxa());

        Time visitante = criarTime();
        agendar(visitante, "Sexta", "19:00", "23:00");
        autenticar(visitante.getResponsavel().getEmail());
        var semDados = dados();
        semDados.setData(sexta);
        semDados.setHoraInicio(null);
        semDados.setHoraFim(null);
        semDados.setNomeCampo(null);
        semDados.setValorTaxa(null);
        PartidaExterna semCampo = partidas.findById(service.registrar(semDados).getIdPartidaExterna()).orElseThrow();
        em.flush();
        assertNull(semCampo.getNomeCampo());
        assertNull(semCampo.getValorTaxa());
        assertEquals(sexta.atTime(19, 0), semCampo.getDataHoraInicio());
        var sabado = dados();
        sabado.setData(sexta.plusDays(1));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, assertThrows(
                org.springframework.web.server.ResponseStatusException.class, () -> service.registrar(sabado)).getStatusCode());
    }

    /** Time que joga Futebol de Campo, categoria Esporte, todos os dias das 12:00 às 14:00. */
    private Time criarTimeComAgenda() {
        Time time = criarTime();
        for (String dia : List.of("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")) {
            agendar(time, dia, "12:00", "14:00");
        }
        return time;
    }

    private void agendar(Time time, String dia, String inicio, String fim) {
        Agenda agenda = new Agenda();
        agenda.setTime(time);
        agenda.setModalidade(Modalidade.FUTEBOL_CAMPO);
        agenda.setCategoria(Categoria.ESPORTE);
        agenda.setDiaSemana(dia);
        agenda.setHoraInicio(inicio);
        agenda.setHoraFim(fim);
        em.persist(agenda);
    }

    private Time criarTime() {
        Usuario usuario = new Usuario();
        usuario.setNome("Representante de teste");
        usuario.setEmail(UUID.randomUUID() + "@teste.local");
        usuario.setSenha("teste");
        usuario.setCpf(UUID.randomUUID().toString().replace("-", "").substring(0, 11));
        usuario.setPerfil(Perfil.REPRESENTANTE);
        em.persist(usuario);
        Time time = new Time();
        time.setNome("Time de teste");
        time.setResponsavel(usuario);
        time.getModalidades().addAll(List.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL));
        em.persist(time);
        em.flush();
        return time;
    }

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    private RegistroPartidaExternaDTO dados() {
        var dto = new RegistroPartidaExternaDTO();
        dto.setChaveSolicitacao(UUID.randomUUID());
        dto.setNomeAdversario("União de teste");
        dto.setModalidade(Modalidade.FUTEBOL_CAMPO);
        dto.setCategoria(Categoria.ESPORTE);
        dto.setData(LocalDate.now().plusDays(13));
        dto.setHoraInicio(LocalTime.NOON);
        dto.setHoraFim(LocalTime.of(14, 0));
        dto.setNomeCampo("Campo de teste");
        dto.setValorTaxa(new BigDecimal("150.00"));
        return dto;
    }
}
