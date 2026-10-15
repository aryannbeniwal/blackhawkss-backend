package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.ApplicationStatus;
import in.blackhawkss.api.domain.JobApplication;
import in.blackhawkss.api.util.PhoneNumbers;

import java.time.Instant;

public record JobApplicationResponse(Long id, String fullName, String phone, String phoneDisplay, String whatsappUrl,
                                     String city, String roleApplied, Integer experienceYears, boolean exServiceman,
                                     String serviceUnit, Integer age, String message, ApplicationStatus status,
                                     Instant createdAt, Instant updatedAt) {
    public static JobApplicationResponse from(JobApplication a) {
        return new JobApplicationResponse(a.getId(), a.getFullName(), a.getPhone(), PhoneNumbers.display(a.getPhone()),
                "https://wa.me/" + PhoneNumbers.toE164(a.getPhone()), a.getCity(), a.getRoleApplied(),
                a.getExperienceYears(), a.isExServiceman(), a.getServiceUnit(), a.getAge(), a.getMessage(),
                a.getStatus(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
