package in.blackhawkss.api.web.rest.pub;

import in.blackhawkss.api.dto.request.JobApplicationRequest;
import in.blackhawkss.api.service.JobApplicationService;
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

@Tag(name = "Public · Careers")
@RestController
@RequestMapping("/api/v1/public/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final JobApplicationService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@Valid @RequestBody JobApplicationRequest req, HttpServletRequest http) {
        if (req.isSpam()) return ResponseEntity.ok(ApiResponse.ok(Map.of("received", true)));
        Long id = service.create(req, RequestUtils.ipHash(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(Map.of("received", true, "id", id)));
    }
}
