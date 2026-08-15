package helpdesk.api.auth;

import helpdesk.api.user.UserRole;

public record AuthenticatedUser(Long id, String email, UserRole role) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isSolicitante() {
        return role == UserRole.SOLICITANTE;
    }

    public boolean owns(Long requesterId) {
        return id != null && id.equals(requesterId);
    }
}
