package br.com.arenamatch.dto;

import java.util.List;

public record PaginaHistoricoGestaoPartidaDTO(
        List<ResumoHistoricoGestaoPartidaDTO> partidas,
        boolean temMais) {
}
