package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.Announcement;

import java.time.Instant;

public record AnnouncementResponse(Long id, String title, String body, Instant startsAt, Instant endsAt,
                                   boolean published, Instant createdAt) {
    public static AnnouncementResponse from(Announcement a) {
        return new AnnouncementResponse(a.getId(), a.getTitle(), a.getBody(), a.getStartsAt(), a.getEndsAt(),
                a.isPublished(), a.getCreatedAt());
    }
}
