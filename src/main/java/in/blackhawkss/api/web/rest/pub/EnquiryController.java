package in.blackhawkss.api.web.rest.pub;

import in.blackhawkss.api.dto.request.EnquiryRequest;
import in.blackhawkss.api.dto.response.EnquiryCreatedResponse;
import in.blackhawkss.api.service.EnquiryService;
import in.blackhawkss.api.util.RequestUtils;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public · Enquiries")
@RestController
@RequestMapping("/api/v1/public/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    @Operation(summary = "Submit a Request-a-Quote enquiry")
    @PostMapping
    public ResponseEntity<ApiResponse<EnquiryCreatedResponse>> create(@Valid @RequestBody EnquiryRequest req,
                                                                      HttpServletRequest http) {
        if (req.isSpam()) {
            // Honeypot tripped: look successful to the bot, store nothing.
            return ResponseEntity.ok(ApiResponse.ok(new EnquiryCreatedResponse("BH-0000-0000")));
        }
        String code = enquiryService.create(req, RequestUtils.ipHash(http), RequestUtils.userAgent(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(new EnquiryCreatedResponse(code)));
    }
}
