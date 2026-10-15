package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.Enquiry;
import in.blackhawkss.api.domain.EnquiryNote;
import in.blackhawkss.api.domain.EnquirySource;
import in.blackhawkss.api.domain.EnquiryStatus;
import in.blackhawkss.api.domain.ServiceCategory;
import in.blackhawkss.api.util.PhoneNumbers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record EnquiryResponse(
        Long id, String referenceCode, String fullName, String phone, String phoneDisplay, String whatsappUrl,
        String email, String organisation, String siteName, ServiceCategory serviceCategory, String location,
        LocalDate preferredDate, Integer personnelCount, String durationShift, String requirements,
        String eventVenue, Integer expectedCrowd, String eventStart, String eventEnd,
        EnquirySource source, EnquiryStatus status, Instant followUpAt, boolean notifyFailed,
        Instant createdAt, Instant updatedAt, List<NoteResponse> notes
) {
    public record NoteResponse(Long id, String authorEmail, String body, Instant createdAt) {
        public static NoteResponse from(EnquiryNote n) {
            return new NoteResponse(n.getId(), n.getAuthorEmail(), n.getBody(), n.getCreatedAt());
        }
    }

    public static EnquiryResponse summary(Enquiry e) {
        return build(e, null);
    }

    public static EnquiryResponse detail(Enquiry e) {
        return build(e, e.getNotes().stream().map(NoteResponse::from).toList());
    }

    private static EnquiryResponse build(Enquiry e, List<NoteResponse> notes) {
        return new EnquiryResponse(
                e.getId(), e.getReferenceCode(), e.getFullName(), e.getPhone(), PhoneNumbers.display(e.getPhone()),
                "https://wa.me/" + PhoneNumbers.toE164(e.getPhone()),
                e.getEmail(), e.getOrganisation(), e.getSiteName(), e.getServiceCategory(), e.getLocation(),
                e.getPreferredDate(), e.getPersonnelCount(), e.getDurationShift(), e.getRequirements(),
                e.getEventVenue(), e.getExpectedCrowd(), e.getEventStart(), e.getEventEnd(),
                e.getSource(), e.getStatus(), e.getFollowUpAt(), e.isNotifyFailed(),
                e.getCreatedAt(), e.getUpdatedAt(), notes);
    }
}
