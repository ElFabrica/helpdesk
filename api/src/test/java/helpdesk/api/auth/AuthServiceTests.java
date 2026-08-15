package helpdesk.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import helpdesk.api.auth.dto.LoginRequestDTO;
import helpdesk.api.auth.dto.RegisterRequestDTO;
import helpdesk.api.auth.dto.TokenResponseDTO;
import helpdesk.api.auth.dto.UserResponseDTO;
import helpdesk.api.auth.service.AuthService;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersRequesterWithEncodedPassword() {
        UserResponseDTO response = authService.register(new RegisterRequestDTO(
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

        assertThatThrownBy(() -> authService.register(new RegisterRequestDTO(
                "Outro Usuario",
                "duplicate-register@example.com",
                "654321"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void logsInWithValidCredentialsAndReturnsJwt() {
        User user = userRepository.save(new User(
                "Maria Login",
                "maria-login@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        TokenResponseDTO response = authService.login(new LoginRequestDTO("maria-login@example.com", "123456"));
        Jwt jwt = jwtDecoder.decode(response.token());

        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(jwt.getSubject()).isEqualTo("maria-login@example.com");
        assertThat(jwt.getClaimAsString("role")).isEqualTo(UserRole.SOLICITANTE.name());
        assertThat(jwt.getClaimAsString("userId")).isEqualTo(user.getId().toString());
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void rejectsInvalidCredentials() {
        userRepository.save(new User(
                "Maria Login",
                "maria-invalid-password@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO(
                "maria-invalid-password@example.com",
                "wrong-password"
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginEndpointReturnsBearerToken() throws Exception {
        userRepository.save(new User(
                "Maria Endpoint",
                "maria-login-endpoint@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria-login-endpoint@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registerEndpointReturnsConflictForDuplicateEmail() throws Exception {
        userRepository.save(new User(
                "Usuario Existente",
                "duplicate-register-endpoint@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Outro Usuario",
                                  "email": "duplicate-register-endpoint@example.com",
                                  "password": "654321"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("E-mail ja cadastrado"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void loginEndpointReturnsUnauthorizedForInvalidCredentials() throws Exception {
        userRepository.save(new User(
                "Maria Endpoint",
                "maria-login-invalid-endpoint@example.com",
                passwordEncoder.encode("123456"),
                UserRole.SOLICITANTE
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria-login-invalid-endpoint@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
