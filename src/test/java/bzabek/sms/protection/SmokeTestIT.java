package bzabek.sms.protection;

import bzabek.sms.protection.subscription.SubscribeRequest;
import bzabek.sms.protection.subscription.UnsubscribeRequest;
import bzabek.sms.protection.verifcation.SmsRequest;
import bzabek.sms.protection.verifcation.SmsResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTestIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("public")
            .withUsername("test")
            .withPassword("test");
    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private final String subscriberNumber = "123456789";
    private final String attackerNumber = "987654321";
    private final SubscribeRequest SUBSCRIBE_REQ = new SubscribeRequest(subscriberNumber);
    private final UnsubscribeRequest UNSUBSCRIBE_REQ = new UnsubscribeRequest(subscriberNumber);
    private final SmsRequest TEST_SMS_REQ = new SmsRequest(attackerNumber, subscriberNumber, "Hello, visit: http://test.com");
    @LocalServerPort
    public int port;
    @Autowired
    public TestRestTemplate restTemplate;
    @Autowired
    public DSLContext dslContext;
    private String appAddress;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database.url", postgres::getJdbcUrl);
        registry.add("app.database.username", postgres::getUsername);
        registry.add("app.database.password", postgres::getPassword);
        registry.add("app.web-client.url", () -> wireMockServer.baseUrl());
        registry.add("app.web-client.apiKey", () -> "test-api-key");
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        appAddress = "http://localhost:" + port + "/api/sms/";
        dslContext.createTable("subscriptions")
                .column("phone_number", SQLDataType.VARCHAR(11).nullable(false))
                .column("is_subscribed", SQLDataType.BOOLEAN.nullable(false))
                .constraints(
                        DSL.constraint("pk_phone_number").primaryKey("phone_number")
                )
                .execute();
    }

    @Test
    void smokeTest() {
        // given external service stub that verifies as dangerous
        riskIs("VERY_HIGH");

        // 1. when user is not yet subscribed
        var verificationResponse = sendVerifySmsRequest();
        isOk(verificationResponse);
        // service do not process the uri and assumes it's safe
        isSafe(verificationResponse);

        // 2. when user subscribes
        var subscribeResponse = sendSubscribeRequest();
        isOk(subscribeResponse);

        verificationResponse = sendVerifySmsRequest();
        isOk(verificationResponse);
        // then service correctly verifies danger
        isNotSafe(verificationResponse);

        // 3. when user disables the feature
        var unsubscribeResponse = sendUnsubscribeRequest();
        isOk(unsubscribeResponse);

        verificationResponse = sendVerifySmsRequest();
        isOk(verificationResponse);
        // service do not process the uri and assumes it's safe
        isSafe(verificationResponse);
    }

    private void isOk(ResponseEntity<?> response) {
        assertEquals(200, response.getStatusCode().value());
    }

    private void isSafe(ResponseEntity<SmsResponse> response) {
        assertTrue(response.getBody().isSafe());
    }

    private void isNotSafe(ResponseEntity<SmsResponse> response) {
        assertFalse(response.getBody().isSafe());
    }

    private ResponseEntity<Void> sendSubscribeRequest() {
        String url = appAddress + "subscribe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscribeRequest> entity = new HttpEntity<>(SUBSCRIBE_REQ, headers);

        return restTemplate.postForEntity(url, entity, Void.class);
    }

    private ResponseEntity<Void> sendUnsubscribeRequest() {
        String url = appAddress + "unsubscribe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UnsubscribeRequest> entity = new HttpEntity<>(UNSUBSCRIBE_REQ, headers);

        return restTemplate.postForEntity(url, entity, Void.class);
    }

    private ResponseEntity<SmsResponse> sendVerifySmsRequest() {
        String url = appAddress + "check-phishing";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SmsRequest> entity = new HttpEntity<>(TEST_SMS_REQ, headers);

        return restTemplate.postForEntity(url, entity, SmsResponse.class);
    }

    private void riskIs(String confidenceLevel) {
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/"))
                .withHeader("X-goog-api-key", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                                    {
                                      "scores": [
                                        {                                  
                                          "threatType": "SOCIAL_ENGINEERING",
                                          "confidenceLevel": "%s"
                                        }
                                      ]
                                    }
                                """, confidenceLevel))));
    }
}
