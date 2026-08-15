package helpdesk.api.auth;

import helpdesk.api.user.User;
import helpdesk.api.user.UserRole;
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
