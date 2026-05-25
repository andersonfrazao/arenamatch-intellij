package br.com.arenamatch.service;

import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.enums.PlanoAssinatura;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class BuscaTelaService {

    private final ParametroSistemaService parametroSistemaService;

    public BuscaTelaService(ParametroSistemaService parametroSistemaService) {
        this.parametroSistemaService = parametroSistemaService;
    }

    public void validarFiltro(FiltroBuscaDTO filtro) {
        if (filtro.getDataJogo() == null) {
            throw new RuntimeException("Selecione uma data para realizar a busca.");
        }

        if (filtro.getDataJogo().isBefore(LocalDate.now())) {
            throw new RuntimeException("A data da busca não pode ser inferior à data atual.");
        }

        parametroSistemaService.validarDataMinimaAgendamento(filtro.getDataJogo());
    }

    public void aplicarLimiteRaioBasico(FiltroBuscaDTO filtro, UsuarioDTO usuario) {
        if (isPlanoBasico(usuario)
                && (filtro.getRaioKm() == null || filtro.getRaioKm() > getRaioMaximoPlanoBasicoKm())) {
            filtro.setRaioKm(getRaioMaximoPlanoBasicoKm());
        }
    }

    public boolean isPlanoBasico(UsuarioDTO usuario) {
        return usuario != null && usuario.getPlanoAssinatura() == PlanoAssinatura.BASICO;
    }

    public int getRaioMaximoPlanoBasicoKm() {
        return parametroSistemaService.buscarRaioMaximoBuscaPlanoBasicoKm();
    }
}
