package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.Modalidade;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dia e horário em que o time joga, conforme a agenda do cadastro (tabela agenda). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAgendaTimeDTO implements Serializable {
    private DayOfWeek dia;
    private Modalidade modalidade;
    private Categoria categoria;
    private LocalTime horaInicio;
    private LocalTime horaFim;
}
