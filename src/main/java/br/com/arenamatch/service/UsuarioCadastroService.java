package br.com.arenamatch.service;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioCadastroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParametroSistemaService parametroSistemaService;
    private final CadastroValidacaoService cadastroValidacaoService;
    private final CodigoAtivacaoService codigoAtivacaoService;

    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean emailActivationEnabled;

    public UsuarioCadastroService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ParametroSistemaService parametroSistemaService,
            CadastroValidacaoService cadastroValidacaoService,
            CodigoAtivacaoService codigoAtivacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.parametroSistemaService = parametroSistemaService;
        this.cadastroValidacaoService = cadastroValidacaoService;
        this.codigoAtivacaoService = codigoAtivacaoService;
    }

    public Usuario criarUsuario(CadastroDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNomeResponsavel());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(cadastroValidacaoService.limparMascara(dto.getCpf()));
        usuario.setCelular(cadastroValidacaoService.limparMascara(dto.getCelular()));
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setPerfil(Perfil.REPRESENTANTE);
        usuario.setPlanoAssinatura(PlanoAssinatura.TRIAL);
        usuario.setStatusPagamento(StatusPagamento.TRIAL);
        usuario.setDataInicioAssinatura(LocalDateTime.now());
        usuario.setDataExpiracao(LocalDateTime.now().plusDays(parametroSistemaService.buscarDiasTrial()));
        usuario.setDataAceiteTermos(LocalDateTime.now());

        if (emailActivationEnabled) {
            usuario.setStatusUsuario(StatusUsuario.PENDENTE_ATIVACAO);
            usuario.setCodigoAtivacaoEmail(codigoAtivacaoService.gerarCodigoNumerico());
            usuario.setValidadeCodigoAtivacaoEmail(LocalDateTime.now().plusMinutes(15));
        } else {
            usuario.setStatusUsuario(StatusUsuario.ATIVO);
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarUsuario(Usuario usuario, CadastroDTO dto) {
        usuario.setNome(dto.getNomeResponsavel());
        usuario.setCelular(cadastroValidacaoService.limparMascara(dto.getCelular()));

        cadastroValidacaoService.validarSenhaEdicao(dto.getSenha());
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }
}
