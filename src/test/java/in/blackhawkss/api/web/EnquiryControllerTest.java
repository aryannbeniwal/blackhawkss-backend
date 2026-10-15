package in.blackhawkss.api.web;

import in.blackhawkss.api.repository.EnquiryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnquiryControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnquiryRepository enquiries;

    private static String body(String phone, String website) {
        return """
                {
                  "fullName": "Test Prospect",
                  "phone": "%s",
                  "email": "prospect@example.com",
                  "serviceCategory": "SECURITY_GUARDS",
                  "location": "Sector 29, Panipat",
                  "personnelCount": 4,
                  "durationShift": "12-hour day shift, 6 days a week",
                  "requirements": "Two gates, one for vehicles and one for staff.",
                  "consent": true,
                  "website": "%s"
                }
                """.formatted(phone, website);
    }

    @Test
    void validSubmissionReturnsReferenceCode() throws Exception {
        long before = enquiries.count();
        mvc.perform(post("/api/v1/public/enquiries").contentType(MediaType.APPLICATION_JSON).content(body("+91 98765 43210", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.referenceCode", startsWith("BH-")));
        assertThat(enquiries.count()).isEqualTo(before + 1);
    }

    @Test
    void invalidPhoneIsRejectedWithFieldError() throws Exception {
        mvc.perform(post("/api/v1/public/enquiries").contentType(MediaType.APPLICATION_JSON).content(body("12345", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fields.phone").exists());
    }

    @Test
    void honeypotLooksSuccessfulButStoresNothing() throws Exception {
        long before = enquiries.count();
        mvc.perform(post("/api/v1/public/enquiries").contentType(MediaType.APPLICATION_JSON).content(body("9876543211", "http://spam.example")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertThat(enquiries.count()).isEqualTo(before);
    }
}
