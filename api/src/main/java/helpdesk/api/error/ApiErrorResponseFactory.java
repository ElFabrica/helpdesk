package helpdesk.api.error;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorResponseFactory {

    private final Clock clock;

    public ApiErrorResponseFactory(Clock clock) {
        this.clock = clock;
    }

    public ApiErrorResponseDTO create(HttpStatusCode statusCode, String message) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        return new ApiErrorResponseDTO(
                statusCode.value(),
                status != null ? status.getReasonPhrase() : "HTTP " + statusCode.value(),
                message,
                LocalDateTime.now(clock)
        );
    }
}
