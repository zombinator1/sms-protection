package bzabek.sms.protection.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestControllerExceptionHandler {

    public static final String CLIENT_FAILURE_MSG = "Internal error. Service could not verify SMS due to failed communication with external service.";
    public static final String DB_FAILURE_MSG = "Internal error. Could not access users subscriptions.";
    public static final String UNEXPECTED_ERROR_MSG = "Internal error. An unexpected error occurred. Please try again later.";
    private static final Logger logger = LoggerFactory.getLogger(RestControllerExceptionHandler.class);

    @ExceptionHandler(WebRiskClientException.class)
    public ResponseEntity<Map<String, Object>> handleWebRiskClientFailure(WebRiskClientException ex) {
        logger.error("Failed to communicate with WebRiskClient", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", CLIENT_FAILURE_MSG);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SubscriptionsDatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseFailure(SubscriptionsDatabaseException ex) {
        logger.error("Failed to communicate with Subscriptions DB", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", DB_FAILURE_MSG);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", UNEXPECTED_ERROR_MSG);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
