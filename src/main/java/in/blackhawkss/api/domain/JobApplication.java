package in.blackhawkss.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "job_applications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobApplication {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(name = "role_applied", nullable = false, length = 60)
    private String roleApplied;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "is_ex_serviceman", nullable = false)
    @Builder.Default
    private boolean exServiceman = false;

    @Column(name = "service_unit", length = 160)
    private String serviceUnit;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.NEW;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
