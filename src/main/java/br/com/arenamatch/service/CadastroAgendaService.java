package br.com.arenamatch.service;

import br.com.arenamatch.dto.CategoriaDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.repository.AgendaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroAgendaService {

    private final AgendaRepository agendaRepository;

    public CadastroAgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    @Transactional
    public void substituirAgenda(Time time, List<DisponibilidadeDTO> disponibilidades) {
        agendaRepository.deleteByTimeId(time.getId());
        salvarAgenda(time, disponibilidades);
    }

    @Transactional
    public void salvarAgenda(Time time, List<DisponibilidadeDTO> disponibilidades) {
        if (disponibilidades == null || disponibilidades.isEmpty()) {
            return;
        }

        for (DisponibilidadeDTO item : disponibilidades) {
            if (item.getCategoria() == null || item.getCategoria().getDescricao() == null) {
                throw new RuntimeException("A categoria do horário de " + item.getDiaSemana()
                        + " está vazia ou inválida. Por favor, remova o horário e adicione novamente.");
            }

            Agenda agenda = new Agenda();
            agenda.setTime(time);
            agenda.setDiaSemana(item.getDiaSemana());
            agenda.setHoraInicio(item.getInicio());
            agenda.setHoraFim(item.getFim());
            agenda.setCategoria(converterCategoria(item.getCategoria()));

            agendaRepository.save(agenda);
        }
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeDTO> buscarDisponibilidades(Long timeId) {
        List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
        for (Agenda agenda : agendaRepository.findByTimeId(timeId)) {
            DisponibilidadeDTO dto = new DisponibilidadeDTO();
            dto.setDiaSemana(agenda.getDiaSemana());
            dto.setInicio(agenda.getHoraInicio());
            dto.setFim(agenda.getHoraFim());

            if (agenda.getCategoria() != null) {
                CategoriaDTO categoriaDTO = new CategoriaDTO();
                categoriaDTO.setId((long) agenda.getCategoria().ordinal());
                categoriaDTO.setDescricao(agenda.getCategoria().getDescricao());
                dto.setCategoria(categoriaDTO);
            }

            disponibilidades.add(dto);
        }
        return disponibilidades;
    }

    public Categoria converterCategoria(CategoriaDTO categoriaDTO) {
        if (categoriaDTO == null) {
            throw new RuntimeException("Categoria vazia ou inválida.");
        }

        if (categoriaDTO.getId() != null) {
            int ordinal = categoriaDTO.getId().intValue();
            Categoria[] categorias = Categoria.values();
            if (ordinal >= 0 && ordinal < categorias.length) {
                return categorias[ordinal];
            }
        }

        if (categoriaDTO.getDescricao() != null) {
            for (Categoria categoria : Categoria.values()) {
                if (categoria.getDescricao().equalsIgnoreCase(categoriaDTO.getDescricao().trim())) {
                    return categoria;
                }
            }
        }

        throw new RuntimeException("Categoria inválida: " + categoriaDTO.getDescricao());
    }
}
