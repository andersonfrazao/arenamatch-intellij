package br.com.arenamatch.jsf.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

class CadastroBeanMensagemErroTest {

    @Test
    void mostraARegraDevolvidaPeloServidorSemOStatusHttp() {
        String regra = "O horário deve ter exatamente 2 horas.";

        assertEquals(regra, CadastroBean.mensagemDoErro(resposta(regra)));
    }

    @Test
    void usaAMensagemDaExcecaoQuandoOCorpoVemVazio() {
        RestClientResponseException semCorpo = resposta("  ");

        assertEquals(semCorpo.getMessage(), CadastroBean.mensagemDoErro(semCorpo));
    }

    @Test
    void mantemAMensagemDeOutrasExcecoes() {
        assertEquals("CEP inválido.", CadastroBean.mensagemDoErro(new IllegalArgumentException("CEP inválido.")));
    }

    private static RestClientResponseException resposta(String corpo) {
        return new RestClientResponseException("400 Bad Request: \"" + corpo + "\"",
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpHeaders.EMPTY, corpo.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
