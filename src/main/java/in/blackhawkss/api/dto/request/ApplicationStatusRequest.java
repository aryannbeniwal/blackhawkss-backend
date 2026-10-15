package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {}
