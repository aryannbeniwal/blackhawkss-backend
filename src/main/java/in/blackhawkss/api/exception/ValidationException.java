package in.blackhawkss.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends ApiException {
    private final Map<String, String> fields;

    public ValidationException(String message, Map<String, String> fields) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
        this.fields = fields;
    }
}
