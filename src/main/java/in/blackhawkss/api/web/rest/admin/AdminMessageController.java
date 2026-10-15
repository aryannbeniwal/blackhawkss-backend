package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.dto.response.ContactMessageResponse;
import in.blackhawkss.api.service.ContactMessageService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import in.blackhawkss.api.web.rest.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin · Messages")
@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final ContactMessageService messages;

    @GetMapping
    public ApiResponse<PageResponse<ContactMessageResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(messages.list(page, size));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<ContactMessageResponse> markRead(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean read) {
        return ApiResponse.ok(messages.markRead(id, read));
    }
}
