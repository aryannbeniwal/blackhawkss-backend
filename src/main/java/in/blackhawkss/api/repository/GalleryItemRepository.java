package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.GalleryCategory;
import in.blackhawkss.api.domain.GalleryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {
    List<GalleryItem> findAllByPublishedTrueOrderBySortOrderAscCreatedAtDesc();
    List<GalleryItem> findAllByPublishedTrueAndCategoryOrderBySortOrderAscCreatedAtDesc(GalleryCategory category);
    List<GalleryItem> findAllByOrderBySortOrderAscCreatedAtDesc();
}
