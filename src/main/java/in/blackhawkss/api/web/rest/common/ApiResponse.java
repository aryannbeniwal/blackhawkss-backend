package in.blackhawkss.api.web.rest.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Uniform JSON envelope shared with the Next.js frontend. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, null));
    }

    public static ApiResponse<Void> fail(String code, String message, Map<String, String> fields) {
        return new ApiResponse<>(false, null, new ApiError(code, message, fields));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiError(String code, String message, Map<String, String> fields) {}
}
