package helpdesk.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import helpdesk.api.user.User;
import helpdesk.api.user.UserRepository;
import helpdesk.api.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registersRequesterWithEncodedPassword() {
        UserResponse response = authService.register(new RegisterRequest(
                "Maria Silva",
                "maria-register@example.com",
                "123456"
        ));

        User savedUser = userRepository.findByEmail("maria-register@example.com").orElseThrow();

        assertThat(response.id()).isEqualTo(savedUser.getId());
        assertThat(response.name()).isEqualTo("Maria Silva");
        assertThat(response.email()).isEqualTo("maria-register@example.com");
        assertThat(response.role()).isEqualTo(UserRole.SOLICITANTE);
        assertThat(savedUser.getRole()).isEqualTo(UserRole.SOLICITANTE);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("123456");
        assertThat(passwordEncoder.matches("123456", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.save(new User(
                "Usuario Existente",
                "duplicate-register@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "Outro Usuario",
                "duplicate-register@example.com",
                "654321"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
