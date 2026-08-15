package helpdesk.api.error;

import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ApiErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " " + errorMessage(error))
                .collect(Collectors.joining("; ", "Dados invalidos: ", ""));

        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponseDTO> handleBadRequest(Exception exception) {
        return build(HttpStatus.BAD_REQUEST, "Dados invalidos");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAuthentication(AuthenticationException exception) {
        return build(HttpStatus.UNAUTHORIZED, "Autenticacao necessaria");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResponseStatus(ResponseStatusException exception) {
        String message = exception.getReason();
        if (message == null || message.isBlank()) {
            message = "Erro na requisicao";
        }

        return build(exception.getStatusCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnexpected(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
    }

    private ResponseEntity<ApiErrorResponseDTO> build(HttpStatusCode status, String message) {
        return ResponseEntity
                .status(status)
                .body(errorResponseFactory.create(status, message));
    }

    private String errorMessage(FieldError error) {
        String code = error.getCode();
        if ("NotBlank".equals(code)) {
            return "nao pode ficar em branco";
        }
        if ("NotNull".equals(code)) {
            return "nao pode ser nulo";
        }
        if ("Email".equals(code)) {
            return "deve ser um e-mail valido";
        }

        return defaultErrorMessage(error);
    }

    private String defaultErrorMessage(DefaultMessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return message != null ? message : "invalido";
    }
}
