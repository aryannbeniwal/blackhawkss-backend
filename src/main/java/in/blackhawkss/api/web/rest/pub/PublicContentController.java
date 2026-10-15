package in.blackhawkss.api.web.rest.pub;

import in.blackhawkss.api.domain.GalleryCategory;
import in.blackhawkss.api.dto.response.AnnouncementResponse;
import in.blackhawkss.api.dto.response.ClientOrgResponse;
import in.blackhawkss.api.dto.response.GalleryItemResponse;
import in.blackhawkss.api.service.AnnouncementService;
import in.blackhawkss.api.service.ClientOrgService;
import in.blackhawkss.api.service.GalleryService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Public · Content")
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicContentController {

    private final GalleryService gallery;
    private final ClientOrgService clients;
    private final AnnouncementService announcements;

    @GetMapping("/gallery")
    public ApiResponse<List<GalleryItemResponse>> gallery(@RequestParam(required = false) GalleryCategory category) {
        return ApiResponse.ok(gallery.listPublished(category));
    }

    @GetMapping("/clients")
    public ApiResponse<List<ClientOrgResponse>> clients() {
        return ApiResponse.ok(clients.listPublished());
    }

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementResponse>> announcements() {
        return ApiResponse.ok(announcements.listActive());
    }
}
