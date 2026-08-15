package helpdesk.api.auth.service;

import helpdesk.api.auth.dto.LoginRequestDTO;
import helpdesk.api.auth.dto.RegisterRequestDTO;
import helpdesk.api.auth.dto.TokenResponseDTO;
import helpdesk.api.auth.dto.UserResponseDTO;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                UserRole.SOLICITANTE
        );

        return UserResponseDTO.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return TokenResponseDTO.bearer(jwtTokenService.generateToken(user));
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
    }
}
