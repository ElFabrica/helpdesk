package helpdesk.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticacao.")
public record LoginRequestDTO(
        @Schema(description = "E-mail cadastrado.", example = "admin@helpdesk.local")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Senha do usuario.", example = "admin123")
        @NotBlank
        String password
) {
}
