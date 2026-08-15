package helpdesk.api.auth.dto;

import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Usuario retornado pela API sem senha ou hash.")
public record UserResponseDTO(
        @Schema(example = "1")
        Long id,

        @Schema(example = "Maria Silva")
        String name,

        @Schema(example = "maria@example.com")
        String email,

        @Schema(example = "SOLICITANTE")
        UserRole role,

        @Schema(example = "2026-08-15T13:00:00Z")
        Instant createdAt
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
