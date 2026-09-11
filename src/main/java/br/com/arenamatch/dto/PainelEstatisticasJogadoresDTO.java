package br.com.arenamatch.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PainelEstatisticasJogadoresDTO {
    private List<EstatisticaJogadorDTO> jogadores = new ArrayList<>();
    private List<EstatisticaJogadorDTO> artilheiros = new ArrayList<>();
    private List<EstatisticaJogadorDTO> disciplina = new ArrayList<>();
}
