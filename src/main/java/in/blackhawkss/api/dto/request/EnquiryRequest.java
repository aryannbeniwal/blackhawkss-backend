package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.domain.ServiceCategory;
import in.blackhawkss.api.util.PhoneNumbers;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EnquiryRequest(
        @NotBlank(message = "Please enter your full name")
        @Size(min = 2, max = 120, message = "Name must be 2–120 characters")
        String fullName,

        @NotBlank(message = "Please enter your mobile number")
        @Pattern(regexp = PhoneNumbers.INDIAN_MOBILE_REGEX, message = "Enter a valid 10-digit Indian mobile number")
        String phone,

        @Email(message = "Enter a valid email address")
        @Size(max = 190)
        String email,

        @Size(max = 160) String organisation,
        @Size(max = 160) String siteName,

        @NotNull(message = "Please choose a service")
        ServiceCategory serviceCategory,

        @NotBlank(message = "Please tell us the location")
        @Size(max = 200)
        String location,

        @FutureOrPresent(message = "Preferred date cannot be in the past")
        LocalDate preferredDate,

        @NotNull(message = "How many personnel do you need?")
        @Min(value = 1, message = "At least 1 personnel")
        @Max(value = 10000, message = "Please contact us directly for very large deployments")
        Integer personnelCount,

        @NotBlank(message = "Please describe the duration or shift requirement")
        @Size(max = 200)
        String durationShift,

        @NotBlank(message = "Please describe your requirements")
        @Size(max = 4000, message = "Please keep requirements under 4000 characters")
        String requirements,

        @Size(max = 200) String eventVenue,
        @Min(0) @Max(1000000) Integer expectedCrowd,
        @Size(max = 40) String eventStart,
        @Size(max = 40) String eventEnd,

        @AssertTrue(message = "Please agree to the privacy policy")
        Boolean consent,

        /** Honeypot: must stay empty. Bots fill it. */
        String website
) {
    public boolean isSpam() {
        return website != null && !website.isBlank();
    }
}
