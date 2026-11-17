package br.com.arenamatch.jsf.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.primefaces.model.file.UploadedFile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CadastroClientTest {

    @Test
    void enviaEscudoComoMultipartComNomeTipoEConteudo() throws IOException {
        var construtor = RestClient.builder().baseUrl("http://localhost");
        var servidor = MockRestServiceServer.bindTo(construtor).build();
        UploadedFile arquivo = mock(UploadedFile.class);
        var entrada = spy(new ByteArrayInputStream("imagem-ficticia".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        when(arquivo.getInputStream()).thenReturn(entrada);
        when(arquivo.getFileName()).thenReturn("escudo.png");
        when(arquivo.getContentType()).thenReturn("image/png");

        servidor.expect(requestTo("http://localhost/api/cadastro/escudo"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(containsString("name=\"arquivo\"; filename=\"escudo.png\"")))
                .andExpect(content().string(containsString("Content-Type: image/png")))
                .andExpect(content().string(containsString("imagem-ficticia")))
                .andRespond(withSuccess("/uploads/escudos/escudo.png", MediaType.TEXT_PLAIN));

        assertEquals("/uploads/escudos/escudo.png", new CadastroClient(construtor.build()).uploadEscudo(arquivo));
        verify(entrada).close();
        servidor.verify();
    }

    @Test
    void apresentaFalhaDeLeituraComoMensagemDeUpload() throws IOException {
        UploadedFile arquivo = mock(UploadedFile.class);
        when(arquivo.getInputStream()).thenThrow(new IOException("falha simulada"));
        var erro = assertThrows(RuntimeException.class,
                () -> new CadastroClient(RestClient.create()).uploadEscudo(arquivo));
        assertEquals("Nao foi possivel ler o arquivo do escudo.", erro.getMessage());
    }
}
