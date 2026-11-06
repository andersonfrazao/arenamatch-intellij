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
                    .findFirstByTimeIdAndDiaSemanaAndModalidadeAndCategoriaOrderByHoraInicioAsc(mandante.getId(), diaBanco, dto.getModalidade(), dto.getCategoria())
                    .orElse(null);
        }

        if (agendaMandante == null && dto.getCategoria() == null) {
            agendaMandante = agendaRepository
                    .findFirstByTimeIdAndDiaSemanaAndModalidadeOrderByHoraInicioAsc(mandante.getId(), diaBanco, dto.getModalidade())
                    .orElseThrow(() -> new RuntimeException("O time mandante não possui horário cadastrado para " + diaBanco + "."));
        }

        if (agendaMandante == null) {
            throw new RuntimeException("O mandante nao possui horario para a modalidade e categoria selecionadas.");
        }
        LocalTime horaInicio = LocalTime.parse(agendaMandante.getHoraInicio());
        return dto.getDataHoraPartida().toLocalDate().atTime(horaInicio);
    }
}
