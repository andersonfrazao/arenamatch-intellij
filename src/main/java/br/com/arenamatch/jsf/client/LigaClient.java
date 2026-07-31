package br.com.arenamatch.jsf.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.CandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.BanimentoLigaDTO;
import br.com.arenamatch.dto.BanirTimeLigaDTO;
import br.com.arenamatch.dto.EnviarConviteLigaDTO;
import br.com.arenamatch.dto.JogoRecenteLigaDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.NovaLigaDTO;
import br.com.arenamatch.dto.NovaPublicacaoLigaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.PublicacaoLigaDTO;
import br.com.arenamatch.dto.RankingLigaDTO;
import br.com.arenamatch.dto.ResponderConviteLigaDTO;
import br.com.arenamatch.dto.ResultadoCandidaturaPublicacaoLigaDTO;
import br.com.arenamatch.dto.ScoutLigaDTO;

@Service
public class LigaClient {

    @Autowired
    private RestClient restClient;

    public void criarLiga(Long idTimeAdmin, String nome, String descricao) {
        NovaLigaDTO dto = new NovaLigaDTO();
        dto.setIdTimeAdmin(idTimeAdmin);
        dto.setNome(nome);
        dto.setDescricao(descricao);

        restClient.post()
                .uri("/api/ligas")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<br.com.arenamatch.dto.LigaDetalheDTO> buscarLigasDoTime(Long timeId) {
        br.com.arenamatch.dto.LigaDetalheDTO[] ligas = restClient.get()
                .uri("/api/ligas/time/" + timeId)
                .retrieve()
                .body(br.com.arenamatch.dto.LigaDetalheDTO[].class);
        return ligas != null ? Arrays.asList(ligas) : new ArrayList<>();
    }

    public List<ConviteLigaDTO> buscarConvitesPendentes(Long timeId) {
        ConviteLigaDTO[] convites = restClient.get()
                .uri("/api/ligas/convites/time/" + timeId)
                .retrieve()
                .body(ConviteLigaDTO[].class);
        return convites != null ? Arrays.asList(convites) : new ArrayList<>();
    }

    public void responderConvite(Long idConvite, boolean aceitar) {
        ResponderConviteLigaDTO dto = new ResponderConviteLigaDTO();
        dto.setIdConvite(idConvite);
        dto.setAceitar(aceitar);

        restClient.post()
                .uri("/api/ligas/convites/responder")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
 // Atualize o retorno para LigaDetalheDTO
    public br.com.arenamatch.dto.LigaDetalheDTO buscarLigaPorId(Long id) {
        return restClient.get()
                .uri("/api/ligas/" + id)
                .retrieve()
                .body(br.com.arenamatch.dto.LigaDetalheDTO.class);
    }

    public List<PartidaDTO> listarJogosDaLiga(Long ligaId) {
        PartidaDTO[] jogos = restClient.get()
                .uri("/api/ligas/" + ligaId + "/jogos")
                .retrieve()
                .body(PartidaDTO[].class);
        return jogos != null ? new ArrayList<>(Arrays.asList(jogos)) : new ArrayList<>();
    }

    public PublicacaoLigaDTO criarPublicacao(Long ligaId, NovaPublicacaoLigaDTO dto) {
        return restClient.post()
                .uri("/api/ligas/" + ligaId + "/publicacoes")
                .body(dto)
                .retrieve()
                .body(PublicacaoLigaDTO.class);
    }

    public PublicacaoLigaDTO criarPublicacaoGlobal(NovaPublicacaoLigaDTO dto) {
        return restClient.post()
                .uri("/api/ligas/mural/publicacoes")
                .body(dto)
                .retrieve()
                .body(PublicacaoLigaDTO.class);
    }

    public List<PublicacaoLigaDTO> listarPublicacoesDaLiga(Long ligaId) {
        PublicacaoLigaDTO[] publicacoes = restClient.get()
                .uri("/api/ligas/" + ligaId + "/publicacoes")
                .retrieve()
                .body(PublicacaoLigaDTO[].class);
        return publicacoes != null ? new ArrayList<>(Arrays.asList(publicacoes)) : new ArrayList<>();
    }

    public List<PublicacaoLigaDTO> listarPublicacoesGlobais(Long meuTimeId) {
        PublicacaoLigaDTO[] publicacoes = restClient.get()
                .uri("/api/ligas/mural/publicacoes?meuTimeId=" + meuTimeId)
                .retrieve()
                .body(PublicacaoLigaDTO[].class);
        return publicacoes != null ? new ArrayList<>(Arrays.asList(publicacoes)) : new ArrayList<>();
    }

    public List<JogoRecenteLigaDTO> listarJogosRecentesDoMural(int limite) {
        JogoRecenteLigaDTO[] jogos = restClient.get()
                .uri("/api/ligas/mural/jogos-recentes?limit=" + limite)
                .retrieve()
                .body(JogoRecenteLigaDTO[].class);
        return jogos != null ? new ArrayList<>(Arrays.asList(jogos)) : new ArrayList<>();
    }

    public ResultadoCandidaturaPublicacaoLigaDTO candidatarPublicacao(
            Long idPublicacao,
            Long idTimeCandidato,
            Long idLigaCandidato) {
        CandidaturaPublicacaoLigaDTO dto = new CandidaturaPublicacaoLigaDTO();
        dto.setIdTimeCandidato(idTimeCandidato);
        dto.setIdLigaCandidato(idLigaCandidato);

        return restClient.post()
                .uri("/api/ligas/mural/publicacoes/" + idPublicacao + "/candidatar")
                .body(dto)
                .retrieve()
                .body(ResultadoCandidaturaPublicacaoLigaDTO.class);
    }

    public void cancelarPublicacao(Long idPublicacao, Long idTimeSolicitante) {
        restClient.delete()
                .uri("/api/ligas/publicacoes/" + idPublicacao + "/time/" + idTimeSolicitante)
                .retrieve()
                .toBodilessEntity();
    }

    public List<RankingLigaDTO> buscarRankingDaLiga(Long ligaId) {
        RankingLigaDTO[] ranking = restClient.get()
                .uri("/api/ligas/" + ligaId + "/ranking")
                .retrieve()
                .body(RankingLigaDTO[].class);
        return ranking != null ? new ArrayList<>(Arrays.asList(ranking)) : new ArrayList<>();
    }

    public ScoutLigaDTO buscarScoutDaLiga(Long ligaId, Long idTime) {
        return restClient.get()
                .uri("/api/ligas/" + ligaId + "/scout/time/" + idTime)
                .retrieve()
                .body(ScoutLigaDTO.class);
    }

    // Atualize o retorno para List<TimeSimplesDTO>
    public List<br.com.arenamatch.dto.TimeSimplesDTO> buscarTimesPorNome(String nome) {
        br.com.arenamatch.dto.TimeSimplesDTO[] times = restClient.get()
                .uri("/api/times/buscar-por-nome?nome=" + nome) 
                .retrieve()
                .body(br.com.arenamatch.dto.TimeSimplesDTO[].class);
                
        // AGORA SIM! Criamos um ArrayList de verdade e mutável:
        return times != null ? new ArrayList<>(Arrays.asList(times)) : new ArrayList<>();
    }
    
 // --- MÉTODO PARA ENVIAR CONVITE (Ponto 3) ---
    public void enviarConvite(Long idLiga, Long idTimeConvidado, String mensagem) {
        EnviarConviteLigaDTO dto = new EnviarConviteLigaDTO();
        dto.setIdLiga(idLiga);
        dto.setIdTimeConvidado(idTimeConvidado);
        dto.setMensagem(mensagem);

        restClient.post()
                .uri("/api/ligas/convites")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    // --- MÉTODO PARA REMOVER MEMBRO (Ponto 2) ---
    public void removerMembro(Long idLiga, Long idTime, Long idTimeSolicitante) {
        restClient.delete()
                .uri("/api/ligas/" + idLiga + "/membros/" + idTime + "?idTimeSolicitante=" + idTimeSolicitante)
                .retrieve()
                .toBodilessEntity();
    }

    public BanimentoLigaDTO banirMembro(Long idLiga, Long idTime, Long idTimeAdmin, String motivo) {
        BanirTimeLigaDTO dto = new BanirTimeLigaDTO();
        dto.setIdTime(idTime);
        dto.setIdTimeAdmin(idTimeAdmin);
        dto.setMotivo(motivo);

        return restClient.post()
                .uri("/api/ligas/" + idLiga + "/banimentos")
                .body(dto)
                .retrieve()
                .body(BanimentoLigaDTO.class);
    }

    public List<BanimentoLigaDTO> listarBanimentosAtivos(Long idLiga) {
        BanimentoLigaDTO[] banimentos = restClient.get()
                .uri("/api/ligas/" + idLiga + "/banimentos/ativos")
                .retrieve()
                .body(BanimentoLigaDTO[].class);
        return banimentos != null ? new ArrayList<>(Arrays.asList(banimentos)) : new ArrayList<>();
    }

    public void reverterBanimento(Long idLiga, Long idTime, Long idTimeAdmin) {
        restClient.delete()
                .uri("/api/ligas/" + idLiga + "/banimentos/" + idTime + "?idTimeAdmin=" + idTimeAdmin)
                .retrieve()
                .toBodilessEntity();
    }
    
    public List<ConviteLigaDTO> buscarConvitesParaAgenda(Long timeId) {
        ConviteLigaDTO[] convites = restClient.get()
                .uri("/api/ligas/convites/agenda/time/" + timeId)
                .retrieve()
                .body(ConviteLigaDTO[].class);
        return convites != null ? new ArrayList<>(Arrays.asList(convites)) : new ArrayList<>();
    }
    
 // --- NOVO: CONSUMIR OS IDS COM CONVITE PENDENTE ---
    public List<Long> buscarIdsTimesComConvitePendente(Long ligaId) {
        Long[] ids = restClient.get()
                .uri("/api/ligas/" + ligaId + "/convites/pendentes/times")
                .retrieve()
                .body(Long[].class);
        return ids != null ? new ArrayList<>(Arrays.asList(ids)) : new ArrayList<>();
    }
    
    public List<LigaExplorarDTO> listarLigasEmAlta(Long meuTimeId) {
        LigaExplorarDTO[] array = restClient.get()
                .uri("/api/ligas/explorar/top/" + meuTimeId)
                .retrieve()
                .body(LigaExplorarDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public List<LigaExplorarDTO> buscarLigasPorNome(String nomeBusca, Long meuTimeId) {
        LigaExplorarDTO[] array = restClient.get()
                .uri("/api/ligas/explorar/busca/" + nomeBusca + "/" + meuTimeId)
                .retrieve()
                .body(LigaExplorarDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public void solicitarEntradaNaLiga(Long idLiga, Long meuTimeId) {
        restClient.post()
                .uri("/api/ligas/" + idLiga + "/solicitar-entrada/" + meuTimeId)
                .retrieve()
                .toBodilessEntity();
    }
    
    
}
