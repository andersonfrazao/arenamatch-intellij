package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            Time time = timeRepository.findByResponsavelId(idUsuarioLogado)
                    .orElseThrow(() -> new RuntimeException("Time não encontrado para este usuário."));

            validarEdicaoPermitida(time);
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
    public void desativarConta(Long idUsuarioLogado) {
        Usuario usuario = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        usuario.setStatusUsuario(br.com.arenamatch.enums.StatusUsuario.INATIVO);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public CadastroDTO buscarDadosParaEdicao(Long idUsuarioLogado) {
        Usuario usuario = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Time time = timeRepository.findByResponsavelId(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Time não encontrado."));

        return cadastroMapper.toDTO(usuario, time);
    }

    private void validarEdicaoPermitida(Time time) {
        boolean temJogoFuturo = partidaRepository.existemJogosFuturosPendentesOuAgendados(time.getId());
        if (temJogoFuturo) {
            throw new RuntimeException("Não é permitido alterar os dados cadastrais. Você possui jogos agendados ou convites pendentes futuros. Cancele-os na sua Agenda primeiro.");
        }
    }

    private void validarEmailUnicoNaAtualizacao(Usuario usuario, CadastroDTO dto) {
        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Este E-mail já está em uso por outra conta.");
            }
            usuario.setEmail(dto.getEmail());
        }
    }
}
