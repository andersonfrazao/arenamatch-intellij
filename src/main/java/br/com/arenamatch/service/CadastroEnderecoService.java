package br.com.arenamatch.service;

import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.integracao.GeoClient;
import br.com.arenamatch.integracao.ViaCepClient;
import org.springframework.stereotype.Service;

@Service
public class CadastroEnderecoService {
    private final ViaCepClient viaCepClient;
    private final GeoClient geoClient;

    public CadastroEnderecoService(ViaCepClient viaCepClient, GeoClient geoClient) {
        this.viaCepClient = viaCepClient;
        this.geoClient = geoClient;
    }

    public EnderecoDTO buscarPorCep(String cep) {
        String cepLimpo = limparCep(cep);
        if (cepLimpo.length() != 8) {
            throw new RuntimeException("Digite um CEP completo com 8 numeros para realizar a busca.");
        }
        EnderecoDTO endereco = viaCepClient.buscarEndereco(cepLimpo);
        if (endereco == null || endereco.isErro()) {
            throw new RuntimeException("Nao foi possivel encontrar o endereco deste CEP.");
        }
        return endereco;
    }

    public EnderecoDTO buscarPorCoordenadas(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new RuntimeException("Nao foi possivel obter sua localizacao atual.");
        }
        EnderecoDTO enderecoGoogle = geoClient.buscarEnderecoPorCoordenadas(latitude, longitude);
        if (enderecoGoogle == null) {
            throw new RuntimeException("Nao foi possivel converter sua localizacao em endereco.");
        }
        String cep = limparCep(enderecoGoogle.getCep());
        if (cep.length() == 8) {
            EnderecoDTO enderecoViaCep = viaCepClient.buscarEndereco(cep);
            if (enderecoViaCep != null && !enderecoViaCep.isErro()) {
                return enderecoViaCep;
            }
        }
        return enderecoGoogle;
    }

    private String limparCep(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }
}
