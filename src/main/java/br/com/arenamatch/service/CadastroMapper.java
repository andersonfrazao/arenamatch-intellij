package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CadastroMapper {

    private final CadastroAgendaService cadastroAgendaService;

    public CadastroMapper(CadastroAgendaService cadastroAgendaService) {
        this.cadastroAgendaService = cadastroAgendaService;
    }

    public CadastroDTO toDTO(Usuario usuario, Time time) {
        CadastroDTO dto = new CadastroDTO();
        dto.setNomeResponsavel(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(usuario.getCpf());
        dto.setCelular(usuario.getCelular());

        dto.setNomeTime(time.getNome());
        dto.setCep(time.getCep());
        dto.setLogradouro(time.getLogradouro());
        dto.setBairro(time.getBairro());
        dto.setCidade(time.getCidade());
        dto.setUf(time.getUf());
        dto.setEscudo(time.getEscudo());
        dto.setNumero(time.getNumero());
        dto.setComplemento(time.getComplemento());
        dto.setRegiao(time.getRegiao());
        dto.setValorTaxa(time.getValorTaxa());
        dto.setMandoCampo(time.isMandoCampo());
        dto.setLatitude(time.getLatitude());
        dto.setLongitude(time.getLongitude());
        dto.setDisponibilidades(cadastroAgendaService.buscarDisponibilidades(time.getId()));
        return dto;
    }
}
