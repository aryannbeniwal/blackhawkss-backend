package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.ApplicationStatus;
import in.blackhawkss.api.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    long countByPhoneAndCreatedAtAfter(String phone, Instant after);
    Page<JobApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<JobApplication> findAllByStatusOrderByCreatedAtDesc(ApplicationStatus status, Pageable pageable);
    long countByStatus(ApplicationStatus status);
}
