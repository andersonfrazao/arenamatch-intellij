package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;
    private final PartidaRepository partidaRepository;
    private final CadastroValidacaoService cadastroValidacaoService;
    private final UsuarioCadastroService usuarioCadastroService;
    private final TimeCadastroService timeCadastroService;
    private final CadastroAgendaService cadastroAgendaService;
    private final CadastroMapper cadastroMapper;
    private final EmailService emailService;

    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean emailActivationEnabled;

    public CadastroService(
            UsuarioRepository usuarioRepository,
            TimeRepository timeRepository,
            PartidaRepository partidaRepository,
            CadastroValidacaoService cadastroValidacaoService,
            UsuarioCadastroService usuarioCadastroService,
            TimeCadastroService timeCadastroService,
            CadastroAgendaService cadastroAgendaService,
            CadastroMapper cadastroMapper,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.timeRepository = timeRepository;
        this.partidaRepository = partidaRepository;
        this.cadastroValidacaoService = cadastroValidacaoService;
        this.usuarioCadastroService = usuarioCadastroService;
        this.timeCadastroService = timeCadastroService;
        this.cadastroAgendaService = cadastroAgendaService;
        this.cadastroMapper = cadastroMapper;
        this.emailService = emailService;
    }

    @Transactional
    public void criarConta(CadastroDTO dto) {
        try {
            cadastroValidacaoService.validarCriacao(dto);

            Usuario usuario = usuarioCadastroService.criarUsuario(dto);
            Time time = timeCadastroService.criarTime(dto, usuario);
            cadastroAgendaService.salvarAgenda(time, dto.getDisponibilidades());

            if (emailActivationEnabled) {
                emailService.enviarCodigoAtivacao(usuario.getEmail(), usuario.getCodigoAtivacaoEmail());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar a conta " + e.getMessage());
        }
    }

    @Transactional
    public void atualizarConta(Long idUsuarioLogado, CadastroDTO dto) {
        try {
            Usuario usuario = usuarioRepository.findById(idUsuarioLogado)
                    .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

            Time time = timeRepository.findByResponsavelId(idUsuarioLogado)
                    .orElseThrow(() -> new RuntimeException("Time nao encontrado para este usuario."));

            validarAlteracaoEnderecoPermitida(time, dto);
            dto.setMandoCampo(time.isMandoCampo());
            validarEmailUnicoNaAtualizacao(usuario, dto);
            cadastroValidacaoService.validarAtualizacao(dto);

            usuarioCadastroService.atualizarUsuario(usuario, dto);
            timeCadastroService.atualizarTime(time, dto);
            cadastroAgendaService.substituirAgenda(time, dto.getDisponibilidades());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar a conta: " + e.getMessage());
        }
    }

    @Transactional
    public void desativarContaAutenticada() {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                : null;

        if (email == null || email.isBlank() || "null".equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado."));

        usuario.setStatusUsuario(br.com.arenamatch.enums.StatusUsuario.INATIVO);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public CadastroDTO buscarDadosParaEdicao(Long idUsuarioLogado) {
        Usuario usuario = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        Time time = timeRepository.findByResponsavelId(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Time nao encontrado."));

        return cadastroMapper.toDTO(usuario, time);
    }

    private void validarAlteracaoEnderecoPermitida(Time time, CadastroDTO dto) {
        if (enderecoAlterado(time, dto) && partidaRepository.existemJogosAgendadosFuturos(time.getId())) {
            throw new RuntimeException("Nao e possivel alterar o endereco, pois existem jogos agendados nesse endereco. Primeiro realize ou cancele os jogos para poder alterar o endereco!");
        }
    }

    private boolean enderecoAlterado(Time time, CadastroDTO dto) {
        return diferenteCep(time.getCep(), dto.getCep())
                || diferente(time.getLogradouro(), dto.getLogradouro())
                || diferente(time.getNumero(), dto.getNumero())
                || diferente(time.getComplemento(), dto.getComplemento())
                || diferente(time.getBairro(), dto.getBairro())
                || diferente(time.getRegiao(), dto.getRegiao())
                || diferente(time.getCidade(), dto.getCidade())
                || diferente(time.getUf(), dto.getUf());
    }

    private boolean diferente(String atual, String novo) {
        return !normalizar(atual).equals(normalizar(novo));
    }

    private boolean diferenteCep(String atual, String novo) {
        return !normalizar(cadastroValidacaoService.limparMascara(atual))
                .equals(normalizar(cadastroValidacaoService.limparMascara(novo)));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private void validarEmailUnicoNaAtualizacao(Usuario usuario, CadastroDTO dto) {
        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Este E-mail ja esta em uso por outra conta.");
            }
            usuario.setEmail(dto.getEmail());
        }
    }
}
