package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.AtletaDTO;
import br.com.arenamatch.dto.DisponibilidadeGestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaDTO;
import br.com.arenamatch.dto.GestaoPartidaRequestDTO;
import br.com.arenamatch.enums.EtapaGestaoPartida;
import br.com.arenamatch.enums.PapelParticipacao;
import br.com.arenamatch.enums.SituacaoAtleta;
import br.com.arenamatch.enums.TipoEventoSumula;
import br.com.arenamatch.jsf.client.AtletaClient;
import br.com.arenamatch.jsf.client.GestaoPartidaClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
@Getter @Setter
public class GestaoTimeBean implements Serializable {
    @Inject private AtletaClient atletaClient;
    @Inject private GestaoPartidaClient gestaoClient;
    @Inject private SessaoBean sessaoBean;

    private List<AtletaDTO> atletas = new ArrayList<>();
    private List<LinhaAtleta> escalacao = new ArrayList<>();
    private String novoNome;
    private String novoApelido;
    private String novoNomePartida;
    private String novoApelidoPartida;
    private String filtroAtleta;
    private SituacaoAtleta filtroSituacao;
    private Long atletaEmEdicaoId;
    private Long retornoPartidaId;
    private String origem;
    private String retornoOrigem;
    private String abaJogadoresMobile = "DISPONIVEIS";
    private String formacao = "3-5-2";
    private String formacaoPersonalizada;
    private Integer duracaoMinutos;
    private List<SubstituicaoLinha> substituicoes = new ArrayList<>();
    private Long atletaSaiuId;
    private Long atletaEntrouId;
    private Integer minutoSubstituicao;
    private boolean visaoInicial;
    private SlotTatico slotSelecionado;
    private Long atletaSelecionadoId;
    private Long partidaId;
    private Long versao;
    private String status;
    private LocalDateTime dataPublicacao;
    private String publicadoPor;
    private DisponibilidadeGestaoPartidaDTO disponibilidade;

    @PostConstruct
    public void init() {
        if (!sessaoBean.isAssinantePro()) return;
        carregarAtletas();
        Map<String, String> parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = parametros.get("partida");
        if (id != null && !id.isBlank()) {
            try { partidaId = Long.valueOf(id); carregarPartida(); }
            catch (NumberFormatException e) { erro("Partida informada é inválida."); }
        }
        String retorno = parametros.get("retornoPartida");
        if (retorno != null && !retorno.isBlank()) {
            try { retornoPartidaId = Long.valueOf(retorno); }
            catch (NumberFormatException e) { retornoPartidaId = null; }
        }
        origem = parametros.get("origem");
        retornoOrigem = parametros.get("retornoOrigem");
    }

    public void adicionarAtleta() {
        try {
            boolean editando = atletaEmEdicaoId != null;
            if (atletaEmEdicaoId == null) atletaClient.criar(novoNome, novoApelido);
            else atletaClient.atualizar(atletaEmEdicaoId, novoNome, novoApelido);
            cancelarEdicaoAtleta();
            carregarAtletas();
            info(editando ? "Atleta atualizado." : "Atleta adicionado ao elenco.");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().validationFailed();
            erro(mensagem(e, "Não foi possível cadastrar o atleta."));
        }
    }

    public void novoAtleta() { cancelarEdicaoAtleta(); }

    public void adicionarAtletaPartida() {
        try {
            AtletaDTO criado = atletaClient.criar(novoNomePartida, novoApelidoPartida);
            novoNomePartida = null;
            novoApelidoPartida = null;
            carregarAtletas();
            atletaSelecionadoId = criado == null ? null : criado.getId();
            abaJogadoresMobile = "DISPONIVEIS";
            info("Atleta adicionado e disponível para a escalação.");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().validationFailed();
            erro(mensagem(e, "Não foi possível cadastrar o atleta."));
        }
    }

    public void cancelarSelecaoAtleta() { atletaSelecionadoId = null; }
    public void exibirDisponiveis() { abaJogadoresMobile = "DISPONIVEIS"; }
    public void exibirReservas() { abaJogadoresMobile = "RESERVAS"; }

    public void editarAtleta(AtletaDTO atleta) {
        atletaEmEdicaoId = atleta.getId();
        novoNome = atleta.getNome();
        novoApelido = atleta.getApelido();
    }

    public void cancelarEdicaoAtleta() {
        atletaEmEdicaoId = null;
        novoNome = null;
        novoApelido = null;
    }

    public void alternarSituacao(AtletaDTO atleta) {
        try {
            SituacaoAtleta situacao = atleta.getSituacao() == SituacaoAtleta.ATIVO
                    ? SituacaoAtleta.INATIVO : SituacaoAtleta.ATIVO;
            atletaClient.alterarSituacao(atleta.getId(), situacao);
            carregarAtletas();
        } catch (Exception e) { erro(mensagem(e, "Não foi possível alterar o atleta.")); }
    }

    public List<AtletaDTO> getAtletasFiltrados() {
        String termo = normalizarBusca(filtroAtleta);
        return atletas.stream()
                .filter(atleta -> filtroSituacao == null || atleta.getSituacao() == filtroSituacao)
                .filter(atleta -> termo.isEmpty()
                        || contem(atleta.getNome(), termo)
                        || contem(atleta.getApelido(), termo))
                .toList();
    }

    private boolean contem(String valor, String termo) {
        return valor != null && normalizarBusca(valor).contains(termo);
    }

    private String normalizarBusca(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    public void salvarRascunho() { persistir(false); }
    public void publicar() { persistir(true); }

    private void persistir(boolean publicar) {
        if (partidaId == null) { erro("Abra uma partida pela Agenda para informar as estatísticas."); return; }
        try {
            GestaoPartidaRequestDTO request = montarRequest(publicar);
            GestaoPartidaDTO resposta = publicar ? gestaoClient.publicar(partidaId, request)
                    : gestaoClient.salvar(partidaId, request);
            aplicar(resposta);
            info(publicar ? "Estatísticas publicadas com sucesso." : "Rascunho salvo com sucesso.");
        } catch (Exception e) { erro(mensagem(e, "Não foi possível salvar as estatísticas.")); }
    }

    GestaoPartidaRequestDTO montarRequest(boolean publicar) {
        List<GestaoPartidaRequestDTO.ParticipacaoRequestDTO> participacoes = new ArrayList<>();
        List<GestaoPartidaRequestDTO.EventoRequestDTO> eventos = new ArrayList<>();
        int ordem = 0;
        for (LinhaAtleta linha : escalacao) {
            PapelParticipacao papelPersistido = substituicoes.isEmpty() ? linha.getPapel() : linha.getPapelInicial();
            if (papelPersistido == null) continue;
            participacoes.add(new GestaoPartidaRequestDTO.ParticipacaoRequestDTO(linha.getAtleta().getId(),
                    papelPersistido, linha.getNumeroCamisa(),
                    substituicoes.isEmpty() ? linha.getPosicao() : linha.getPosicaoInicial(),
                    substituicoes.isEmpty() ? linha.getSlotTatico() : linha.getSlotTaticoInicial(),
                    substituicoes.isEmpty() ? linha.getCoordenadaX() : linha.getCoordenadaXInicial(),
                    substituicoes.isEmpty() ? linha.getCoordenadaY() : linha.getCoordenadaYInicial(), ordem++));
            repetir(eventos, linha.getAtleta().getId(), TipoEventoSumula.GOL, linha.getGols());
            repetir(eventos, linha.getAtleta().getId(), TipoEventoSumula.CARTAO_AMARELO, linha.getAmarelos());
            repetir(eventos, linha.getAtleta().getId(), TipoEventoSumula.CARTAO_VERMELHO, linha.getVermelhos());
        }
        EtapaGestaoPartida etapa = publicar ? EtapaGestaoPartida.PUBLICACAO
                : (isPlacarInformado() ? EtapaGestaoPartida.OCORRENCIAS : EtapaGestaoPartida.ESCALACAO);
        List<GestaoPartidaRequestDTO.SubstituicaoRequestDTO> trocas = substituicoes.stream()
                .map(item -> new GestaoPartidaRequestDTO.SubstituicaoRequestDTO(
                        item.atletaSaiuId, item.atletaEntrouId, item.minuto, item.ordem))
                .toList();
        return new GestaoPartidaRequestDTO(versao, etapa, formacao, formacaoPersonalizada,
                duracaoMinutos, participacoes, eventos, trocas);
    }

    private void repetir(List<GestaoPartidaRequestDTO.EventoRequestDTO> eventos, Long atletaId,
                         TipoEventoSumula tipo, Integer quantidade) {
        int total = quantidade == null ? 0 : quantidade;
        for (int i = 0; i < total; i++)
            eventos.add(new GestaoPartidaRequestDTO.EventoRequestDTO(atletaId, tipo, null));
    }

    private void carregarAtletas() {
        try {
            atletas = atletaClient.listar();
            Map<Long, LinhaAtleta> atuais = new HashMap<>();
            escalacao.forEach(l -> atuais.put(l.getAtleta().getId(), l));
            escalacao = new ArrayList<>(atletas.stream().filter(a -> a.getSituacao() == SituacaoAtleta.ATIVO)
                    .map(a -> atuais.getOrDefault(a.getId(), new LinhaAtleta(a))).toList());
        } catch (Exception e) { erro(mensagem(e, "Não foi possível carregar o elenco.")); }
    }

    private void carregarPartida() {
        try {
            disponibilidade = gestaoClient.disponibilidade(partidaId);
            if (disponibilidade.acessoPro()) {
                GestaoPartidaDTO dto = gestaoClient.buscar(partidaId);
                if (dto != null) aplicar(dto);
            }
        } catch (Exception e) { erro(mensagem(e, "Não foi possível carregar a gestão da partida.")); }
    }

    void aplicar(GestaoPartidaDTO dto) {
        versao = dto.versao(); status = dto.status() == null ? null : dto.status().name();
        dataPublicacao = dto.dataPublicacao();
        publicadoPor = dto.publicadoPor();
        formacao = dto.formacao() == null ? "3-5-2" : dto.formacao();
        formacaoPersonalizada = dto.formacaoPersonalizada();
        duracaoMinutos = dto.duracaoMinutos();
        substituicoes = new ArrayList<>();
        Map<Long, LinhaAtleta> linhas = new HashMap<>();
        escalacao.forEach(l -> { l.limpar(); linhas.put(l.getAtleta().getId(), l); });
        dto.participacoes().forEach(p -> {
            LinhaAtleta l = linhas.get(p.atletaId());
            if (l == null) {
                AtletaDTO atletaHistorico = atletas.stream()
                        .filter(atleta -> atleta.getId().equals(p.atletaId())).findFirst().orElse(null);
                if (atletaHistorico == null)
                    atletaHistorico = new AtletaDTO(p.atletaId(), p.nomeAtleta(), null, SituacaoAtleta.INATIVO);
                l = new LinhaAtleta(atletaHistorico);
                escalacao.add(l);
                linhas.put(p.atletaId(), l);
            }
            if (l != null) { l.papel = p.papel() == PapelParticipacao.RELACIONADO
                        ? PapelParticipacao.RESERVA : p.papel(); l.numeroCamisa = p.numeroCamisa();
                l.posicao = p.posicao(); l.slotTatico = p.slotTatico();
                l.coordenadaX = p.coordenadaX(); l.coordenadaY = p.coordenadaY();
                l.congelarInicial(); }
        });
        if (dto.substituicoes() != null) dto.substituicoes().forEach(item ->
                substituicoes.add(new SubstituicaoLinha(item.atletaSaiuId(), item.nomeAtletaSaiu(),
                        item.atletaEntrouId(), item.nomeAtletaEntrou(), item.minuto(), item.ordem())));
        reaplicarSubstituicoes();
        dto.eventos().forEach(e -> {
            if (e.tipo() == TipoEventoSumula.GOL_CONTRA) return;
            LinhaAtleta l = linhas.get(e.atletaId()); if (l == null) return;
            if (e.tipo() == TipoEventoSumula.GOL) l.gols++;
            else if (e.tipo() == TipoEventoSumula.CARTAO_AMARELO) l.amarelos++;
            else if (e.tipo() == TipoEventoSumula.CARTAO_VERMELHO) l.vermelhos++;
        });
    }

    public boolean isAcessoPro() { return disponibilidade != null && disponibilidade.acessoPro(); }
    public boolean isEditavel() { return disponibilidade != null && disponibilidade.editavel(); }
    public boolean isPlacarInformado() { return disponibilidade != null && disponibilidade.placarInformado(); }
    public boolean isPlacarConfirmado() { return disponibilidade != null && disponibilidade.placarConfirmado(); }
    public String getMensagemDisponibilidade() { return disponibilidade == null ? "" : disponibilidade.mensagem(); }
    public String getResumoPlacar() {
        if (disponibilidade == null) return "";
        String mandante = disponibilidade.nomeTimeMandante();
        String visitante = disponibilidade.nomeTimeVisitante();
        if (!disponibilidade.placarInformado()) return mandante + " x " + visitante + " · placar pendente";
        return mandante + " " + disponibilidade.golsMandante() + " x "
                + disponibilidade.golsVisitante() + " " + visitante;
    }
    public int getTotalGolsSumula() {
        return escalacao.stream().mapToInt(linha -> linha.getGols() == null ? 0 : linha.getGols()).sum();
    }
    public boolean isGolsConferem() {
        return disponibilidade != null && disponibilidade.golsDoTime() != null
                && getTotalGolsSumula() <= disponibilidade.golsDoTime();
    }
    public String getResumoConferenciaGols() {
        if (disponibilidade == null || disponibilidade.golsDoTime() == null)
            return "O placar do time ainda não está disponível para conferência.";
        return getTotalGolsSumula() + " de " + disponibilidade.golsDoTime()
                + " gol(s) atribuído(s) aos jogadores. A diferença pode ficar sem autor individual.";
    }
    public boolean isSumulaPublicada() { return "PUBLICADO".equals(status); }

    public void alterarFormacao() {
        if (!substituicoes.isEmpty()) {
            erro("Remova as substituicoes antes de alterar a formacao inicial.");
            return;
        }
        escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.TITULAR).forEach(l -> {
            l.setPapel(PapelParticipacao.RESERVA); l.limparPosicaoTatica();
        });
        atletaSelecionadoId = null;
    }

    public List<SlotTatico> getSlotsFormacao() {
        return switch (formacao == null ? "3-5-2" : formacao) {
            case "4-4-2" -> montarSlots(new int[]{4, 4, 2});
            case "4-3-3" -> montarSlots(new int[]{4, 3, 3});
            case "PERSONALIZADA" -> montarSlots(parseFormacaoPersonalizada());
            default -> montarSlots(new int[]{3, 5, 2});
        };
    }

    private int[] parseFormacaoPersonalizada() {
        try {
            if (formacaoPersonalizada == null || formacaoPersonalizada.isBlank()) return new int[]{3, 5, 2};
            String[] partes = formacaoPersonalizada.trim().split("-");
            if (partes.length < 2 || partes.length > 5) return new int[]{3, 5, 2};
            int[] valores = new int[partes.length];
            int total = 1;
            for (int i = 0; i < partes.length; i++) { valores[i] = Integer.parseInt(partes[i]); total += valores[i]; }
            return total <= 15 ? valores : new int[]{3, 5, 2};
        } catch (NumberFormatException e) { return new int[]{3, 5, 2}; }
    }

    private List<SlotTatico> montarSlots(int[] linhas) {
        List<SlotTatico> slots = new ArrayList<>();
        slots.add(new SlotTatico("GOL", "Goleiro", BigDecimal.valueOf(50), BigDecimal.valueOf(88)));
        for (int linha = 0; linha < linhas.length; linha++) {
            int quantidade = linhas[linha];
            double y = linhas.length == 1 ? 48 : 68 - (linha * (48d / (linhas.length - 1)));
            String setor = linha == 0 ? "Defesa" : linha == linhas.length - 1 ? "Ataque" : "Meio";
            for (int posicao = 0; posicao < quantidade; posicao++) {
                double x = (posicao + 1) * 100d / (quantidade + 1);
                slots.add(new SlotTatico("L" + linha + "P" + posicao, setor,
                        BigDecimal.valueOf(x), BigDecimal.valueOf(y)));
            }
        }
        return slots;
    }

    public LinhaAtleta getOcupante(SlotTatico slot) {
        return escalacao.stream().filter(l -> slot.id().equals(
                visaoInicial ? l.getSlotTaticoInicial() : l.getSlotTatico())).findFirst().orElse(null);
    }

    public void selecionarAtleta(LinhaAtleta linha) { atletaSelecionadoId = linha.getAtleta().getId(); }

    public void abrirSeletorPosicao(SlotTatico slot) {
        if (slot == null || visaoInicial || !substituicoes.isEmpty()) return;
        slotSelecionado = slot;
        atletaSelecionadoId = null;
    }

    public void escolherJogadorParaPosicao(LinhaAtleta linha) {
        if (slotSelecionado == null || linha == null || !substituicoes.isEmpty()) return;
        atletaSelecionadoId = linha.getAtleta().getId();
        clicarSlot(slotSelecionado);
        slotSelecionado = null;
    }

    public void cancelarSeletorPosicao() {
        slotSelecionado = null;
    }

    public String getTituloSeletorPosicao() {
        if (slotSelecionado == null) return "Escolher jogador";
        return (getOcupante(slotSelecionado) == null ? "Escolher" : "Trocar")
                + " jogador - " + slotSelecionado.rotulo();
    }

    public void prepararSubstituicaoPeloSlot(SlotTatico slot) {
        LinhaAtleta ocupante = slot == null ? null : getOcupante(slot);
        if (ocupante == null || ocupante.getPapel() != PapelParticipacao.TITULAR) {
            FacesContext.getCurrentInstance().validationFailed();
            erro("Selecione um titular que esteja atualmente em campo.");
            return;
        }
        atletaSaiuId = ocupante.getAtleta().getId();
        atletaEntrouId = null;
        minutoSubstituicao = null;
    }

    public void clicarSlot(SlotTatico slot) {
        if (visaoInicial) return;
        LinhaAtleta ocupante = getOcupante(slot);
        if (atletaSelecionadoId == null) {
            if (ocupante != null) atletaSelecionadoId = ocupante.getAtleta().getId();
            return;
        }
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado == null) return;
        if (!substituicoes.isEmpty() && selecionado.getPapel() != PapelParticipacao.TITULAR) {
            atletaSelecionadoId = null;
            erro("Use a acao Substituir para trocar atletas entre campo e reserva.");
            return;
        }
        if (ocupante != null && ocupante != selecionado) {
            if (!substituicoes.isEmpty() && ocupante.getPapel() == PapelParticipacao.TITULAR) {
                String slotAnterior = selecionado.slotTatico; String posicaoAnterior = selecionado.posicao;
                BigDecimal xAnterior = selecionado.coordenadaX; BigDecimal yAnterior = selecionado.coordenadaY;
                ocupante.slotTatico = slotAnterior; ocupante.posicao = posicaoAnterior;
                ocupante.coordenadaX = xAnterior; ocupante.coordenadaY = yAnterior;
            } else {
                ocupante.setPapel(PapelParticipacao.RESERVA); ocupante.limparPosicaoTatica();
            }
        }
        selecionado.setPapel(PapelParticipacao.TITULAR);
        selecionado.setSlotTatico(slot.id()); selecionado.setPosicao(slot.rotulo());
        selecionado.setCoordenadaX(slot.x()); selecionado.setCoordenadaY(slot.y());
        atletaSelecionadoId = null;
    }

    public void moverAtletaArrastado() {
        Map<String, String> parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        try {
            atletaSelecionadoId = Long.valueOf(parametros.get("atletaId"));
            String destino = parametros.get("destino");
            if ("RESERVA".equals(destino)) moverSelecionado(PapelParticipacao.RESERVA);
            else getSlotsFormacao().stream().filter(s -> s.id().equals(destino)).findFirst().ifPresent(this::clicarSlot);
        } catch (RuntimeException e) { erro("Não foi possível movimentar o atleta na prancheta."); }
    }

    public void moverParaReserva(LinhaAtleta linha) { atletaSelecionadoId = linha.getAtleta().getId(); moverSelecionado(PapelParticipacao.RESERVA); }
    public void removerDaPartida(LinhaAtleta linha) { linha.limpar(); atletaSelecionadoId = null; }

    public void moverSelecionadoParaDisponiveis() {
        if (!substituicoes.isEmpty()) { erro("Remova as substituicoes antes de alterar os relacionados."); return; }
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado != null) selecionado.limpar();
        atletaSelecionadoId = null;
        abaJogadoresMobile = "DISPONIVEIS";
    }

    public void moverSelecionadoParaReservas() {
        moverSelecionado(PapelParticipacao.RESERVA);
        abaJogadoresMobile = "RESERVAS";
    }
    public boolean isAtletaSelecionado() { return atletaSelecionadoId != null; }

    public boolean isSelecionadoNoCampo() {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        return selecionado != null && selecionado.getPapel() == PapelParticipacao.TITULAR;
    }

    public boolean isSelecionadoNaReserva() {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        return selecionado != null && selecionado.getPapel() == PapelParticipacao.RESERVA;
    }

    public boolean isSelecionadoDisponivel() {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        return selecionado != null && selecionado.getPapel() == null;
    }
    public String getNomeAtletaSelecionado() {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado == null) return "Atleta selecionado";
        AtletaDTO atleta = selecionado.getAtleta();
        return atleta.getApelido() == null || atleta.getApelido().isBlank()
                ? atleta.getNome() : atleta.getApelido();
    }

    private void moverSelecionado(PapelParticipacao papel) {
        if (!substituicoes.isEmpty()) { erro("Use a acao Substituir ou remova as trocas registradas."); return; }
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado != null) { selecionado.setPapel(papel); selecionado.limparPosicaoTatica(); }
        atletaSelecionadoId = null;
    }

    private LinhaAtleta linha(Long atletaId) {
        return escalacao.stream().filter(l -> l.getAtleta().getId().equals(atletaId)).findFirst().orElse(null);
    }

    public List<LinhaAtleta> getDisponiveis() { return escalacao.stream().filter(l -> l.getPapel() == null).toList(); }
    public List<LinhaAtleta> getReservas() { return escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.RESERVA).toList(); }
    public int getTotalParticipantes() { return (int) escalacao.stream().filter(l -> l.getPapel() != null).count(); }

    public boolean isEdicaoPranchetaPermitida() { return isEditavel() && !visaoInicial; }

    public List<LinhaAtleta> getTitularesAtuais() {
        return escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.TITULAR).toList();
    }

    public void registrarSubstituicao() {
        LinhaAtleta saiu = linha(atletaSaiuId);
        LinhaAtleta entrou = linha(atletaEntrouId);
        if (saiu == null || entrou == null || saiu == entrou
                || saiu.getPapel() != PapelParticipacao.TITULAR
                || entrou.getPapel() != PapelParticipacao.RESERVA) {
            FacesContext.getCurrentInstance().validationFailed();
            erro("Selecione um titular em campo e um reserva para entrar.");
            return;
        }
        if (minutoSubstituicao != null && (minutoSubstituicao < 0
                || (duracaoMinutos != null && minutoSubstituicao > duracaoMinutos))) {
            FacesContext.getCurrentInstance().validationFailed();
            erro("O minuto deve estar dentro da duracao informada.");
            return;
        }
        if (substituicoes.isEmpty()) escalacao.forEach(LinhaAtleta::congelarInicial);
        SubstituicaoLinha troca = new SubstituicaoLinha(saiu.getAtleta().getId(), nome(saiu),
                entrou.getAtleta().getId(), nome(entrou), minutoSubstituicao, substituicoes.size());
        substituicoes.add(troca);
        aplicarTroca(troca);
        atletaSaiuId = null; atletaEntrouId = null; minutoSubstituicao = null;
        visaoInicial = false;
        abaJogadoresMobile = "RESERVAS";
        info("Substituicao registrada.");
    }

    public void removerSubstituicao(SubstituicaoLinha troca) {
        int indice = substituicoes.indexOf(troca);
        if (indice < 0) return;
        substituicoes.remove(indice);
        if (!sequenciaValida()) {
            substituicoes.add(indice, troca);
            erro("Esta substituicao sustenta uma troca posterior. Remova primeiro as trocas dependentes.");
            return;
        }
        for (int i = 0; i < substituicoes.size(); i++) substituicoes.get(i).setOrdem(i);
        reaplicarSubstituicoes();
    }

    private boolean sequenciaValida() {
        Map<Long, PapelParticipacao> estado = new HashMap<>();
        escalacao.forEach(l -> estado.put(l.atleta.getId(), l.papelInicial));
        for (SubstituicaoLinha item : substituicoes.stream()
                .sorted(java.util.Comparator.comparing(SubstituicaoLinha::getOrdem)).toList()) {
            if (estado.get(item.atletaSaiuId) != PapelParticipacao.TITULAR
                    || estado.get(item.atletaEntrouId) == PapelParticipacao.TITULAR) return false;
            estado.put(item.atletaSaiuId, PapelParticipacao.RESERVA);
            estado.put(item.atletaEntrouId, PapelParticipacao.TITULAR);
        }
        return true;
    }

    public void exibirEscalacaoInicial() { visaoInicial = true; atletaSelecionadoId = null; }
    public void exibirSituacaoAtual() { visaoInicial = false; }

    private void reaplicarSubstituicoes() {
        escalacao.forEach(LinhaAtleta::restaurarInicial);
        substituicoes.stream().sorted(java.util.Comparator.comparing(SubstituicaoLinha::getOrdem))
                .forEach(this::aplicarTroca);
    }

    private void aplicarTroca(SubstituicaoLinha troca) {
        LinhaAtleta saiu = linha(troca.atletaSaiuId);
        LinhaAtleta entrou = linha(troca.atletaEntrouId);
        if (saiu == null || entrou == null) return;
        String slot = saiu.slotTatico; String posicaoAtual = saiu.posicao;
        BigDecimal x = saiu.coordenadaX; BigDecimal y = saiu.coordenadaY;
        saiu.papel = PapelParticipacao.RESERVA; saiu.limparPosicaoTatica();
        entrou.papel = PapelParticipacao.TITULAR; entrou.slotTatico = slot;
        entrou.posicao = posicaoAtual; entrou.coordenadaX = x; entrou.coordenadaY = y;
    }

    public String getMinutosJogador(LinhaAtleta linha) {
        Integer minutos = calcularMinutos(linha);
        return minutos == null ? "Nao informado" : minutos + " min";
    }

    private Integer calcularMinutos(LinhaAtleta alvo) {
        if (duracaoMinutos == null || substituicoes.stream().anyMatch(s -> s.minuto == null)) return null;
        boolean emCampo = (substituicoes.isEmpty() ? alvo.papel : alvo.papelInicial)
                == PapelParticipacao.TITULAR;
        int inicio = 0, total = 0;
        List<int[]> intervalos = new ArrayList<>();
        for (SubstituicaoLinha troca : substituicoes.stream()
                .sorted(java.util.Comparator.comparing(SubstituicaoLinha::getOrdem)).toList()) {
            if (troca.atletaSaiuId.equals(alvo.atleta.getId()) && emCampo) {
                intervalos.add(new int[]{inicio, troca.minuto}); emCampo = false;
            } else if (troca.atletaEntrouId.equals(alvo.atleta.getId()) && !emCampo) {
                inicio = troca.minuto; emCampo = true;
            }
        }
        if (emCampo) intervalos.add(new int[]{inicio, duracaoMinutos});
        for (int[] intervalo : intervalos) {
            total += Math.max(0, intervalo[1] - intervalo[0]);
        }
        return total;
    }

    private String nome(LinhaAtleta linha) {
        return linha.atleta.getApelido() == null || linha.atleta.getApelido().isBlank()
                ? linha.atleta.getNome() : linha.atleta.getApelido();
    }

    private String mensagem(Exception e, String padrao) {
        if (e instanceof RestClientResponseException re && re.getResponseBodyAsString() != null
                && !re.getResponseBodyAsString().isBlank()) return re.getResponseBodyAsString();
        return padrao;
    }
    private void info(String m) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null)); }
    private void erro(String m) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null)); }

    @Getter @Setter
    public static class LinhaAtleta implements Serializable {
        private AtletaDTO atleta;
        private PapelParticipacao papel;
        private Integer numeroCamisa;
        private String posicao;
        private String slotTatico;
        private BigDecimal coordenadaX;
        private BigDecimal coordenadaY;
        private Integer gols = 0;
        private Integer amarelos = 0;
        private Integer vermelhos = 0;
        private PapelParticipacao papelInicial;
        private String posicaoInicial;
        private String slotTaticoInicial;
        private BigDecimal coordenadaXInicial;
        private BigDecimal coordenadaYInicial;
        public LinhaAtleta(AtletaDTO atleta) { this.atleta = atleta; }
        void limparPosicaoTatica() { slotTatico=null; coordenadaX=null; coordenadaY=null; }
        void limpar() { papel=null; numeroCamisa=null; posicao=null; limparPosicaoTatica(); gols=0; amarelos=0; vermelhos=0; papelInicial=null; posicaoInicial=null; slotTaticoInicial=null; coordenadaXInicial=null; coordenadaYInicial=null; }
        void congelarInicial() { papelInicial=papel; posicaoInicial=posicao; slotTaticoInicial=slotTatico; coordenadaXInicial=coordenadaX; coordenadaYInicial=coordenadaY; }
        void restaurarInicial() { papel=papelInicial; posicao=posicaoInicial; slotTatico=slotTaticoInicial; coordenadaX=coordenadaXInicial; coordenadaY=coordenadaYInicial; }
    }

    @Getter @Setter
    public static class SubstituicaoLinha implements Serializable {
        private Long atletaSaiuId;
        private String nomeAtletaSaiu;
        private Long atletaEntrouId;
        private String nomeAtletaEntrou;
        private Integer minuto;
        private Integer ordem;
        public SubstituicaoLinha(Long atletaSaiuId, String nomeAtletaSaiu, Long atletaEntrouId,
                                 String nomeAtletaEntrou, Integer minuto, Integer ordem) {
            this.atletaSaiuId=atletaSaiuId; this.nomeAtletaSaiu=nomeAtletaSaiu;
            this.atletaEntrouId=atletaEntrouId; this.nomeAtletaEntrou=nomeAtletaEntrou;
            this.minuto=minuto; this.ordem=ordem;
        }
    }

    public record SlotTatico(String id, String rotulo, BigDecimal x, BigDecimal y) implements Serializable {
        // O Jakarta EL resolve propriedades pelo padrão JavaBean (getX),
        // enquanto records expõem originalmente apenas x(), y(), etc.
        public String getId() { return id; }
        public String getRotulo() { return rotulo; }
        public BigDecimal getX() { return x; }
        public BigDecimal getY() { return y; }
    }
}
