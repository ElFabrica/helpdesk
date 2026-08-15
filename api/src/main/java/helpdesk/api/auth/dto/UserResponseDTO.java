package helpdesk.api.auth.dto;

import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import java.time.Instant;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        UserRole role,
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
