package helpdesk.api.auth;

import helpdesk.api.user.User;
import helpdesk.api.user.UserRole;
import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
