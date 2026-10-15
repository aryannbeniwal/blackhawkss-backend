package in.blackhawkss.api.repository;

import in.blackhawkss.api.domain.ReferenceCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReferenceCounterRepository extends JpaRepository<ReferenceCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ReferenceCounter c where c.year = :year")
    Optional<ReferenceCounter> findForUpdate(@Param("year") int year);
}
