package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.ClientOrg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientOrgRepository extends JpaRepository<ClientOrg, Long> {
    List<ClientOrg> findAllByPublishedTrueOrderBySortOrderAscNameAsc();
    List<ClientOrg> findAllByOrderBySortOrderAscNameAsc();
}
