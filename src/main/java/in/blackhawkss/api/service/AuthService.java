package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.AdminUser;
import in.blackhawkss.api.domain.RefreshToken;
import in.blackhawkss.api.dto.response.AuthResponse;
import in.blackhawkss.api.exception.UnauthorizedException;
import in.blackhawkss.api.repository.AdminUserRepository;
import in.blackhawkss.api.repository.RefreshTokenRepository;
import in.blackhawkss.api.security.JwtService;
import in.blackhawkss.api.util.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AdminUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties props;

    @Transactional
    public AuthResponse login(String email, String password) {
        AdminUser user = users.findByEmailIgnoreCase(email.trim())
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        user.setLastLoginAt(Instant.now());
        users.save(user);
        return issue(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokens.findByTokenHash(Hashing.sha256Hex(rawRefreshToken))
                .filter(RefreshToken::isUsable)
                .orElseThrow(() -> new UnauthorizedException("Session expired. Please sign in again."));
        stored.setRevokedAt(Instant.now());
        refreshTokens.save(stored);
        AdminUser user = stored.getAdminUser();
        if (!user.isActive()) throw new UnauthorizedException("Account disabled.");
        return issue(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        refreshTokens.findByTokenHash(Hashing.sha256Hex(rawRefreshToken)).ifPresent(t -> {
            t.setRevokedAt(Instant.now());
            refreshTokens.save(t);
        });
    }

    private AuthResponse issue(AdminUser user) {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(RefreshToken.builder()
                .tokenHash(Hashing.sha256Hex(raw))
                .adminUser(user)
                .expiresAt(Instant.now().plus(props.jwt().refreshTtl()))
                .build());
        refreshTokens.purgeExpired(Instant.now());
        return new AuthResponse(jwtService.issueAccessToken(user), raw, jwtService.accessTtlSeconds(),
                new AuthResponse.UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole()));
    }
}
