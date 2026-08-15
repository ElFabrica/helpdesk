package helpdesk.api.auth.service;

import helpdesk.api.auth.AuthenticatedUser;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        Jwt jwt = currentJwt();

        return new AuthenticatedUser(
                userId(jwt),
                jwt.getSubject(),
                userRole(jwt)
        );
    }

    @Transactional(readOnly = true)
    public User getAuthenticatedUserEntity() {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();

        return userRepository.findById(authenticatedUser.id())
                .orElseThrow(this::unauthorized);
    }

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            throw unauthorized();
        }

        return jwtAuthentication.getToken();
    }

    private Long userId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value && !value.isBlank()) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException exception) {
                throw unauthorized();
            }
        }

        throw unauthorized();
    }

    private UserRole userRole(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            throw unauthorized();
        }

        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            throw unauthorized();
        }
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado invalido");
    }
}
