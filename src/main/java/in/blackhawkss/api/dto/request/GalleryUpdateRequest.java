package in.blackhawkss.api.dto.request;

import in.blackhawkss.api.domain.GalleryCategory;
import jakarta.validation.constraints.Size;

public record GalleryUpdateRequest(@Size(max = 160) String title, GalleryCategory category, Boolean published, Integer sortOrder) {}
