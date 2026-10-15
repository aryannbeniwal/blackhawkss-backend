package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.ApplicationStatus;
import in.blackhawkss.api.domain.JobApplication;
import in.blackhawkss.api.dto.request.JobApplicationRequest;
import in.blackhawkss.api.dto.response.JobApplicationResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.exception.RateLimitedException;
import in.blackhawkss.api.exception.ValidationException;
import in.blackhawkss.api.repository.JobApplicationRepository;
import in.blackhawkss.api.util.PhoneNumbers;
import in.blackhawkss.api.web.rest.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applications;
    private final NotificationService notifications;

    @Transactional
    public Long create(JobApplicationRequest req, String ipHash) {
        String phone = PhoneNumbers.normalise(req.phone());
        if (phone == null) {
            throw new ValidationException("Please correct the highlighted fields.",
                    Map.of("phone", "Enter a valid 10-digit Indian mobile number"));
        }
        if (applications.countByPhoneAndCreatedAtAfter(phone, Instant.now().minus(Duration.ofDays(1))) >= 3) {
            throw new RateLimitedException("We already have your application. Our team will call you.");
        }
        JobApplication a = applications.save(JobApplication.builder()
                .fullName(req.fullName().trim())
                .phone(phone)
                .city(req.city().trim())
                .roleApplied(req.roleApplied().trim())
                .experienceYears(req.experienceYears())
                .exServiceman(Boolean.TRUE.equals(req.exServiceman()))
                .serviceUnit(req.serviceUnit() == null || req.serviceUnit().isBlank() ? null : req.serviceUnit().trim())
                .age(req.age())
                .message(req.message() == null || req.message().isBlank() ? null : req.message().trim())
                .ipHash(ipHash)
                .build());
        notifications.onNewApplication(a.getId());
        return a.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> list(int page, int size, ApplicationStatus status) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        var result = status == null
                ? applications.findAllByOrderByCreatedAtDesc(pageable)
                : applications.findAllByStatusOrderByCreatedAtDesc(status, pageable);
        return PageResponse.of(result, JobApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse get(Long id) {
        return JobApplicationResponse.from(find(id));
    }

    @Transactional
    public JobApplicationResponse updateStatus(Long id, ApplicationStatus status) {
        JobApplication a = find(id);
        a.setStatus(status);
        return JobApplicationResponse.from(applications.save(a));
    }

    private JobApplication find(Long id) {
        return applications.findById(id).orElseThrow(() -> new NotFoundException("Application", id));
    }
}
