package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.ApplicationStatus;
import in.blackhawkss.api.domain.EnquiryStatus;
import in.blackhawkss.api.dto.response.DashboardResponse;
import in.blackhawkss.api.repository.ContactMessageRepository;
import in.blackhawkss.api.repository.EnquiryRepository;
import in.blackhawkss.api.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EnquiryRepository enquiries;
    private final JobApplicationRepository applications;
    private final ContactMessageRepository messages;

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (EnquiryStatus s : EnquiryStatus.values()) {
            byStatus.put(s.name(), enquiries.countByStatus(s));
        }
        ZonedDateTime nowIst = ZonedDateTime.now(ReferenceCodeService.IST);
        LocalDate today = nowIst.toLocalDate();
        Instant weekStart = today.minusDays(6).atStartOfDay(ReferenceCodeService.IST).toInstant();
        Instant dayStart = today.atStartOfDay(ReferenceCodeService.IST).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(ReferenceCodeService.IST).toInstant();
        return new DashboardResponse(
                byStatus,
                enquiries.countByCreatedAtAfter(weekStart),
                enquiries.countByFollowUpAtBetween(dayStart, dayEnd),
                applications.countByStatus(ApplicationStatus.NEW),
                messages.countByReadFalse(),
                enquiries.count());
    }
}
