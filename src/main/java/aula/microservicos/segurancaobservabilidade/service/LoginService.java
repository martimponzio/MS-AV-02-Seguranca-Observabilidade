package aula.microservicos.segurancaobservabilidade.service;

import org.springframework.stereotype.Service;

import aula.microservicos.segurancaobservabilidade.dto.LoginRequest;
import aula.microservicos.segurancaobservabilidade.metrics.LoginMetricas;
import aula.microservicos.segurancaobservabilidade.model.Usuario;
import aula.microservicos.segurancaobservabilidade.repository.UsuarioRepository;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final LoginMetricas loginMetricas;

    public LoginService(
            UsuarioRepository usuarioRepository,
            LoginMetricas loginMetricas) {
        this.usuarioRepository = usuarioRepository;
        this.loginMetricas = loginMetricas;
    }

    public LoginResultado autenticar(LoginRequest login) {

        Usuario usuario = usuarioRepository
                .buscar(login.username())
                .orElse(null);

        if (usuario == null) {
            loginMetricas.loginInvalido();
            return LoginResultado.INVALIDO;
        }

        if (usuarioRepository.estaBloqueado(login.username())) {
            loginMetricas.loginInvalido();
            return LoginResultado.BLOQUEADO;
        }

        if (usuario.senha().equals(login.senha())) {
            usuarioRepository.resetarTentativas(login.username());
            loginMetricas.loginValido();
            return LoginResultado.VALIDO;
        }

        usuarioRepository.registrarFalha(login.username());
        loginMetricas.loginInvalido();

        if (usuarioRepository.estaBloqueado(login.username())) {
            return LoginResultado.BLOQUEADO;
        }

        return LoginResultado.INVALIDO;
    }
}
