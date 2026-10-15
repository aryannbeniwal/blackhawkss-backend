package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.dto.response.DashboardResponse;
import in.blackhawkss.api.service.DashboardService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin · Dashboard")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboard;

    @GetMapping
    public ApiResponse<DashboardResponse> summary() {
        return ApiResponse.ok(dashboard.summary());
    }
}
