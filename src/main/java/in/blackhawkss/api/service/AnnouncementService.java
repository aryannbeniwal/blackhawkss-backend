package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.Announcement;
import in.blackhawkss.api.dto.request.AnnouncementRequest;
import in.blackhawkss.api.dto.response.AnnouncementResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcements;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listActive() {
        return announcements.findActive(Instant.now()).stream().map(AnnouncementResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAll() {
        return announcements.findAllByOrderByCreatedAtDesc().stream().map(AnnouncementResponse::from).toList();
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest req) {
        return AnnouncementResponse.from(announcements.save(apply(new Announcement(), req)));
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest req) {
        return AnnouncementResponse.from(announcements.save(apply(find(id), req)));
    }

    @Transactional
    public void delete(Long id) {
        announcements.delete(find(id));
    }

    private Announcement find(Long id) {
        return announcements.findById(id).orElseThrow(() -> new NotFoundException("Announcement", id));
    }

    private static Announcement apply(Announcement a, AnnouncementRequest req) {
        a.setTitle(req.title().trim());
        a.setBody(req.body().trim());
        a.setStartsAt(req.startsAt());
        a.setEndsAt(req.endsAt());
        if (req.published() != null) a.setPublished(req.published());
        return a;
    }
}
