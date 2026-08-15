package helpdesk.api.auth;

import helpdesk.api.auth.dto.LoginRequestDTO;
import helpdesk.api.auth.dto.RegisterRequestDTO;
import helpdesk.api.auth.dto.TokenResponseDTO;
import helpdesk.api.auth.dto.UserResponseDTO;
import helpdesk.api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
