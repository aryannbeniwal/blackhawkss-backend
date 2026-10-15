package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.domain.ApplicationStatus;
import in.blackhawkss.api.dto.request.ApplicationStatusRequest;
import in.blackhawkss.api.dto.response.JobApplicationResponse;
import in.blackhawkss.api.service.JobApplicationService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import in.blackhawkss.api.web.rest.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin · Applications")
@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {

    private final JobApplicationService applications;

    @GetMapping
    public ApiResponse<PageResponse<JobApplicationResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) ApplicationStatus status) {
        return ApiResponse.ok(applications.list(page, size, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobApplicationResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(applications.get(id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<JobApplicationResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody ApplicationStatusRequest req) {
        return ApiResponse.ok(applications.updateStatus(id, req.status()));
    }
}
