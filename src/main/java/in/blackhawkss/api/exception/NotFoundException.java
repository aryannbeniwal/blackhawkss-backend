package in.blackhawkss.api.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String what, Object id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, what + " " + id + " not found");
    }
}
