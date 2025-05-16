package bzabek.sms.protection.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebRiskClientException extends RuntimeException {

    // make spring happy
    private WebRiskClientException() {
    }

    public WebRiskClientException(String message) {
        super(message);
    }

    public WebRiskClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
