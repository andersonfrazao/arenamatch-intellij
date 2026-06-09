package br.com.arenamatch.service;

import br.com.arenamatch.dto.AdminParametroSistemaDTO;
import br.com.arenamatch.dto.AdminParametroSistemaEdicaoDTO;
import br.com.arenamatch.entity.ParametroSistema;
import br.com.arenamatch.repository.ParametroSistemaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminParametroSistemaService {

    private final ParametroSistemaRepository parametroSistemaRepository;
    private final AdminParametroSistemaMapper mapper;

    public AdminParametroSistemaService(ParametroSistemaRepository parametroSistemaRepository,
                                        AdminParametroSistemaMapper mapper) {
        this.parametroSistemaRepository = parametroSistemaRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AdminParametroSistemaDTO> listarParametros() {
        return parametroSistemaRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getChave().compareToIgnoreCase(b.getChave()))
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public AdminParametroSistemaDTO atualizarParametro(AdminParametroSistemaEdicaoDTO dto) {
        if (dto.getChave() == null || dto.getChave().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametro nao informado.");
        }
        if (dto.getValor() == null || dto.getValor().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor do parametro nao pode ficar vazio.");
        }

        ParametroSistema parametro = parametroSistemaRepository.findById(dto.getChave())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parametro nao encontrado."));

        validarValorConhecido(parametro.getChave(), dto.getValor().trim());
        parametro.setValor(dto.getValor().trim());
        return mapper.toDto(parametroSistemaRepository.save(parametro));
    }

    private void validarValorConhecido(String chave, String valor) {
        if (ParametroSistemaService.MIN_DIAS_ANTECEDENCIA_AGENDAMENTO.equals(chave)
                || ParametroSistemaService.MIN_DIAS_ANTECEDENCIA_CANCELAMENTO.equals(chave)
                || ParametroSistemaService.DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO.equals(chave)
                || ParametroSistemaService.DIAS_TRIAL.equals(chave)
                || ParametroSistemaService.DIAS_CONFIRMACAO_AUTOMATICA_PLACAR.equals(chave)
                || ParametroSistemaService.RAIO_MAXIMO_BUSCA_PLANO_BASICO_KM.equals(chave)) {
            try {
                int numero = Integer.parseInt(valor);
                if (numero < 0) {
                    throw new NumberFormatException("negativo");
                }
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este parametro exige um numero inteiro positivo.");
            }
        }
    }
}
