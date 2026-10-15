package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.domain.GalleryCategory;
import in.blackhawkss.api.dto.request.GalleryUpdateRequest;
import in.blackhawkss.api.dto.response.GalleryItemResponse;
import in.blackhawkss.api.service.GalleryService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin · Gallery")
@RestController
@RequestMapping("/api/v1/admin/gallery")
@RequiredArgsConstructor
public class AdminGalleryController {

    private final GalleryService gallery;

    @GetMapping
    public ApiResponse<List<GalleryItemResponse>> list() {
        return ApiResponse.ok(gallery.listAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GalleryItemResponse>> upload(@RequestPart("file") MultipartFile file,
                                                                   @RequestParam(required = false) String title,
                                                                   @RequestParam(required = false) GalleryCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(gallery.upload(file, title, category)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<GalleryItemResponse> update(@PathVariable Long id, @RequestBody GalleryUpdateRequest req) {
        return ApiResponse.ok(gallery.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        gallery.delete(id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
