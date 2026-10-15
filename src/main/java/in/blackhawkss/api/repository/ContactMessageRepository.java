package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    long countByPhoneAndCreatedAtAfter(String phone, Instant after);
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByReadFalse();
}
