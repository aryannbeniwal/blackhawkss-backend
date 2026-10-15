package in.blackhawkss.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enquiries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Enquiry {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_code", nullable = false, unique = true, length = 20)
    private String referenceCode;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(length = 190)
    private String email;

    @Column(length = 160)
    private String organisation;

    @Column(name = "site_name", length = 160)
    private String siteName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", nullable = false, length = 40)
    private ServiceCategory serviceCategory;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "personnel_count")
    private Integer personnelCount;

    @Column(name = "duration_shift", length = 200)
    private String durationShift;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "event_venue", length = 200)
    private String eventVenue;

    @Column(name = "expected_crowd")
    private Integer expectedCrowd;

    @Column(name = "event_start", length = 40)
    private String eventStart;

    @Column(name = "event_end", length = 40)
    private String eventEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnquirySource source = EnquirySource.WEB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EnquiryStatus status = EnquiryStatus.NEW;

    @Column(name = "follow_up_at")
    private Instant followUpAt;

    @Column(name = "notify_failed", nullable = false)
    @Builder.Default
    private boolean notifyFailed = false;

    @Column(name = "reminder_count", nullable = false)
    @Builder.Default
    private int reminderCount = 0;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<EnquiryNote> notes = new ArrayList<>();

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
