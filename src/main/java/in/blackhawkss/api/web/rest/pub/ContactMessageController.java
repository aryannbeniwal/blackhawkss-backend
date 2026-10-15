package in.blackhawkss.api.web.rest.pub;

import in.blackhawkss.api.dto.request.ContactMessageRequest;
import in.blackhawkss.api.service.ContactMessageService;
import in.blackhawkss.api.util.RequestUtils;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Public · Contact")
@RestController
@RequestMapping("/api/v1/public/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@Valid @RequestBody ContactMessageRequest req, HttpServletRequest http) {
        if (req.isSpam()) return ResponseEntity.ok(ApiResponse.ok(Map.of("received", true)));
        Long id = service.create(req, RequestUtils.ipHash(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(Map.of("received", true, "id", id)));
    }
}
