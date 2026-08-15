package helpdesk.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import helpdesk.api.ApiApplication;
import helpdesk.api.auth.service.AuthenticatedUserService;
import helpdesk.api.auth.service.JwtTokenService;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = {ApiApplication.class, AuthorizationSecurityTests.ProtectedTestController.class})
@AutoConfigureMockMvc
@Transactional
class AuthorizationSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/security-test/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenAccessesProtectedRoute() throws Exception {
        User user = saveUser("Maria Token", "maria-token@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(get("/api/security-test/protected")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("maria-token@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.SOLICITANTE.name()));
    }

    @Test
    void insufficientProfileReturnsForbidden() throws Exception {
        User requester = saveUser("Joao Solicitante", "joao-solicitante@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(get("/api/security-test/admin")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminProfileAccessesAdminRoute() throws Exception {
        User admin = saveUser("Ana Admin", "ana-admin@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/security-test/admin")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(UserRole.ADMIN.name()));
    }

    private User saveUser(String name, String email, UserRole role) {
        return userRepository.save(new User(name, email, "password-hash", role));
    }

    @RestController
    static class ProtectedTestController {

        private final AuthenticatedUserService authenticatedUserService;

        ProtectedTestController(AuthenticatedUserService authenticatedUserService) {
            this.authenticatedUserService = authenticatedUserService;
        }

        @GetMapping("/api/security-test/protected")
        Map<String, Object> protectedRoute() {
            AuthenticatedUser user = authenticatedUserService.getAuthenticatedUser();

            return Map.of(
                    "id", user.id(),
                    "email", user.email(),
                    "role", user.role().name()
            );
        }

        @GetMapping("/api/security-test/admin")
        @PreAuthorize("hasRole('ADMIN')")
        Map<String, Object> adminRoute() {
            AuthenticatedUser user = authenticatedUserService.getAuthenticatedUser();

            return Map.of("role", user.role().name());
        }
    }
}
