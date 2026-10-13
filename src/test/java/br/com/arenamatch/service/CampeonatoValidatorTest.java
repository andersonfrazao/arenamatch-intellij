package br.com.arenamatch.service;

import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.enums.CriterioDesempate;
import br.com.arenamatch.enums.FormatoCampeonato;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class CampeonatoValidatorTest {
    private final CampeonatoValidator validator = new CampeonatoValidator();
    @Test void rascunhoIncompletoEPermitidoMasValoresInvalidosNao() {
        CampeonatoRequestDTO r = request(); assertDoesNotThrow(() -> validator.validar(r));
        r.setTaxaInscricao(new BigDecimal("1000.001")); assertThrows(ResponseStatusException.class, () -> validator.validar(r));
        r.setTaxaInscricao(BigDecimal.ZERO); r.setDataInicio(LocalDate.of(2026,12,1)); r.setDataFim(LocalDate.of(2027,2,1));
        assertDoesNotThrow(() -> validator.validar(r));
        r.setDataFim(LocalDate.of(2026,11,1)); assertThrows(ResponseStatusException.class, () -> validator.validar(r));
    }
    @Test void gruposPrecisamTerTotaisConsistentesESemOverflow() {
        CampeonatoRequestDTO r = request(); r.setFormato(FormatoCampeonato.GRUPOS_MATA_MATA);
        r.setQuantidadeGrupos(2); r.setTimesPorGrupo(4); r.setQuantidadeTimes(8); r.setClassificadosPorGrupo(2);
        assertDoesNotThrow(() -> validator.validar(r));
        r.setQuantidadeTimes(9); assertThrows(ResponseStatusException.class, () -> validator.validar(r));
        r.setQuantidadeTimes(8); r.setClassificadosPorGrupo(5); assertThrows(ResponseStatusException.class, () -> validator.validar(r));
        r.setClassificadosPorGrupo(2); r.setQuantidadeGrupos(Integer.MAX_VALUE); assertThrows(ResponseStatusException.class, () -> validator.validar(r));
    }
    @Test void formatosNaoAceitamFasesIncompativeis() {
        CampeonatoRequestDTO r = request(); r.setIdaVoltaEliminatoria(true);
        assertThrows(ResponseStatusException.class, () -> validator.validar(r));
        r.setFormato(FormatoCampeonato.MATA_MATA); r.setIdaVoltaClassificatoria(true);
        assertThrows(ResponseStatusException.class, () -> validator.validar(r));
        r.setIdaVoltaClassificatoria(false); assertDoesNotThrow(() -> validator.validar(r));
    }
    @Test void ordemDeDesempatesEConfiguravelSemDuplicatas() {
        CampeonatoRequestDTO r = request(); r.setDesempates(List.of(CriterioDesempate.GOLS_PRO, CriterioDesempate.SALDO_GOLS));
        assertDoesNotThrow(() -> validator.validar(r));
        r.setDesempates(List.of(CriterioDesempate.GOLS_PRO, CriterioDesempate.GOLS_PRO));
        assertThrows(ResponseStatusException.class, () -> validator.validar(r));
    }
    @Test void publicarTabelaCongelaFormatoPontuacaoEAno() {
        CampeonatoRequestDTO r = request(); r.setDataInicio(LocalDate.of(2026,12,1));
        Campeonato c = new Campeonato(); BeanUtils.copyProperties(r,c); c.setTabelaPublicada(true);
        r.setNome("Outro nome"); assertDoesNotThrow(() -> validator.validarAlteracaoPublicada(c,r));
        r.setDataInicio(LocalDate.of(2027,1,1)); assertThrows(ResponseStatusException.class, () -> validator.validarAlteracaoPublicada(c,r));
        r.setDataInicio(c.getDataInicio()); r.setFormato(FormatoCampeonato.MATA_MATA);
        assertThrows(ResponseStatusException.class, () -> validator.validarAlteracaoPublicada(c,r));
    }
    private CampeonatoRequestDTO request() { CampeonatoRequestDTO r = new CampeonatoRequestDTO(); r.setNome("Copa"); return r; }
}
