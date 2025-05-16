package bzabek.sms.protection.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SubscriptionsDatabaseException extends RuntimeException {

    // make spring happy
    private SubscriptionsDatabaseException() {
    }

    public SubscriptionsDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
