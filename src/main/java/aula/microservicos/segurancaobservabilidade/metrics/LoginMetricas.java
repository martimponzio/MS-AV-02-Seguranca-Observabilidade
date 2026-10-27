package aula.microservicos.segurancaobservabilidade.metrics;

import org.springframework.stereotype.Component;

import aula.microservicos.segurancaobservabilidade.repository.UsuarioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(
            MeterRegistry registry,
            UsuarioRepository usuarioRepository) {

        loginsValidos = Counter.builder("logins.validos")
                .description("Quantidade de logins válidos")
                .register(registry);

        loginsInvalidos = Counter.builder("logins.invalidos")
                .description("Quantidade de logins inválidos")
                .register(registry);

        Gauge.builder(
                        "usuarios.bloqueados",
                        usuarioRepository,
                        UsuarioRepository::contarBloqueados)
                .description("Total de usuários bloqueados")
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido() {
        loginsInvalidos.increment();
    }
}
