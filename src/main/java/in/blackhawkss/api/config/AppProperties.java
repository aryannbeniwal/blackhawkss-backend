package in.blackhawkss.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Docs docs,
        @NotBlank String publicBaseUrl,
        @NotBlank String adminBaseUrl,
        @NotNull Jwt jwt,
        @NotNull Cors cors,
        @NotNull Mail mail,
        @NotNull Uploads uploads,
        Admin admin,
        RateLimit rateLimit,
        Reminders reminders
) {
    public record Docs(boolean enabled) {}
    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {}
    public record Cors(List<String> allowedOrigins) {}
    public record Mail(boolean enabled, String from, List<String> ownerTo, String replyTo) {}
    public record Uploads(@NotBlank String dir) {}
    public record Admin(String seedEmail, String seedPassword, String seedName) {}
    public record RateLimit(int perIpPerHour, int perPhonePerDay) {}
    public record Reminders(boolean enabled) {}
}
