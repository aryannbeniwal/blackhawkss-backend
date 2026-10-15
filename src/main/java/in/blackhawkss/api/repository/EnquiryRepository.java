package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.Enquiry;
import in.blackhawkss.api.domain.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long>, JpaSpecificationExecutor<Enquiry> {
    long countByPhoneAndCreatedAtAfter(String phone, Instant after);
    long countByStatus(EnquiryStatus status);
    long countByCreatedAtAfter(Instant after);
    long countByFollowUpAtBetween(Instant from, Instant to);
    List<Enquiry> findByStatusAndCreatedAtBeforeAndReminderCountLessThan(EnquiryStatus status, Instant before, int reminderCount);
}
