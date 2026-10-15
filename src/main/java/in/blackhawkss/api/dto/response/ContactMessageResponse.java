package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.ContactMessage;
import in.blackhawkss.api.util.PhoneNumbers;

import java.time.Instant;

public record ContactMessageResponse(Long id, String fullName, String phone, String phoneDisplay, String email,
                                     String subject, String message, boolean read, Instant createdAt) {
    public static ContactMessageResponse from(ContactMessage m) {
        return new ContactMessageResponse(m.getId(), m.getFullName(), m.getPhone(), PhoneNumbers.display(m.getPhone()),
                m.getEmail(), m.getSubject(), m.getMessage(), m.isRead(), m.getCreatedAt());
    }
}
