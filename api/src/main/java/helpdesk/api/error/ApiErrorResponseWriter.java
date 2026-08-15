package helpdesk.api.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorResponseWriter {

    public void write(HttpServletResponse response, ApiErrorResponseDTO errorResponse) throws IOException {
        response.setStatus(errorResponse.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(toJson(errorResponse));
    }

    private String toJson(ApiErrorResponseDTO errorResponse) {
        return """
                {"status":%d,"error":"%s","message":"%s","timestamp":"%s"}\
                """.formatted(
                errorResponse.status(),
                escape(errorResponse.error()),
                escape(errorResponse.message()),
                errorResponse.timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
