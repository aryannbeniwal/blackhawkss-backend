package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("""
            select a from Announcement a
            where a.published = true
              and (a.startsAt is null or a.startsAt <= :now)
              and (a.endsAt is null or a.endsAt >= :now)
            order by a.createdAt desc
            """)
    List<Announcement> findActive(@Param("now") Instant now);

    List<Announcement> findAllByOrderByCreatedAtDesc();
}
