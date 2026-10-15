package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.ClientOrg;
import in.blackhawkss.api.dto.request.ClientOrgRequest;
import in.blackhawkss.api.dto.response.ClientOrgResponse;
import in.blackhawkss.api.exception.NotFoundException;
import in.blackhawkss.api.repository.ClientOrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientOrgService {

    private final ClientOrgRepository clients;

    @Transactional(readOnly = true)
    public List<ClientOrgResponse> listPublished() {
        return clients.findAllByPublishedTrueOrderBySortOrderAscNameAsc().stream().map(ClientOrgResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientOrgResponse> listAll() {
        return clients.findAllByOrderBySortOrderAscNameAsc().stream().map(ClientOrgResponse::from).toList();
    }

    @Transactional
    public ClientOrgResponse create(ClientOrgRequest req) {
        return ClientOrgResponse.from(clients.save(apply(new ClientOrg(), req)));
    }

    @Transactional
    public ClientOrgResponse update(Long id, ClientOrgRequest req) {
        return ClientOrgResponse.from(clients.save(apply(find(id), req)));
    }

    @Transactional
    public void delete(Long id) {
        clients.delete(find(id));
    }

    private ClientOrg find(Long id) {
        return clients.findById(id).orElseThrow(() -> new NotFoundException("Client", id));
    }

    private static ClientOrg apply(ClientOrg c, ClientOrgRequest req) {
        c.setName(req.name().trim());
        c.setArea(req.area() == null || req.area().isBlank() ? null : req.area().trim());
        c.setCity(req.city().trim());
        c.setSector(req.sector() == null || req.sector().isBlank() ? null : req.sector().trim());
        c.setHighlight(req.highlight() == null || req.highlight().isBlank() ? null : req.highlight().trim());
        if (req.published() != null) c.setPublished(req.published());
        if (req.sortOrder() != null) c.setSortOrder(req.sortOrder());
        return c;
    }
}
