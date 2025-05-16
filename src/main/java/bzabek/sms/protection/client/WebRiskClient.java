package bzabek.sms.protection.client;

import bzabek.sms.protection.config.WebClientProperties;
import bzabek.sms.protection.exceptions.WebRiskClientException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class WebRiskClient {

    private static final String CIRCUIT_BREAKER_NAME = "webRiskService";
    public final WebClientProperties webClientProperties;
    private final String uri;
    private final String apiKey;
    private final Client client;

    private final CircuitBreaker circuitBreaker;

    public WebRiskClient(WebClientProperties webClientProperties) {
        this.webClientProperties = webClientProperties;
        this.uri = webClientProperties.getUrl();
        this.apiKey = webClientProperties.getApiKey();
        this.client = ClientBuilder.newClient();
        this.circuitBreaker = createCircuitBreaker();

    }

    public WebRiskResponse verifyUri(String uri) {
        return executeWithCircuitBreaker(() -> sendRequest(WebRiskRequest.phishing(uri)));
    }

    private WebRiskResponse executeWithCircuitBreaker(Supplier<WebRiskResponse> supplier) {
        return circuitBreaker.executeSupplier(supplier);
    }

    private WebRiskResponse sendRequest(WebRiskRequest riskRequest) {
        try {
            Response response = client
                    .target(uri)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-goog-api-key", apiKey)
                    .post(Entity.entity(riskRequest, MediaType.APPLICATION_JSON_TYPE));
            int httpStatus = response.getStatus();
            if (httpStatus / 2 != 100) {
                String message = String.format("Failed to communicate with external Web Risk service. Uri: [%s] Http status: [%d]. Response message: [%s]", uri, httpStatus, response.getEntity().toString());
                throw new WebRiskClientException(message);
            } else {
                return response.readEntity(WebRiskResponse.class);
            }
        } catch (Exception exception) {
            String message = String.format("Failed to communicate with external Web Risk service. Uri: [%s]", uri);
            throw new WebRiskClientException(message, exception);
        } finally {
            client.close();
        }
    }

    private CircuitBreaker createCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .recordExceptions(WebRiskClientException.class)
                .build();

        return CircuitBreakerRegistry.of(config)
                .circuitBreaker(CIRCUIT_BREAKER_NAME);
    }
}
