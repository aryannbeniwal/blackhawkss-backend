package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.domain.EnquiryStatus;

import java.time.Instant;

/** Partial update: null fields are left untouched. Use clearFollowUp=true to remove the date. */
public record EnquiryUpdateRequest(EnquiryStatus status, Instant followUpAt, Boolean clearFollowUp) {}
