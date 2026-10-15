package in.blackhawkss.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "client_orgs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ClientOrg {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160)
    private String area;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(length = 40)
    private String sector;

    @Column(length = 300)
    private String highlight;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
