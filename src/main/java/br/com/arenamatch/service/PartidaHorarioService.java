package br.com.arenamatch.service;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.AgendaRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class PartidaHorarioService {

    private final AgendaRepository agendaRepository;

    public PartidaHorarioService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public String traduzirDia(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terça";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    public LocalDateTime definirDataHoraPeloMandante(Time mandante, String diaBanco, DesafioDTO dto) {
        Agenda agendaMandante = null;
        if (dto.getCategoria() != null) {
            agendaMandante = agendaRepository
                    .findFirstByTimeIdAndDiaSemanaAndCategoriaOrderByHoraInicioAsc(mandante.getId(), diaBanco, dto.getCategoria())
                    .orElse(null);
        }

        if (agendaMandante == null) {
            agendaMandante = agendaRepository
                    .findFirstByTimeIdAndDiaSemanaOrderByHoraInicioAsc(mandante.getId(), diaBanco)
                    .orElseThrow(() -> new RuntimeException("O time mandante não possui horário cadastrado para " + diaBanco + "."));
        }

        LocalTime horaInicio = LocalTime.parse(agendaMandante.getHoraInicio());
        return dto.getDataHoraPartida().toLocalDate().atTime(horaInicio);
    }
}
