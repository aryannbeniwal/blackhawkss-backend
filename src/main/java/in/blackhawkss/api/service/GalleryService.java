package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.GalleryCategory;
import in.blackhawkss.api.domain.GalleryItem;
import in.blackhawkss.api.dto.request.GalleryUpdateRequest;
import in.blackhawkss.api.dto.response.GalleryItemResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.exception.ValidationException;
import in.blackhawkss.api.repository.GalleryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_EDGE = 1600;
    private static final int THUMB_EDGE = 480;

    private final GalleryItemRepository items;
    private final AppProperties props;

    @Transactional(readOnly = true)
    public List<GalleryItemResponse> listPublished(GalleryCategory category) {
        var list = category == null
                ? items.findAllByPublishedTrueOrderBySortOrderAscCreatedAtDesc()
                : items.findAllByPublishedTrueAndCategoryOrderBySortOrderAscCreatedAtDesc(category);
        return list.stream().map(GalleryItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GalleryItemResponse> listAll() {
        return items.findAllByOrderBySortOrderAscCreatedAtDesc().stream().map(GalleryItemResponse::from).toList();
    }

    @Transactional
    public GalleryItemResponse upload(MultipartFile file, String title, GalleryCategory category) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Please choose an image.", Map.of("file", "Image is required"));
        }
        if (file.getContentType() == null || !ALLOWED.contains(file.getContentType().toLowerCase())) {
            throw new ValidationException("Unsupported image type.", Map.of("file", "Use JPEG, PNG or WebP"));
        }
        try {
            Path dir = uploadsDir();
            Files.createDirectories(dir);
            String id = UUID.randomUUID().toString().replace("-", "");
            String fileName = id + ".jpg";
            String thumbName = id + "_thumb.jpg";

            BufferedImage source;
            try (InputStream in = file.getInputStream()) {
                source = ImageIO.read(in);
            }
            if (source == null) {
                throw new ValidationException("Image could not be read.", Map.of("file", "Corrupt or unsupported image"));
            }
            BufferedImage main = Thumbnails.of(source).size(MAX_EDGE, MAX_EDGE).keepAspectRatio(true)
                    .imageType(BufferedImage.TYPE_INT_RGB).asBufferedImage();
            Thumbnails.of(main).scale(1.0).outputFormat("jpg").outputQuality(0.85).toFile(dir.resolve(fileName).toFile());
            Thumbnails.of(main).size(THUMB_EDGE, THUMB_EDGE).keepAspectRatio(true).outputFormat("jpg").outputQuality(0.8)
                    .toFile(dir.resolve(thumbName).toFile());

            GalleryItem item = items.save(GalleryItem.builder()
                    .title(title == null || title.isBlank() ? "Untitled" : title.trim())
                    .category(category == null ? GalleryCategory.SITES : category)
                    .fileName(fileName)
                    .thumbFileName(thumbName)
                    .width(main.getWidth())
                    .height(main.getHeight())
                    .build());
            return GalleryItemResponse.from(item);
        } catch (IOException e) {
            log.error("Gallery upload failed", e);
            throw new IllegalStateException("Could not store image", e);
        }
    }

    @Transactional
    public GalleryItemResponse update(Long id, GalleryUpdateRequest req) {
        GalleryItem g = find(id);
        if (req.title() != null && !req.title().isBlank()) g.setTitle(req.title().trim());
        if (req.category() != null) g.setCategory(req.category());
        if (req.published() != null) g.setPublished(req.published());
        if (req.sortOrder() != null) g.setSortOrder(req.sortOrder());
        return GalleryItemResponse.from(items.save(g));
    }

    @Transactional
    public void delete(Long id) {
        GalleryItem g = find(id);
        items.delete(g);
        try {
            Files.deleteIfExists(uploadsDir().resolve(g.getFileName()));
            Files.deleteIfExists(uploadsDir().resolve(g.getThumbFileName()));
        } catch (IOException e) {
            log.warn("Could not delete files for gallery item {}", id, e);
        }
    }

    private GalleryItem find(Long id) {
        return items.findById(id).orElseThrow(() -> new NotFoundException("Gallery item", id));
    }

    private Path uploadsDir() {
        return Path.of(props.uploads().dir()).toAbsolutePath().normalize();
    }
}
