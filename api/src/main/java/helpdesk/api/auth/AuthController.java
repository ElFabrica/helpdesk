package helpdesk.api.auth;

import helpdesk.api.auth.dto.LoginRequestDTO;
import helpdesk.api.auth.dto.RegisterRequestDTO;
import helpdesk.api.auth.dto.TokenResponseDTO;
import helpdesk.api.auth.dto.UserResponseDTO;
import helpdesk.api.auth.service.AuthService;
import helpdesk.api.error.ApiErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "Cadastro de solicitantes e login com JWT.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar solicitante", description = "Cria um usuario com perfil SOLICITANTE.")
    @ApiResponse(responseCode = "201", description = "Usuario cadastrado")
    @ApiResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "E-mail ja cadastrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))
    )
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Valida credenciais e retorna token JWT Bearer.")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Credenciais invalidas",
            content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))
    )
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
