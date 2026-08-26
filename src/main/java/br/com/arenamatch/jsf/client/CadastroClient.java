package br.com.arenamatch.jsf.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.CadastroResponsavelRequestDTO;
import br.com.arenamatch.dto.CadastroDisponibilidadeRequestDTO;
import br.com.arenamatch.dto.CadastroFinalizacaoRequestDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.enums.Categoria;
import java.io.IOException;
import java.util.List;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;

@Component
public class CadastroClient {
    private final RestClient restClient;

    public CadastroClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public boolean enviarCadastro(CadastroDTO dto) {
        try {
            restClient.post().uri("/api/cadastro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void salvarTime(CadastroDTO dto) {
        restClient.post()
                .uri("/api/cadastro") // Verifique se sua API usa esta URL
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public CadastroDTO buscarDadosParaEdicao(Long idUsuario) {
        return restClient.get()
                .uri("/api/cadastro/" + idUsuario)
                .retrieve()
                .body(CadastroDTO.class);
    }

    public void atualizarConta(Long idUsuario, CadastroDTO dto) {
        restClient.put()
                .uri("/api/cadastro/" + idUsuario)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public void desativarConta() {
        restClient.put()
                .uri("/api/cadastro/minha-conta/desativar")
                .retrieve()
                .toBodilessEntity();
    }

    public void validarResponsavel(CadastroDTO dto, String confirmarSenha, boolean novoCadastro) {
        CadastroResponsavelRequestDTO request = new CadastroResponsavelRequestDTO();
        request.setCadastro(dto);
        request.setConfirmarSenha(confirmarSenha);
        request.setNovoCadastro(novoCadastro);
        postSemResposta("/api/cadastro/validacoes/responsavel", request);
    }

    public void validarTime(CadastroDTO dto) {
        postSemResposta("/api/cadastro/validacoes/time", dto);
    }

    public DisponibilidadeDTO criarDisponibilidade(CadastroDTO dto, List<DisponibilidadeDTO> agenda,
            Categoria categoria, String dia, String inicio, String fim) {
        CadastroDisponibilidadeRequestDTO request = new CadastroDisponibilidadeRequestDTO();
        request.setCadastro(dto); request.setAgenda(agenda); request.setCategoria(categoria);
        request.setDia(dia); request.setInicio(inicio); request.setFim(fim);
        return restClient.post().uri("/api/cadastro/disponibilidades")
                .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().body(DisponibilidadeDTO.class);
    }

    public void validarFinalizacao(CadastroDTO dto, List<DisponibilidadeDTO> agenda, boolean novoCadastro) {
        CadastroFinalizacaoRequestDTO request = new CadastroFinalizacaoRequestDTO();
        request.setCadastro(dto); request.setAgenda(agenda); request.setNovoCadastro(novoCadastro);
        postSemResposta("/api/cadastro/validacoes/finalizacao", request);
    }

    public EnderecoDTO buscarEnderecoPorCep(String cep) {
        return restClient.get().uri(builder -> builder.path("/api/cadastro/endereco/cep")
                .queryParam("cep", cep).build()).retrieve().body(EnderecoDTO.class);
    }

    public EnderecoDTO buscarEnderecoPorCoordenadas(Double latitude, Double longitude) {
        return restClient.get().uri(builder -> builder.path("/api/cadastro/endereco/coordenadas")
                .queryParam("latitude", latitude).queryParam("longitude", longitude).build())
                .retrieve().body(EnderecoDTO.class);
    }

    public String uploadEscudo(UploadedFile arquivo) {
        try {
            byte[] conteudo = arquivo.getInputStream().readAllBytes();
            MultipartBodyBuilder multipart = new MultipartBodyBuilder();
            multipart.part("arquivo", new ByteArrayResource(conteudo) {
                @Override public String getFilename() { return arquivo.getFileName(); }
            }).contentType(MediaType.parseMediaType(arquivo.getContentType()));
            return restClient.post().uri("/api/cadastro/escudo")
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(multipart.build())
                    .retrieve().body(String.class);
        } catch (IOException e) {
            throw new RuntimeException("Nao foi possivel ler o arquivo do escudo.", e);
        }
    }

    private void postSemResposta(String uri, Object body) {
        restClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().toBodilessEntity();
    }
}
