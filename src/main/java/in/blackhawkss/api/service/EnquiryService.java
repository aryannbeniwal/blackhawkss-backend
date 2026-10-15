package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.Enquiry;
import in.blackhawkss.api.domain.EnquiryNote;
import in.blackhawkss.api.domain.EnquirySource;
import in.blackhawkss.api.domain.EnquiryStatus;
import in.blackhawkss.api.dto.request.EnquiryRequest;
import in.blackhawkss.api.dto.request.EnquiryUpdateRequest;
import in.blackhawkss.api.dto.response.EnquiryResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.exception.RateLimitedException;
import in.blackhawkss.api.exception.ValidationException;
import in.blackhawkss.api.repository.EnquiryRepository;
import in.blackhawkss.api.util.PhoneNumbers;
import in.blackhawkss.api.web.rest.common.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiries;
    private final ReferenceCodeService referenceCodes;
    private final NotificationService notifications;
    private final AppProperties props;

    @Transactional
    public String create(EnquiryRequest req, String ipHash, String userAgent) {
        String phone = PhoneNumbers.normalise(req.phone());
        if (phone == null) {
            throw new ValidationException("Please correct the highlighted fields.",
                    Map.of("phone", "Enter a valid 10-digit Indian mobile number"));
        }
        int perPhone = props.rateLimit() == null ? 3 : props.rateLimit().perPhonePerDay();
        if (enquiries.countByPhoneAndCreatedAtAfter(phone, Instant.now().minus(Duration.ofDays(1))) >= perPhone) {
            throw new RateLimitedException("We already have recent enquiries from this number. Our team will call you, or reach us on WhatsApp.");
        }
        Enquiry e = Enquiry.builder()
                .referenceCode(referenceCodes.next())
                .fullName(req.fullName().trim())
                .phone(phone)
                .email(blankToNull(req.email()))
                .organisation(blankToNull(req.organisation()))
                .siteName(blankToNull(req.siteName()))
                .serviceCategory(req.serviceCategory())
                .location(req.location().trim())
                .preferredDate(req.preferredDate())
                .personnelCount(req.personnelCount())
                .durationShift(req.durationShift().trim())
                .requirements(req.requirements().trim())
                .eventVenue(blankToNull(req.eventVenue()))
                .expectedCrowd(req.expectedCrowd())
                .eventStart(blankToNull(req.eventStart()))
                .eventEnd(blankToNull(req.eventEnd()))
                .source(EnquirySource.WEB)
                .status(EnquiryStatus.NEW)
                .ipHash(ipHash)
                .userAgent(userAgent)
                .consentAt(Instant.now())
                .build();
        e = enquiries.save(e);
        notifications.onNewEnquiry(e.getId());
        log.info("New enquiry {} ({})", e.getReferenceCode(), e.getServiceCategory());
        return e.getReferenceCode();
    }

    @Transactional(readOnly = true)
    public PageResponse<EnquiryResponse> list(int page, int size, EnquiryStatus status, String q, LocalDate from, LocalDate to) {
        Specification<Enquiry> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                String digits = q.replaceAll("\\D", "");
                Predicate text = cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("referenceCode")), like),
                        cb.like(cb.lower(root.get("location")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("organisation"), "")), like));
                ps.add(digits.isEmpty() ? text : cb.or(text, cb.like(root.get("phone"), "%" + digits + "%")));
            }
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay().toInstant(ZoneOffset.UTC)));
            if (to != null) ps.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
            return cb.and(ps.toArray(Predicate[]::new));
        };
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(enquiries.findAll(spec, pageable), EnquiryResponse::summary);
    }

    @Transactional(readOnly = true)
    public EnquiryResponse get(Long id) {
        return EnquiryResponse.detail(find(id));
    }

    @Transactional
    public EnquiryResponse update(Long id, EnquiryUpdateRequest req) {
        Enquiry e = find(id);
        if (req.status() != null) e.setStatus(req.status());
        if (Boolean.TRUE.equals(req.clearFollowUp())) e.setFollowUpAt(null);
        else if (req.followUpAt() != null) e.setFollowUpAt(req.followUpAt());
        return EnquiryResponse.detail(enquiries.save(e));
    }

    @Transactional
    public EnquiryResponse addNote(Long id, String authorEmail, String body) {
        Enquiry e = find(id);
        e.getNotes().add(EnquiryNote.builder().enquiry(e).authorEmail(authorEmail).body(body.trim()).build());
        return EnquiryResponse.detail(enquiries.save(e));
    }

    private Enquiry find(Long id) {
        return enquiries.findById(id).orElseThrow(() -> new NotFoundException("Enquiry", id));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
