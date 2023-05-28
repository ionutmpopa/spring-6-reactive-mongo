package guru.springframework.reactivemongo.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String VALIDATION_FAILED = "Validation failed";

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  ErrorAttributeOptions options) {

        Throwable error = getError(request);

        Map<String, Object> map = super.getErrorAttributes(
            request, options);

        return switch (getErrorMessage(error)) {
            case "Element not found" -> notFoundObjectMap(map);
            case VALIDATION_FAILED -> badRequestObjectMap(map);
            default -> internalErrorObjectMap(map);
        };
    }

    private static String getErrorMessage(Throwable error) {
        String message;
        if (error.getMessage().contains(VALIDATION_FAILED) ||
            error.getMessage().contains("400 BAD_REQUEST")) {
            message = VALIDATION_FAILED;
        } else {
            message = error.getMessage();
        }
        return message;
    }

    private static Map<String, Object> internalErrorObjectMap(Map<String, Object> map) {
        map.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
        map.put(ERROR, "Internal Server Error");
        map.put(MESSAGE, "Unexpected error occurred!");
        return map;
    }

    private static Map<String, Object> badRequestObjectMap(Map<String, Object> map) {
        map.put(STATUS, HttpStatus.BAD_REQUEST);
        map.put(ERROR, "Bad request");
        map.put(MESSAGE, "Invalid argument!");
        return map;
    }

    private static Map<String, Object> notFoundObjectMap(Map<String, Object> map) {
        map.put(STATUS, HttpStatus.NOT_FOUND);
        map.put(ERROR, "No such element");
        map.put(MESSAGE, "The element is missing!");
        return map;
    }

}
