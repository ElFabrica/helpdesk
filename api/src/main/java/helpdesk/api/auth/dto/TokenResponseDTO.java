package helpdesk.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token emitido apos login.")
public record TokenResponseDTO(String token, String type) {

    public static TokenResponseDTO bearer(String token) {
        return new TokenResponseDTO(token, "Bearer");
    }
}
