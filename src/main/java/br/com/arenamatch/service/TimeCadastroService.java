package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.GeoDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.integracao.GeoClient;
import br.com.arenamatch.repository.TimeRepository;
import org.springframework.stereotype.Service;

@Service
public class TimeCadastroService {

    private final TimeRepository timeRepository;
    private final GeoClient geoClient;
    private final CadastroValidacaoService cadastroValidacaoService;

    public TimeCadastroService(
            TimeRepository timeRepository,
            GeoClient geoClient,
            CadastroValidacaoService cadastroValidacaoService) {
        this.timeRepository = timeRepository;
        this.geoClient = geoClient;
        this.cadastroValidacaoService = cadastroValidacaoService;
    }

    public Time criarTime(CadastroDTO dto, Usuario responsavel) {
        Time time = new Time();
        aplicarDados(time, dto);
        time.setResponsavel(responsavel);
        return timeRepository.save(time);
    }

    public Time atualizarTime(Time time, CadastroDTO dto) {
        aplicarDados(time, dto);
        return timeRepository.save(time);
    }

    private void aplicarDados(Time time, CadastroDTO dto) {
        time.setNome(dto.getNomeTime());
        time.setCep(dto.getCep());
        time.setLogradouro(dto.getLogradouro());
        time.setBairro(dto.getBairro());
        time.setCidade(dto.getCidade());
        time.setUf(dto.getUf());
        time.setMandoCampo(dto.getMandoCampo());
        time.setNumero(dto.getNumero());
        time.setComplemento(dto.getComplemento());
        time.setRegiao(dto.getRegiao());
        time.setValorTaxa(dto.getValorTaxa());
        preencherCoordenadas(time, dto);
    }

    private void preencherCoordenadas(Time time, CadastroDTO dto) {
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            time.setLatitude(dto.getLatitude());
            time.setLongitude(dto.getLongitude());
            return;
        }

        GeoDTO coords = geoClient.buscarCoordenadas(cadastroValidacaoService.limparMascara(dto.getCep()));
        if (coords != null) {
            time.setLatitude(coords.getLat());
            time.setLongitude(coords.getLon());
        }
    }
}
