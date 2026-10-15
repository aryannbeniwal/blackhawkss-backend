package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.ContactMessage;
import in.blackhawkss.api.dto.request.ContactMessageRequest;
import in.blackhawkss.api.dto.response.ContactMessageResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.exception.RateLimitedException;
import in.blackhawkss.api.exception.ValidationException;
import in.blackhawkss.api.repository.ContactMessageRepository;
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
public class ContactMessageService {

    private final ContactMessageRepository messages;
    private final NotificationService notifications;

    @Transactional
    public Long create(ContactMessageRequest req, String ipHash) {
        String phone = PhoneNumbers.normalise(req.phone());
        if (phone == null) {
            throw new ValidationException("Please correct the highlighted fields.",
                    Map.of("phone", "Enter a valid 10-digit Indian mobile number"));
        }
        if (messages.countByPhoneAndCreatedAtAfter(phone, Instant.now().minus(Duration.ofDays(1))) >= 3) {
            throw new RateLimitedException("We already have recent messages from this number. Please call or WhatsApp us.");
        }
        ContactMessage m = messages.save(ContactMessage.builder()
                .fullName(req.fullName().trim())
                .phone(phone)
                .email(req.email() == null || req.email().isBlank() ? null : req.email().trim())
                .subject(req.subject() == null || req.subject().isBlank() ? null : req.subject().trim())
                .message(req.message().trim())
                .ipHash(ipHash)
                .build());
        notifications.onNewContactMessage(m.getId());
        return m.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactMessageResponse> list(int page, int size) {
        return PageResponse.of(messages.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))),
                ContactMessageResponse::from);
    }

    @Transactional
    public ContactMessageResponse markRead(Long id, boolean read) {
        ContactMessage m = messages.findById(id).orElseThrow(() -> new NotFoundException("Message", id));
        m.setRead(read);
        return ContactMessageResponse.from(messages.save(m));
    }
}
