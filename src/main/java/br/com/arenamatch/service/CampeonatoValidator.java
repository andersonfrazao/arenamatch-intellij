package br.com.arenamatch.service;

import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.entity.Campeonato;
import br.com.arenamatch.enums.FormatoCampeonato;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CampeonatoValidator {
    public void validar(CampeonatoRequestDTO r) {
        if (r == null) erro("Informe os dados do campeonato.");
        texto(r.getNome(), 150, true, "Nome");
        texto(r.getCategoria(), 80, true, "Categoria");
        texto(r.getDescricao(), 4000, false, "Descrição");
        texto(r.getRegulamento(), 10000, false, "Regulamento");
        texto(r.getPremiacoes(), 4000, false, "Premiações");
        texto(r.getDiasSugeridos(), 200, false, "Dias sugeridos");
        if (r.getFormato() == null) erro("Escolha o formato do campeonato.");
        if (r.getIdadeMinima() != null && (r.getIdadeMinima() < 0 || r.getIdadeMinima() > 120)) erro("Idade mínima deve ficar entre 0 e 120 anos.");
        if ("Livre".equalsIgnoreCase(r.getCategoria().trim()) && r.getIdadeMinima() != null) erro("Categoria Livre não possui idade mínima.");
        if (r.getDataInicio() != null && r.getDataFim() != null && r.getDataFim().isBefore(r.getDataInicio())) erro("O fim previsto deve ser igual ou posterior ao início.");
        BigDecimal taxa = r.getTaxaInscricao();
        if (taxa == null || taxa.signum() < 0 || taxa.compareTo(new BigDecimal("9999999999.99")) > 0 || taxa.stripTrailingZeros().scale() > 2) erro("Informe uma taxa válida, com até duas casas decimais, ou zero para inscrição gratuita.");
        minimo(r.getQuantidadeTimes(), 2, "Quantidade de times");
        minimo(r.getQuantidadeGrupos(), 2, "Quantidade de grupos");
        minimo(r.getTimesPorGrupo(), 2, "Times por grupo");
        minimo(r.getClassificadosPorGrupo(), 1, "Classificados por grupo");
        if (r.getFormato() != FormatoCampeonato.GRUPOS_MATA_MATA &&
                (r.getQuantidadeGrupos() != null || r.getTimesPorGrupo() != null || r.getClassificadosPorGrupo() != null)) erro("Configurações de grupos só se aplicam ao formato Grupos + mata-mata.");
        if (r.getFormato() == FormatoCampeonato.LIGA && r.isIdaVoltaEliminatoria()) erro("Uma liga não possui fase eliminatória.");
        if (r.getFormato() == FormatoCampeonato.MATA_MATA && r.isIdaVoltaClassificatoria()) erro("Mata-mata direto não possui fase classificatória.");
        if (r.getTimesPorGrupo() != null && r.getClassificadosPorGrupo() != null && r.getClassificadosPorGrupo() > r.getTimesPorGrupo()) erro("Classificados não podem superar os times por grupo.");
        if (r.getQuantidadeGrupos() != null && r.getTimesPorGrupo() != null) {
            long total = (long) r.getQuantidadeGrupos() * r.getTimesPorGrupo();
            if (total > Integer.MAX_VALUE || (r.getQuantidadeTimes() != null && total != r.getQuantidadeTimes())) erro("A quantidade de times deve corresponder ao número de grupos multiplicado pelos times por grupo.");
        }
        pontos(r.getPontosVitoria()); pontos(r.getPontosEmpate()); pontos(r.getPontosDerrota());
        if (r.getDesempates() == null || r.getDesempates().stream().anyMatch(Objects::isNull) || r.getDesempates().isEmpty()
                || new HashSet<>(r.getDesempates()).size() != r.getDesempates().size()) erro("Escolha critérios de desempate distintos e em ordem.");
    }

    public void validarAlteracaoPublicada(Campeonato c, CampeonatoRequestDTO r) {
        if (!c.isTabelaPublicada()) return;
        boolean mudou = c.getFormato() != r.getFormato()
                || c.isIdaVoltaClassificatoria() != r.isIdaVoltaClassificatoria()
                || c.isIdaVoltaEliminatoria() != r.isIdaVoltaEliminatoria()
                || !Objects.equals(c.getQuantidadeTimes(), r.getQuantidadeTimes())
                || !Objects.equals(c.getQuantidadeGrupos(), r.getQuantidadeGrupos())
                || !Objects.equals(c.getTimesPorGrupo(), r.getTimesPorGrupo())
                || !Objects.equals(c.getClassificadosPorGrupo(), r.getClassificadosPorGrupo())
                || !Objects.equals(c.getPontosVitoria(), r.getPontosVitoria())
                || !Objects.equals(c.getPontosEmpate(), r.getPontosEmpate())
                || !Objects.equals(c.getPontosDerrota(), r.getPontosDerrota())
                || !Objects.equals(c.getDesempates(), r.getDesempates())
                || !Objects.equals(c.getCategoria(), r.getCategoria().trim())
                || !Objects.equals(c.getIdadeMinima(), r.getIdadeMinima())
                || !Objects.equals(c.getDataInicio(), r.getDataInicio());
        if (mudou) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Formato, categoria, ano de referência e regras esportivas não podem mudar após a publicação da tabela.");
    }

    private void texto(String v, int limite, boolean obrigatorio, String nome) {
        if (obrigatorio && (v == null || v.isBlank())) erro(nome + " é obrigatório.");
        if (v != null && v.length() > limite) erro(nome + " excede " + limite + " caracteres.");
    }
    private void minimo(Integer v, int limite, String nome) { if (v != null && v < limite) erro(nome + " deve ser pelo menos " + limite + "."); }
    private void pontos(Integer v) { if (v == null || v < 0) erro("Pontuação deve ser um inteiro não negativo."); }
    private void erro(String msg) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }
}
