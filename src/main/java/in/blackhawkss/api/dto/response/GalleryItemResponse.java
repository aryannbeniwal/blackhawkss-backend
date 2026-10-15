package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.GalleryCategory;
import in.blackhawkss.api.domain.GalleryItem;

import java.time.Instant;

public record GalleryItemResponse(Long id, String title, GalleryCategory category, String url, String thumbUrl,
                                  int width, int height, int sortOrder, boolean published, Instant createdAt) {
    public static GalleryItemResponse from(GalleryItem g) {
        return new GalleryItemResponse(g.getId(), g.getTitle(), g.getCategory(),
                "/uploads/" + g.getFileName(), "/uploads/" + g.getThumbFileName(),
                g.getWidth(), g.getHeight(), g.getSortOrder(), g.isPublished(), g.getCreatedAt());
    }
}
