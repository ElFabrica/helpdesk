package helpdesk.api.auth;

public record TokenResponse(String token, String type) {

    public static TokenResponse bearer(String token) {
        return new TokenResponse(token, "Bearer");
    }
}
