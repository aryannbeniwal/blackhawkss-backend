package in.blackhawkss.api.security;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.util.RequestUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Token bucket per client IP for public POST endpoints (default 5 per hour). */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int perHour;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(AppProperties props) {
        this.perHour = props.rateLimit() == null ? 5 : props.rateLimit().perIpPerHour();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/v1/public/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (buckets.size() > 50_000) buckets.clear(); // crude memory guard for a single small VM
        Bucket bucket = buckets.computeIfAbsent(RequestUtils.clientIp(request), ip -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perHour).refillGreedy(perHour, Duration.ofHours(1)).build())
                .build());
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests. Please try again in a while or call us directly.\"}}");
    }
}
