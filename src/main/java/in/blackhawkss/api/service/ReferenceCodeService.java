package in.blackhawkss.api.service;

import in.blackhawkss.api.domain.ReferenceCounter;
import in.blackhawkss.api.repository.ReferenceCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Generates enquiry references like BH-2026-0001; the sequence resets each calendar year (IST). */
@Service
@RequiredArgsConstructor
public class ReferenceCodeService {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private final ReferenceCounterRepository counters;

    @Transactional(propagation = Propagation.REQUIRED)
    public String next() {
        int year = ZonedDateTime.now(IST).getYear();
        ReferenceCounter counter = counters.findForUpdate(year).orElseGet(() -> {
            ReferenceCounter c = ReferenceCounter.builder().year(year).lastValue(0).build();
            return counters.saveAndFlush(c);
        });
        // Re-lock after a fresh insert so concurrent callers serialise on the row.
        counter = counters.findForUpdate(year).orElse(counter);
        long value = counter.getLastValue() + 1;
        counter.setLastValue(value);
        counters.saveAndFlush(counter);
        return format(year, value);
    }

    static String format(int year, long value) {
        return String.format("BH-%d-%04d", year, value);
    }
}
