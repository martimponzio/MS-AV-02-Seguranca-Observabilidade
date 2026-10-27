package aula.microservicos.segurancaobservabilidade.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS-AV-02 - Segurança e Observabilidade",
                version = "1.0",
                description = "API RESTFul para login, usuários, bloqueio, métricas e observabilidade"))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic")
public class OpenApiConfig {
}
