package in.blackhawkss.api.security;

public record AdminPrincipal(Long id, String email, String displayName, String role) {}
