package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.util.PhoneNumbers;
import jakarta.validation.constraints.*;

public record ContactMessageRequest(
        @NotBlank(message = "Please enter your full name") @Size(min = 2, max = 120) String fullName,
        @NotBlank(message = "Please enter your mobile number")
        @Pattern(regexp = PhoneNumbers.INDIAN_MOBILE_REGEX, message = "Enter a valid 10-digit Indian mobile number")
        String phone,
        @Email(message = "Enter a valid email address") @Size(max = 190) String email,
        @Size(max = 160) String subject,
        @NotBlank(message = "Please write a message") @Size(max = 4000) String message,
        @AssertTrue(message = "Please agree to the privacy policy") Boolean consent,
        String website
) {
    public boolean isSpam() {
        return website != null && !website.isBlank();
    }
}
