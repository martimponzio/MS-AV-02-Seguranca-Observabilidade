package aula.microservicos.segurancaobservabilidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocaSenhaRequest(
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(max = 10, message = "A senha atual deve possuir no máximo 10 caracteres")
        String atual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(max = 10, message = "A nova senha deve possuir no máximo 10 caracteres")
        String nova) {
}
