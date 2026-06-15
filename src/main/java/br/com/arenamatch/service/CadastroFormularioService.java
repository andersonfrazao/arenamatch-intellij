package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.CategoriaDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.enums.Categoria;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CadastroFormularioService {

    private final CadastroValidacaoService cadastroValidacaoService;

    public CadastroFormularioService(CadastroValidacaoService cadastroValidacaoService) {
        this.cadastroValidacaoService = cadastroValidacaoService;
    }

    public void validarAvancoResponsavel(CadastroDTO dto, String confirmarSenha, boolean novoCadastro) {
        boolean novaSenhaInformada = dto.getSenha() != null && !dto.getSenha().trim().isEmpty();

        if (novoCadastro) {
            cadastroValidacaoService.validarCpf(dto.getCpf());
        }

        if (novoCadastro || novaSenhaInformada) {
            if (dto.getSenha() == null || dto.getSenha().length() < 6) {
                throw new RuntimeException("A senha precisa ter no minimo 6 caracteres.");
            }

            if (!dto.getSenha().equals(confirmarSenha)) {
                throw new RuntimeException("Verifique os campos de senha.");
            }
        }
    }

    public void validarAvancoTime(CadastroDTO dto) {
        cadastroValidacaoService.validarEnderecoCompleto(dto);
    }

    public DisponibilidadeDTO criarDisponibilidade(
            CadastroDTO dto,
            List<DisponibilidadeDTO> agenda,
            Categoria categoria,
            String dia,
            String inicio,
            String fim) {
        if (categoria == null) {
            throw new RuntimeException("Selecione a categoria.");
        }

        if (isVazio(dia) || isVazio(inicio) || isVazio(fim)) {
            throw new RuntimeException("Preencha todos os campos do horário.");
        }

        boolean existeConflito = agenda.stream().anyMatch(item ->
                item.getCategoria().getDescricao().equals(categoria.getDescricao())
                        && item.getDiaSemana().equals(dia));
        if (existeConflito) {
            throw new RuntimeException("Você já adicionou a categoria " + categoria.getDescricao()
                    + " para " + dia + ". Escolha outro dia.");
        }

        CadastroDTO dtoValidacao = new CadastroDTO();
        dtoValidacao.setMandoCampo(dto.getMandoCampo());
        DisponibilidadeDTO itemValidacao = montarDisponibilidade(categoria, dia, inicio, fim);
        dtoValidacao.setDisponibilidades(List.of(itemValidacao));
        cadastroValidacaoService.validarHorariosMandante(dtoValidacao);

        return itemValidacao;
    }

    public void validarFinalizacao(CadastroDTO dto, List<DisponibilidadeDTO> agenda, boolean novoCadastro) {
        if (novoCadastro) {
            cadastroValidacaoService.validarTermosAceitos(dto);
        }

        if (agenda == null || agenda.isEmpty()) {
            throw new RuntimeException("Adicione pelo menos um horário na agenda para finalizar.");
        }
    }

    private DisponibilidadeDTO montarDisponibilidade(Categoria categoria, String dia, String inicio, String fim) {
        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO();

        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId((long) categoria.ordinal());
        categoriaDTO.setDescricao(categoria.getDescricao());

        disponibilidade.setCategoria(categoriaDTO);
        disponibilidade.setDiaSemana(dia);
        disponibilidade.setInicio(inicio);
        disponibilidade.setFim(fim);
        return disponibilidade;
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
