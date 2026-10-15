package in.blackhawkss.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AnnouncementRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 4000) String body,
        Instant startsAt,
        Instant endsAt,
        Boolean published
) {}
