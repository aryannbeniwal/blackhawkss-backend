package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.dto.request.AnnouncementRequest;
import in.blackhawkss.api.dto.response.AnnouncementResponse;
import in.blackhawkss.api.service.AnnouncementService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin · Announcements")
@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcements;

    @GetMapping
    public ApiResponse<List<AnnouncementResponse>> list() {
        return ApiResponse.ok(announcements.listAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(@Valid @RequestBody AnnouncementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(announcements.create(req)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnnouncementResponse> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest req) {
        return ApiResponse.ok(announcements.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        announcements.delete(id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
