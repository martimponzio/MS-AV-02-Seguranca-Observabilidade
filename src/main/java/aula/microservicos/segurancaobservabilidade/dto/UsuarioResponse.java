package aula.microservicos.segurancaobservabilidade.dto;

public record UsuarioResponse(
        String username,
        boolean bloqueado) {
}
