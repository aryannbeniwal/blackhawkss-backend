package in.blackhawkss.api.dto.response;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
    public record UserResponse(Long id, String email, String displayName, String role) {}
}
