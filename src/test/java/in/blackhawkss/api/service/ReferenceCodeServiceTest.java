package in.blackhawkss.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReferenceCodeServiceTest {

    @Autowired
    ReferenceCodeService service;

    @Test
    void formatsAsBhYearSequence() {
        assertThat(ReferenceCodeService.format(2026, 7)).isEqualTo("BH-2026-0007");
        assertThat(ReferenceCodeService.format(2026, 12345)).isEqualTo("BH-2026-12345");
    }

    @Test
    void generatesSequentialUniqueCodesForCurrentYear() {
        String year = String.valueOf(ZonedDateTime.now(ReferenceCodeService.IST).getYear());
        String first = service.next();
        String second = service.next();
        assertThat(first).startsWith("BH-" + year + "-");
        assertThat(second).isNotEqualTo(first);
        int a = Integer.parseInt(first.substring(first.lastIndexOf('-') + 1));
        int b = Integer.parseInt(second.substring(second.lastIndexOf('-') + 1));
        assertThat(b).isEqualTo(a + 1);
    }

    @Test
    void concurrentCallersNeverCollide() throws Exception {
        Set<String> codes = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 40; i++) {
            pool.submit(() -> codes.add(service.next()));
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(codes).hasSize(40);
    }
}
