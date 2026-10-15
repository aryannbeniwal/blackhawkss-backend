package in.blackhawkss.api.exception;

import org.springframework.http.HttpStatus;

public class RateLimitedException extends ApiException {
    public RateLimitedException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMITED, message);
    }
}
