package aula.microservicos.segurancaobservabilidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "O username é obrigatório")
        String username,

        @NotBlank(message = "A senha é obrigatória")
        @Size(max = 10, message = "A senha deve possuir no máximo 10 caracteres")
        String senha) {
}
