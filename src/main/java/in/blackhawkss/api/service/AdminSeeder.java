package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.AdminUser;
import in.blackhawkss.api.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first admin from ADMIN_SEED_EMAIL / ADMIN_SEED_PASSWORD when no admin exists yet. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final AdminUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) return;
        AppProperties.Admin admin = props.admin();
        if (admin == null || admin.seedEmail() == null || admin.seedEmail().isBlank()
                || admin.seedPassword() == null || admin.seedPassword().length() < 10) {
            log.warn("No admin user exists and ADMIN_SEED_EMAIL/ADMIN_SEED_PASSWORD (>=10 chars) are not set; admin login unavailable.");
            return;
        }
        users.save(AdminUser.builder()
                .email(admin.seedEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(admin.seedPassword()))
                .displayName(admin.seedName() == null ? "Admin" : admin.seedName())
                .build());
        log.info("Seeded first admin user {}", admin.seedEmail());
    }
}
