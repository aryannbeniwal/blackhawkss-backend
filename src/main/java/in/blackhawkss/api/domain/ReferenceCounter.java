package in.blackhawkss.api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reference_counters")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReferenceCounter {

    @Id
    @Column(name = "ref_year", nullable = false)
    private Integer year;

    @Column(name = "last_value", nullable = false)
    private long lastValue;
}
