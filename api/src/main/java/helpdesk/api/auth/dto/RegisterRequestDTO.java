package helpdesk.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para cadastro de usuario solicitante.")
public record RegisterRequestDTO(
        @Schema(description = "Nome completo do usuario.", example = "Maria Silva")
        @NotBlank String name,

        @Schema(description = "E-mail unico usado para login.", example = "maria@example.com")
        @Email @NotBlank String email,

        @Schema(description = "Senha em texto puro enviada apenas no cadastro.", example = "123456")
        @NotBlank String password
) {
}
