package br.com.arenamatch.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CadastroValidacaoServiceTest {

    private CadastroValidacaoService service;

    @BeforeEach
    void setUp() {
        service = new CadastroValidacaoService(
                mock(UsuarioRepository.class),
                mock(CpfValidator.class));
    }

    @Test
    void deveOrientarBuscaQuandoCepFoiInformadoSemEnderecoCompleto() {
        CadastroDTO dto = new CadastroDTO();
        dto.setCep("01001-000");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.validarEnderecoCompleto(dto));

        assertEquals(
                "Apos informar o CEP, aperte ou clique na luneta para buscar os dados do endereco "
                        + "ou use a localizacao atual do seu smartphone.",
                exception.getMessage());
    }

    @Test
    void deveRejeitarEnderecoQuandoRegiaoNaoFoiPreenchida() {
        CadastroDTO dto = enderecoCompleto();
        dto.setRegiao(" ");

        assertThrows(RuntimeException.class, () -> service.validarEnderecoCompleto(dto));
    }

    @Test
    void deveAceitarEnderecoCompleto() {
        assertDoesNotThrow(() -> service.validarEnderecoCompleto(enderecoCompleto()));
    }

    private CadastroDTO enderecoCompleto() {
        CadastroDTO dto = new CadastroDTO();
        dto.setCep("01001-000");
        dto.setLogradouro("Praca da Se");
        dto.setBairro("Se");
        dto.setRegiao("Centro");
        dto.setCidade("Sao Paulo");
        dto.setUf("SP");
        return dto;
    }
}
