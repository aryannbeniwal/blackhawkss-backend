package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.domain.EnquiryStatus;
import in.blackhawkss.api.dto.request.EnquiryUpdateRequest;
import in.blackhawkss.api.dto.request.NoteRequest;
import in.blackhawkss.api.dto.response.EnquiryResponse;
import in.blackhawkss.api.security.AdminPrincipal;
import in.blackhawkss.api.service.EnquiryService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import in.blackhawkss.api.web.rest.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Admin · Enquiries")
@RestController
@RequestMapping("/api/v1/admin/enquiries")
@RequiredArgsConstructor
public class AdminEnquiryController {

    private final EnquiryService enquiries;

    @GetMapping
    public ApiResponse<PageResponse<EnquiryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(enquiries.list(page, size, status, q, from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<EnquiryResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(enquiries.get(id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<EnquiryResponse> update(@PathVariable Long id, @RequestBody EnquiryUpdateRequest req) {
        return ApiResponse.ok(enquiries.update(id, req));
    }

    @PostMapping("/{id}/notes")
    public ApiResponse<EnquiryResponse> addNote(@PathVariable Long id, @Valid @RequestBody NoteRequest req,
                                                @AuthenticationPrincipal AdminPrincipal admin) {
        return ApiResponse.ok(enquiries.addNote(id, admin.email(), req.body()));
    }
}
