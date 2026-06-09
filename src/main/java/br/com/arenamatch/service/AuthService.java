package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS_CODIGO_ADMIN = 5;
    private static final int MINUTOS_VALIDADE_CODIGO_ADMIN = 10;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UsuarioRepository repository;

    @Autowired 
    private PasswordEncoder passwordEncoder; // Injetar
    
    @Autowired
    private TimeRepository timeRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private AssinaturaService assinaturaService;

    @Transactional
    public UsuarioDTO autenticar(LoginDTO login) {
        Usuario usuario = repository.findByEmail(login.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail nao encontrado."));

        if (!senhaValida(login.getSenha(), usuario)) {
            throw new RuntimeException("Senha incorreta");
        }

        if (StatusUsuario.PENDENTE_ATIVACAO.equals(usuario.getStatusUsuario())) {
            throw new RuntimeException("Conta pendente de ativacao. Ative sua conta com o codigo enviado para seu e-mail.");
        }

        if (StatusUsuario.INATIVO.equals(usuario.getStatusUsuario())) {
            throw new RuntimeException("Sua conta foi desativada. Para solicitar a reativacao, envie um email para arenamatch.app@gmail.com com o assunto \"Ativacao\" informando seu cpf e nome do time");
        }

        if (StatusUsuario.BANIDO.equals(usuario.getStatusUsuario())) {
            throw new RuntimeException("Esta conta nao pode acessar o sistema. Entre em contato com o suporte.");
        }
        
		/*
		 * if (!usuario.getSenha().equals(login.getSenha())) { // Lembre-se: em produção
		 * use BCrypt throw new RuntimeException("Senha incorreta"); }else if
		 * (!passwordEncoder.matches(login.getSenha(), usuario.getSenha())) { //
		 * VALIDAÇÃO COM BCRYPT throw new RuntimeException("Senha incorreta"); }
		 */

        usuario = assinaturaService.atualizarTrialExpirado(usuario);

        return toUsuarioDTO(usuario);
    }

    @Transactional
    public String iniciarAcessoAdmin(String email) {
        Usuario usuario = buscarAdminPorEmail(email);

        if (usuario.getTokenDesafioAdmin() != null
                && usuario.getDataEnvioCodigoAdmin() != null
                && LocalDateTime.now().isBefore(usuario.getDataEnvioCodigoAdmin().plusMinutes(1))) {
            return usuario.getTokenDesafioAdmin();
        }

        String desafio = UUID.randomUUID().toString();
        gerarCodigoAcessoAdmin(usuario, desafio);
        return desafio;
    }

    @Transactional
    public void reenviarCodigoAcessoAdmin(String desafio) {
        Usuario usuario = buscarAdminPorDesafio(desafio);

        if (usuario.getDataEnvioCodigoAdmin() != null
                && LocalDateTime.now().isBefore(usuario.getDataEnvioCodigoAdmin().plusMinutes(1))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Aguarde 1 minuto antes de solicitar um novo codigo.");
        }

        gerarCodigoAcessoAdmin(usuario, desafio);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public UsuarioDTO confirmarAcessoAdmin(String desafio, String codigo) {
        Usuario usuario = buscarAdminPorDesafio(desafio);

        if (usuario.getValidadeCodigoAcessoAdmin() == null
                || LocalDateTime.now().isAfter(usuario.getValidadeCodigoAcessoAdmin())) {
            limparDesafioAdmin(usuario);
            usuarioRepositorySave(usuario);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O codigo expirou. Volte ao login e informe sua senha novamente.");
        }

        int tentativas = usuario.getTentativasCodigoAdmin() != null
                ? usuario.getTentativasCodigoAdmin()
                : 0;

        if (tentativas >= MAX_TENTATIVAS_CODIGO_ADMIN) {
            limparDesafioAdmin(usuario);
            usuarioRepositorySave(usuario);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Limite de tentativas excedido. Volte ao login e tente novamente.");
        }

        boolean codigoValido = codigo != null
                && usuario.getCodigoAcessoAdmin() != null
                && passwordEncoder.matches(codigo.trim(), usuario.getCodigoAcessoAdmin());

        if (!codigoValido) {
            usuario.setTentativasCodigoAdmin(tentativas + 1);
            if (usuario.getTentativasCodigoAdmin() >= MAX_TENTATIVAS_CODIGO_ADMIN) {
                limparDesafioAdmin(usuario);
                usuarioRepositorySave(usuario);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Limite de tentativas excedido. Volte ao login e tente novamente.");
            }
            usuarioRepositorySave(usuario);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Codigo invalido. Verifique o codigo enviado por e-mail.");
        }

        limparDesafioAdmin(usuario);
        usuarioRepositorySave(usuario);
        return toUsuarioDTO(usuario);
    }

    private boolean senhaValida(String senhaInformada, Usuario usuario) {
        if (senhaInformada == null || usuario.getSenha() == null) {
            return false;
        }

        if (usuario.getSenha().startsWith("$2")) {
            return passwordEncoder.matches(senhaInformada, usuario.getSenha());
        }

        if (!usuario.getSenha().equals(senhaInformada)) {
            return false;
        }

        usuario.setSenha(passwordEncoder.encode(senhaInformada));
        repository.save(usuario);
        return true;
    }

    public String mascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }

        String[] partes = email.split("@", 2);
        String nome = partes[0];
        String inicio = nome.substring(0, Math.min(2, nome.length()));
        return inicio + "***@" + partes[1];
    }

    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setPerfil(usuario.getPerfil());
        
        // Verifica Trial
        dto.setPlanoAssinatura(usuario.getPlanoAssinatura());
        dto.setStatusPagamento(usuario.getStatusPagamento());
        dto.setDataExpiracao(usuario.getDataExpiracao());
        
        dto.setExpirado(!assinaturaService.temAcessoCompleto(usuario));
        
        Time timeDoUsuario = timeRepository.findByResponsavelId(usuario.getId()).orElse(null);

        if (timeDoUsuario != null) {
            dto.setIdTime(timeDoUsuario.getId());
        }
        
        return dto;
    }

    private Usuario buscarAdminPorEmail(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));

        if (!Perfil.ADMIN.equals(usuario.getPerfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso nao autorizado.");
        }

        return usuario;
    }

    private Usuario buscarAdminPorDesafio(String desafio) {
        if (desafio == null || desafio.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Desafio de acesso nao informado.");
        }

        Usuario usuario = repository.findByTokenDesafioAdmin(desafio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Desafio de acesso invalido ou ja utilizado."));

        if (!Perfil.ADMIN.equals(usuario.getPerfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso nao autorizado.");
        }

        return usuario;
    }

    private void gerarCodigoAcessoAdmin(Usuario usuario, String desafio) {
        String codigo = String.format("%05d", secureRandom.nextInt(100000));

        usuario.setCodigoAcessoAdmin(passwordEncoder.encode(codigo));
        usuario.setValidadeCodigoAcessoAdmin(LocalDateTime.now().plusMinutes(MINUTOS_VALIDADE_CODIGO_ADMIN));
        usuario.setTokenDesafioAdmin(desafio);
        usuario.setTentativasCodigoAdmin(0);
        usuario.setDataEnvioCodigoAdmin(LocalDateTime.now());
        usuarioRepositorySave(usuario);

        emailService.enviarCodigoAcessoAdmin(usuario.getEmail(), codigo);
    }

    private void limparDesafioAdmin(Usuario usuario) {
        usuario.setCodigoAcessoAdmin(null);
        usuario.setValidadeCodigoAcessoAdmin(null);
        usuario.setTokenDesafioAdmin(null);
        usuario.setTentativasCodigoAdmin(0);
        usuario.setDataEnvioCodigoAdmin(null);
    }

    private void usuarioRepositorySave(Usuario usuario) {
        repository.save(usuario);
    }

    public void ativarConta(String email, String codigoInformado) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail nao encontrado."));

        if (!StatusUsuario.PENDENTE_ATIVACAO.equals(usuario.getStatusUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta ja esta ativa.");
        }

        validarCodigoAtivacao(usuario, codigoInformado);
    }

    private void validarCodigoAtivacao(Usuario usuario, String codigoInformado) {
        if (codigoInformado == null || codigoInformado.trim().isEmpty()) {
            throw new RuntimeException("Informe o codigo de ativacao enviado para seu e-mail.");
        }

        if (usuario.getCodigoAtivacaoEmail() == null || !usuario.getCodigoAtivacaoEmail().equals(codigoInformado.trim())) {
            throw new RuntimeException("Codigo de ativacao invalido.");
        }

        if (usuario.getValidadeCodigoAtivacaoEmail() == null
                || LocalDateTime.now().isAfter(usuario.getValidadeCodigoAtivacaoEmail())) {
            throw new RuntimeException("O codigo de ativacao expirou. Solicite um novo codigo.");
        }

        usuario.setStatusUsuario(StatusUsuario.ATIVO);
        usuario.setCodigoAtivacaoEmail(null);
        usuario.setValidadeCodigoAtivacaoEmail(null);
        repository.save(usuario);
    }
    
    // ... injetar UsuarioRepository e PasswordEncoder se já não estiverem injetados

    public void solicitarCodigoRecuperacao(String email) {
        Usuario usuario = repository.findByEmail(email)
                // Se não achar, lança o Status 404 (Not Found)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        String codigo = String.format("%05d", new java.util.Random().nextInt(100000));
        
        usuario.setCodigoRecuperacao(codigo);
        usuario.setValidadeCodigoRecuperacao(LocalDateTime.now().plusMinutes(15));
        repository.save(usuario);

        emailService.enviarCodigoRecuperacao(email, codigo); // Se falhar aqui, o Spring gera um 500 automaticamente
    }

    public void solicitarCodigoAtivacao(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail nao encontrado."));

        if (!StatusUsuario.PENDENTE_ATIVACAO.equals(usuario.getStatusUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta ja esta ativa.");
        }

        String codigo = String.format("%05d", new java.security.SecureRandom().nextInt(100000));

        usuario.setCodigoAtivacaoEmail(codigo);
        usuario.setValidadeCodigoAtivacaoEmail(LocalDateTime.now().plusMinutes(15));
        repository.save(usuario);

        emailService.enviarCodigoAtivacao(email, codigo);
    }

    public void redefinirSenha(String email, String codigo, String novaSenha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(codigo)) {
            // Se o código não bater, lança Status 400 (Bad Request)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido.");
        }

        if (LocalDateTime.now().isAfter(usuario.getValidadeCodigoRecuperacao())) {
            // Se expirou, também é Status 400 (Bad Request)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código expirou.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setCodigoRecuperacao(null);
        usuario.setValidadeCodigoRecuperacao(null);
        repository.save(usuario);
    }
}
