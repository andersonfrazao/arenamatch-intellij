package br.com.arenamatch.service;

import br.com.arenamatch.dto.*;
import br.com.arenamatch.entity.*;
import br.com.arenamatch.enums.*;
import br.com.arenamatch.integracao.GeoClient;
import br.com.arenamatch.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModalidadeCadastroTest {
    private final CadastroValidacaoService validacao = new CadastroValidacaoService(mock(UsuarioRepository.class), mock(CpfValidator.class));

    @Test void naoPermiteSubstituirOuRemoverModalidadeMesmoPelaApi() {
        Time time = new Time();
        time.getModalidades().add(Modalidade.FUTEBOL_CAMPO);
        CadastroDTO dto = cadastro(Modalidade.FUTSAL);
        TimeRepository repository = mock(TimeRepository.class);
        TimeCadastroService service = new TimeCadastroService(repository, mock(GeoClient.class), validacao);
        assertThrows(RuntimeException.class, () -> service.atualizarTime(time, dto));
        assertEquals(Set.of(Modalidade.FUTEBOL_CAMPO), time.getModalidades());
        verifyNoInteractions(repository);
    }

    @Test void acrescentaNovaModalidadeSemPerderAAnterior() {
        Time time = new Time();
        time.getModalidades().add(Modalidade.FUTEBOL_CAMPO);
        CadastroDTO dto = cadastro(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL);
        TimeCadastroService service = new TimeCadastroService(mock(TimeRepository.class), mock(GeoClient.class), validacao);
        service.atualizarTime(time, dto);
        assertEquals(Set.of(Modalidade.FUTEBOL_CAMPO, Modalidade.FUTSAL), time.getModalidades());
    }

    @Test void exigeModalidadeECadaHorarioDevePertencerAoTime() {
        assertThrows(RuntimeException.class, () -> validacao.validarModalidades(new CadastroDTO()));
        CadastroDTO dto = cadastro(Modalidade.FUTSAL);
        DisponibilidadeDTO horario = new DisponibilidadeDTO("Sábado", "09:00", "11:00");
        horario.setModalidade(Modalidade.FUT_7);
        dto.setDisponibilidades(List.of(horario));
        assertThrows(RuntimeException.class, () -> validacao.validarModalidades(dto));
        horario.setModalidade(Modalidade.FUTSAL);
        assertDoesNotThrow(() -> validacao.validarModalidades(dto));
    }

    @Test void permiteMesmaCategoriaEDiaEmModalidadesDiferentes() {
        CadastroDTO dto = cadastro(Modalidade.FUT_7, Modalidade.FUTSAL);
        CadastroFormularioService formulario = new CadastroFormularioService(validacao);
        DisponibilidadeDTO fut7 = formulario.criarDisponibilidade(dto, List.of(), Categoria.ESPORTE, Modalidade.FUT_7, "Sábado", "09:00", "11:00");
        assertDoesNotThrow(() -> formulario.criarDisponibilidade(dto, List.of(fut7), Categoria.ESPORTE, Modalidade.FUTSAL, "Sábado", "09:00", "11:00"));
        assertThrows(RuntimeException.class, () -> formulario.criarDisponibilidade(dto, List.of(fut7), Categoria.ESPORTE, Modalidade.FUT_7, "Sábado", "09:00", "11:00"));
    }

    @Test void adicionaHorarioDeNovaModalidadeSemEtapaDeCadastroSeparada() {
        CadastroDTO dto = cadastro(Modalidade.FUTEBOL_CAMPO);
        CadastroFormularioService formulario = new CadastroFormularioService(validacao);
        DisponibilidadeDTO horario = formulario.criarDisponibilidade(dto, List.of(), Categoria.VETERANO_35, Modalidade.FUTSAL, "Sábado", "09:00", "11:00");
        assertEquals(Modalidade.FUTSAL, horario.getModalidade());
        assertEquals(Categoria.VETERANO_35.getDescricao(), horario.getCategoria().getDescricao());
    }

    @Test void modalidadeECategoriaSaoObrigatoriasAoAdicionarHorario() {
        CadastroDTO dto = cadastro();
        CadastroFormularioService formulario = new CadastroFormularioService(validacao);
        assertThrows(RuntimeException.class, () -> formulario.criarDisponibilidade(dto, List.of(), Categoria.ESPORTE, null, "Sábado", "09:00", "11:00"));
        assertThrows(RuntimeException.class, () -> formulario.criarDisponibilidade(dto, List.of(), null, Modalidade.FUTSAL, "Sábado", "09:00", "11:00"));
    }

    @Test void edicaoRecuperaModalidadesSemPerderDisponibilidades() {
        Time time = new Time(); time.setId(1L); time.getModalidades().addAll(Set.of(Modalidade.FUT_7, Modalidade.FUTSAL));
        CadastroAgendaService agendas = mock(CadastroAgendaService.class);
        when(agendas.buscarDisponibilidades(1L)).thenReturn(List.of(new DisponibilidadeDTO("Sábado", "09:00", "11:00")));
        CadastroDTO dto = new CadastroMapper(agendas).toDTO(new Usuario(), time);
        assertEquals(time.getModalidades(), new HashSet<>(dto.getModalidades()));
        assertEquals(1, dto.getDisponibilidades().size());
    }

    private CadastroDTO cadastro(Modalidade... modalidades) {
        CadastroDTO dto = new CadastroDTO(); dto.setModalidades(new ArrayList<>(List.of(modalidades)));
        dto.setLatitude(-23.5); dto.setLongitude(-46.6); dto.setMandoCampo(false);
        return dto;
    }
}
