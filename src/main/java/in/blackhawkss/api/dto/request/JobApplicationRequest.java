package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.util.PhoneNumbers;
import jakarta.validation.constraints.*;

public record JobApplicationRequest(
        @NotBlank(message = "Please enter your full name") @Size(min = 2, max = 120) String fullName,
        @NotBlank(message = "Please enter your mobile number")
        @Pattern(regexp = PhoneNumbers.INDIAN_MOBILE_REGEX, message = "Enter a valid 10-digit Indian mobile number")
        String phone,
        @NotBlank(message = "Please enter your city or district") @Size(max = 120) String city,
        @NotBlank(message = "Please choose a role") @Size(max = 60) String roleApplied,
        @Min(0) @Max(60) Integer experienceYears,
        Boolean exServiceman,
        @Size(max = 160) String serviceUnit,
        @Min(value = 18, message = "Applicants must be 18 or older") @Max(70) Integer age,
        @Size(max = 2000) String message,
        @AssertTrue(message = "Please agree to the privacy policy") Boolean consent,
        String website
) {
    public boolean isSpam() {
        return website != null && !website.isBlank();
    }
}
