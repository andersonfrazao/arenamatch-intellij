package br.com.arenamatch.jsf.beans;

import br.com.arenamatch.dto.CampeonatoDTO;
import br.com.arenamatch.dto.CampeonatoRequestDTO;
import br.com.arenamatch.enums.CriterioDesempate;
import br.com.arenamatch.enums.FormatoCampeonato;
import br.com.arenamatch.jsf.client.CampeonatoClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named @ViewScoped @Getter @Setter
public class CampeonatoBean implements Serializable {
    @Inject private CampeonatoClient client;
    @Inject private SessaoBean sessao;
    @Inject private CampeonatoAcessoBean acesso;
    private boolean inicializado;
    private boolean erroLista;
    private boolean formularioAberto;
    private boolean somenteLeitura;
    private Long editandoId;
    private CampeonatoRequestDTO formulario = new CampeonatoRequestDTO();
    private List<CampeonatoDTO> campeonatos = List.of();
    private String categoriaPreset = "Livre";

    public void inicializar() {
        if (inicializado || !sessao.isLogado()) return;
        inicializado = true;
        carregar();
    }
    public void carregar() {
        try { campeonatos = client.listar(); erroLista = false; }
        catch (Exception e) { campeonatos = List.of(); erroLista = true; erro(e); }
    }
    public long getAbertos() { return campeonatos.stream().filter(c -> c.getStatus().ocupaVaga()).count(); }
    public boolean isPodeCriar() { return !erroLista && acesso.isPodeOrganizar() && getAbertos() < 3; }
    public void novo() {
        if (!isPodeCriar()) { mensagem(FacesMessage.SEVERITY_WARN, "Não é possível criar outro campeonato agora."); return; }
        formulario = new CampeonatoRequestDTO(); editandoId = null; categoriaPreset = "Livre";
        somenteLeitura = false; formularioAberto = true;
    }
    public void abrir(CampeonatoDTO dto) {
        try {
            CampeonatoDTO atual = client.consultar(dto.getId());
            formulario = atual; editandoId = atual.getId();
            categoriaPreset = List.of("Livre", "Veterano 35+", "Veterano 40+", "Masters").contains(atual.getCategoria()) ? atual.getCategoria() : "Outra";
            somenteLeitura = !acesso.isPodeOrganizar() || !atual.getStatus().ocupaVaga();
            formularioAberto = true;
        } catch (Exception e) { erro(e); }
    }
    public void salvar() {
        if (somenteLeitura || !acesso.isPodeOrganizar()) return;
        try {
            CampeonatoDTO salvo = client.salvar(editandoId, formulario);
            if (salvo == null) throw new IllegalStateException("Resposta vazia");
            formularioAberto = false; carregar();
            mensagem(FacesMessage.SEVERITY_INFO, "Rascunho salvo.");
        } catch (Exception e) { erro(e); }
    }
    public void cancelar(CampeonatoDTO dto) {
        if (!acesso.isPodeOrganizar()) return;
        try { client.cancelar(dto); formularioAberto = false; carregar(); mensagem(FacesMessage.SEVERITY_INFO, "Rascunho cancelado. O histórico foi preservado."); }
        catch (Exception e) { erro(e); }
    }
    public void fechar() { formularioAberto = false; }
    public void alterarCategoria() {
        formulario.setCategoria("Outra".equals(categoriaPreset) ? "" : categoriaPreset);
        formulario.setIdadeMinima(switch (categoriaPreset) { case "Veterano 35+" -> 35; case "Veterano 40+" -> 40; default -> null; });
    }
    public void alterarFormato() {
        if (formulario.getFormato() != FormatoCampeonato.GRUPOS_MATA_MATA) {
            formulario.setQuantidadeGrupos(null); formulario.setTimesPorGrupo(null); formulario.setClassificadosPorGrupo(null);
        }
        if (formulario.getFormato() == FormatoCampeonato.LIGA) formulario.setIdaVoltaEliminatoria(false);
        if (formulario.getFormato() == FormatoCampeonato.MATA_MATA) formulario.setIdaVoltaClassificatoria(false);
    }
    public void moverCriterio(int index, int direcao) {
        int destino = index + direcao;
        List<CriterioDesempate> lista = new ArrayList<>(formulario.getDesempates());
        if (index < 0 || index >= lista.size() || destino < 0 || destino >= lista.size()) return;
        java.util.Collections.swap(lista, index, destino); formulario.setDesempates(lista);
    }
    public FormatoCampeonato[] getFormatos() { return FormatoCampeonato.values(); }
    public boolean isGrupos() { return formulario.getFormato() == FormatoCampeonato.GRUPOS_MATA_MATA; }
    public boolean isClassificatoria() { return formulario.getFormato() != FormatoCampeonato.MATA_MATA; }
    public boolean isEliminatoria() { return formulario.getFormato() != FormatoCampeonato.LIGA; }
    public boolean isRegrasBloqueadas() { return somenteLeitura || (formulario instanceof CampeonatoDTO dto && dto.isTabelaPublicada()); }
    public Integer getAnoReferencia() { return formulario.getDataInicio() == null ? null : formulario.getDataInicio().getYear(); }
    private void erro(Exception e) {
        String texto = "Não foi possível concluir a operação. Tente novamente.";
        if (e instanceof RestClientResponseException r) {
            try {
                com.fasterxml.jackson.databind.JsonNode body = new com.fasterxml.jackson.databind.ObjectMapper().readTree(r.getResponseBodyAsString());
                if (body.hasNonNull("detail")) texto = body.get("detail").asText();
                else if (body.hasNonNull("message")) texto = body.get("message").asText();
            } catch (Exception ignored) { /* Resposta sem detalhes utilizaveis. */ }
            if (r.getStatusCode().value() == 403) texto = "Seu acesso PREMIUM precisa estar ativo para alterar campeonatos.";
            else if (r.getStatusCode().value() == 409 && texto.startsWith("Não foi possível")) texto = "O campeonato mudou ou o limite de três foi atingido. Atualize a lista.";
        }
        mensagem(FacesMessage.SEVERITY_ERROR, texto);
    }
    private void mensagem(FacesMessage.Severity severity, String texto) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) context.addMessage(null, new FacesMessage(severity, texto, null));
    }
}
