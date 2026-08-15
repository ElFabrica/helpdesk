package helpdesk.api.error;

import java.time.LocalDateTime;

public record ApiErrorResponseDTO(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}
