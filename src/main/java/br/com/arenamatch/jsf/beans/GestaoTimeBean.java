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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Long atletaEmEdicaoId;
    private String formacao = "3-5-2";
    private String formacaoPersonalizada;
    private Long atletaSelecionadoId;
    private Long partidaId;
    private Long versao;
    private String status;
    private DisponibilidadeGestaoPartidaDTO disponibilidade;

    @PostConstruct
    public void init() {
        if (!sessaoBean.isAssinantePro()) return;
        carregarAtletas();
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("partida");
        if (id != null && !id.isBlank()) {
            try { partidaId = Long.valueOf(id); carregarPartida(); }
            catch (NumberFormatException e) { erro("Partida informada é inválida."); }
        }
    }

    public void adicionarAtleta() {
        try {
            boolean editando = atletaEmEdicaoId != null;
            if (atletaEmEdicaoId == null) atletaClient.criar(novoNome, novoApelido);
            else atletaClient.atualizar(atletaEmEdicaoId, novoNome, novoApelido);
            cancelarEdicaoAtleta();
            carregarAtletas();
            info(editando ? "Atleta atualizado." : "Atleta adicionado ao elenco.");
        } catch (Exception e) { erro(mensagem(e, "Não foi possível cadastrar o atleta.")); }
    }

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

    private GestaoPartidaRequestDTO montarRequest(boolean publicar) {
        List<GestaoPartidaRequestDTO.ParticipacaoRequestDTO> participacoes = new ArrayList<>();
        List<GestaoPartidaRequestDTO.EventoRequestDTO> eventos = new ArrayList<>();
        int ordem = 0;
        for (LinhaAtleta linha : escalacao) {
            if (linha.getPapel() == null) continue;
            participacoes.add(new GestaoPartidaRequestDTO.ParticipacaoRequestDTO(linha.getAtleta().getId(),
                    linha.getPapel(), linha.getNumeroCamisa(), linha.getPosicao(), linha.getSlotTatico(),
                    linha.getCoordenadaX(), linha.getCoordenadaY(), ordem++));
            repetir(eventos, linha, TipoEventoSumula.GOL, linha.getGols());
            repetir(eventos, linha, TipoEventoSumula.CARTAO_AMARELO, linha.getAmarelos());
            repetir(eventos, linha, TipoEventoSumula.CARTAO_VERMELHO, linha.getVermelhos());
        }
        EtapaGestaoPartida etapa = publicar ? EtapaGestaoPartida.PUBLICACAO
                : (isPlacarInformado() ? EtapaGestaoPartida.OCORRENCIAS : EtapaGestaoPartida.ESCALACAO);
        return new GestaoPartidaRequestDTO(versao, etapa, formacao, formacaoPersonalizada,
                participacoes, eventos);
    }

    private void repetir(List<GestaoPartidaRequestDTO.EventoRequestDTO> eventos, LinhaAtleta linha,
                         TipoEventoSumula tipo, Integer quantidade) {
        for (int i = 0; i < (quantidade == null ? 0 : quantidade); i++)
            eventos.add(new GestaoPartidaRequestDTO.EventoRequestDTO(linha.getAtleta().getId(), tipo, null));
    }

    private void carregarAtletas() {
        try {
            atletas = atletaClient.listar();
            Map<Long, LinhaAtleta> atuais = new HashMap<>();
            escalacao.forEach(l -> atuais.put(l.getAtleta().getId(), l));
            escalacao = atletas.stream().filter(a -> a.getSituacao() == SituacaoAtleta.ATIVO)
                    .map(a -> atuais.getOrDefault(a.getId(), new LinhaAtleta(a))).toList();
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

    private void aplicar(GestaoPartidaDTO dto) {
        versao = dto.versao(); status = dto.status() == null ? null : dto.status().name();
        formacao = dto.formacao() == null ? "3-5-2" : dto.formacao();
        formacaoPersonalizada = dto.formacaoPersonalizada();
        Map<Long, LinhaAtleta> linhas = new HashMap<>();
        escalacao.forEach(l -> { l.limpar(); linhas.put(l.getAtleta().getId(), l); });
        dto.participacoes().forEach(p -> {
            LinhaAtleta l = linhas.get(p.atletaId());
            if (l != null) { l.papel = p.papel(); l.numeroCamisa = p.numeroCamisa();
                l.posicao = p.posicao(); l.slotTatico = p.slotTatico();
                l.coordenadaX = p.coordenadaX(); l.coordenadaY = p.coordenadaY(); }
        });
        dto.eventos().forEach(e -> {
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

    public void alterarFormacao() {
        escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.TITULAR).forEach(l -> {
            l.setPapel(PapelParticipacao.RELACIONADO); l.limparPosicaoTatica();
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
        return escalacao.stream().filter(l -> slot.id().equals(l.getSlotTatico())).findFirst().orElse(null);
    }

    public void selecionarAtleta(LinhaAtleta linha) { atletaSelecionadoId = linha.getAtleta().getId(); }

    public void clicarSlot(SlotTatico slot) {
        LinhaAtleta ocupante = getOcupante(slot);
        if (atletaSelecionadoId == null) {
            if (ocupante != null) atletaSelecionadoId = ocupante.getAtleta().getId();
            return;
        }
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado == null) return;
        if (ocupante != null && ocupante != selecionado) {
            ocupante.setPapel(PapelParticipacao.RELACIONADO); ocupante.limparPosicaoTatica();
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
            else if ("RELACIONADO".equals(destino)) moverSelecionado(PapelParticipacao.RELACIONADO);
            else getSlotsFormacao().stream().filter(s -> s.id().equals(destino)).findFirst().ifPresent(this::clicarSlot);
        } catch (RuntimeException e) { erro("Não foi possível movimentar o atleta na prancheta."); }
    }

    public void moverParaReserva(LinhaAtleta linha) { atletaSelecionadoId = linha.getAtleta().getId(); moverSelecionado(PapelParticipacao.RESERVA); }
    public void relacionar(LinhaAtleta linha) { atletaSelecionadoId = linha.getAtleta().getId(); moverSelecionado(PapelParticipacao.RELACIONADO); }
    public void removerDaPartida(LinhaAtleta linha) { linha.limpar(); atletaSelecionadoId = null; }

    public void moverSelecionadoParaDisponiveis() {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado != null) selecionado.limpar();
        atletaSelecionadoId = null;
    }

    public void moverSelecionadoParaRelacionados() { moverSelecionado(PapelParticipacao.RELACIONADO); }
    public void moverSelecionadoParaReservas() { moverSelecionado(PapelParticipacao.RESERVA); }
    public boolean isAtletaSelecionado() { return atletaSelecionadoId != null; }

    private void moverSelecionado(PapelParticipacao papel) {
        LinhaAtleta selecionado = linha(atletaSelecionadoId);
        if (selecionado != null) { selecionado.setPapel(papel); selecionado.limparPosicaoTatica(); }
        atletaSelecionadoId = null;
    }

    private LinhaAtleta linha(Long atletaId) {
        return escalacao.stream().filter(l -> l.getAtleta().getId().equals(atletaId)).findFirst().orElse(null);
    }

    public List<LinhaAtleta> getDisponiveis() { return escalacao.stream().filter(l -> l.getPapel() == null).toList(); }
    public List<LinhaAtleta> getRelacionados() { return escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.RELACIONADO).toList(); }
    public List<LinhaAtleta> getReservas() { return escalacao.stream().filter(l -> l.getPapel() == PapelParticipacao.RESERVA).toList(); }
    public int getTotalParticipantes() { return (int) escalacao.stream().filter(l -> l.getPapel() != null).count(); }

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
        public LinhaAtleta(AtletaDTO atleta) { this.atleta = atleta; }
        void limparPosicaoTatica() { slotTatico=null; coordenadaX=null; coordenadaY=null; }
        void limpar() { papel=null; numeroCamisa=null; posicao=null; limparPosicaoTatica(); gols=0; amarelos=0; vermelhos=0; }
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
