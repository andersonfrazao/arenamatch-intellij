package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.repository.UsuarioRepository;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CadastroValidacaoService {

    private final UsuarioRepository usuarioRepository;
    private final CpfValidator cpfValidator;

    @Value("${arenamatch.validation.cpf-enabled:true}")
    private boolean cpfValidationEnabled;

    public CadastroValidacaoService(UsuarioRepository usuarioRepository, CpfValidator cpfValidator) {
        this.usuarioRepository = usuarioRepository;
        this.cpfValidator = cpfValidator;
    }

    public void validarCriacao(CadastroDTO dto) {
        String cpfLimpo = limparMascara(dto.getCpf());
        validarCpf(cpfLimpo);

        validarSenhaObrigatoria(dto.getSenha());
        validarTermosAceitos(dto);
        validarEnderecoCompleto(dto);
        validarNomeCampo(dto);
        validarHorariosMandante(dto);

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Este E-mail já está em uso.");
        }
        if (usuarioRepository.findByCpf(cpfLimpo).isPresent()) {
            throw new RuntimeException("Este CPF já possui cadastro.");
        }
    }

    public void validarCpf(String cpf) {
        if (cpfValidationEnabled && !cpfValidator.isValido(limparMascara(cpf))) {
            throw new RuntimeException("CPF invalido.");
        }
    }

    public void validarAtualizacao(CadastroDTO dto) {
        validarEnderecoCompleto(dto);
        validarNomeCampo(dto);
        validarHorariosMandante(dto);
    }

    private void validarNomeCampo(CadastroDTO dto) {
        if (dto != null && dto.getNomeCampo() != null && dto.getNomeCampo().trim().length() > 120) {
            throw new RuntimeException("O nome do campo deve ter no maximo 120 caracteres.");
        }
    }

    public void validarEnderecoCompleto(CadastroDTO dto) {
        if (dto == null || isVazio(dto.getCep())) {
            throw new RuntimeException("Informe o CEP do endereco do time.");
        }

        if (isVazio(dto.getLogradouro())
                || isVazio(dto.getBairro())
                || isVazio(dto.getRegiao())
                || isVazio(dto.getCidade())
                || isVazio(dto.getUf())) {
            throw new RuntimeException(
                    "Apos informar o CEP, aperte ou clique na luneta para buscar os dados do endereco "
                            + "ou use a localizacao atual do seu smartphone.");
        }
    }

    public void validarSenhaEdicao(String senha) {
        if (senha != null && !senha.trim().isEmpty() && senha.length() < 6) {
            throw new RuntimeException("A senha precisa ter no minimo 6 caracteres.");
        }
    }

    public void validarTermosAceitos(CadastroDTO dto) {
        if (!Boolean.TRUE.equals(dto.getTermosAceitos())) {
            throw new RuntimeException("Você precisa ler e aceitar os Termos de Uso.");
        }
    }

    public void validarHorariosMandante(CadastroDTO dto) {
        if (dto == null || !Boolean.TRUE.equals(dto.getMandoCampo())) {
            return;
        }

        List<DisponibilidadeDTO> disponibilidades = dto.getDisponibilidades();
        if (disponibilidades == null) {
            return;
        }

        for (DisponibilidadeDTO item : disponibilidades) {
            if (item == null) {
                continue;
            }

            try {
                LocalTime inicio = LocalTime.parse(item.getInicio());
                LocalTime fim = LocalTime.parse(item.getFim());
                long minutos = ChronoUnit.MINUTES.between(inicio, fim);

                if (minutos != 120) {
                    throw new RuntimeException("Times mandantes devem cadastrar horarios com exatamente 2 horas.");
                }
            } catch (RuntimeException e) {
                if ("Times mandantes devem cadastrar horarios com exatamente 2 horas.".equals(e.getMessage())) {
                    throw e;
                }
                throw new RuntimeException("Horario invalido na agenda. Use o formato HH:mm.");
            }
        }
    }

    public String limparMascara(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("[^0-9]", "");
    }

    private void validarSenhaObrigatoria(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new RuntimeException("A senha precisa ter no minimo 6 caracteres.");
        }
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
