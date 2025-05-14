package bzabek.sms.protection.client;

import bzabek.sms.protection.config.WebClientProperties;
import bzabek.sms.protection.exceptions.WebRiskClientException;
import bzabek.sms.protection.model.ConfidenceLevel;
import bzabek.sms.protection.model.ThreatType;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class WebRiskClientTestIT {

    @RegisterExtension
    private final static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
    private static WebRiskClient webRiskClient;

    @BeforeEach
    void setUp() {
        WebClientProperties webClientProperties = new WebClientProperties();
        webClientProperties.setUrl(wireMockServer.baseUrl());
        webClientProperties.setApiKey("test-api-key");

        webRiskClient = new WebRiskClient(webClientProperties);
    }

    @Test
    public void verifyUri_shouldReturnSuccessResponse() {
        // Given
        String uri = "http://test.com";
        WebRiskResponse expectedResponse = new WebRiskResponse(
                List.of(new Score(ThreatType.SOCIAL_ENGINEERING, ConfidenceLevel.SAFE)));

        // Stub the WireMock server
        createStub200();

        // When
        WebRiskResponse actualResponse = webRiskClient.verifyUri(uri);

        // Then
        assertEquals(expectedResponse.scores().size(), 1);
        Score expectedScore = expectedResponse.scores().getFirst();
        Score actualScore = actualResponse.scores().getFirst();

        assertEquals(expectedScore, actualScore);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/"))
                .withHeader("X-goog-api-key", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    public void verifyUri_shouldHandleErrorResponse() {
        // Given
        String uri = "http://test.com";

        wireMockServer.stubFor(post(urlEqualTo("/"))
                .withHeader("X-goog-api-key", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // When
        assertThrows(WebRiskClientException.class, () -> webRiskClient.verifyUri(uri));

        // Expected
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("X-goog-api-key", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json")));
        assertThrows(RuntimeException.class, () -> webRiskClient.verifyUri(uri));
    }

    private void createStub200() {
        wireMockServer.stubFor(post(urlEqualTo("/"))
                .withHeader("X-goog-api-key", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                    {
                                      "scores": [
                                        {                                  
                                          "threatType": "SOCIAL_ENGINEERING",
                                          "confidenceLevel": "SAFE"
                                        }
                                      ]
                                    }
                                """)));
    }
}