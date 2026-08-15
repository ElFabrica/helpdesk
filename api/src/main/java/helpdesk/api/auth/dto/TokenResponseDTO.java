package helpdesk.api.auth.dto;

public record TokenResponseDTO(String token, String type) {

    public static TokenResponseDTO bearer(String token) {
        return new TokenResponseDTO(token, "Bearer");
    }
}
