package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.client.Score;
import bzabek.sms.protection.client.WebRiskClient;
import bzabek.sms.protection.client.WebRiskResponse;
import bzabek.sms.protection.subscription.SubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static bzabek.sms.protection.model.ConfidenceLevel.SAFE;
import static bzabek.sms.protection.model.ThreatType.SOCIAL_ENGINEERING;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SmsSecurityServiceTest {

    static UrlExtractor urlExtractor;
    static SubscriptionService subscriptionService;
    static WebRiskClient webRiskClient;
    static RiskResolver riskResolver;
    static SmsSecurityService securityService;
    WebRiskResponse SAFE_RESPONSE = new WebRiskResponse(List.of(new Score(SOCIAL_ENGINEERING, SAFE)));

    @BeforeAll
    static void setUp() {
        urlExtractor = new UrlExtractor();
        subscriptionService = Mockito.mock(SubscriptionService.class);
        webRiskClient = Mockito.mock(WebRiskClient.class);
        riskResolver = new RiskResolver(SAFE.name());

        securityService = new SmsSecurityService(urlExtractor, subscriptionService, webRiskClient, riskResolver);
    }

    @AfterEach
    void cleanup() {
        Mockito.reset(subscriptionService, webRiskClient);
    }

    @Test
    void hapyPath() {
        // given
        String url = "https://example.com";
        String recipient = "123123123";
        String message = String.format("Visit %s for more info.", url);
        SMS sms = new SMS("987987987", recipient, message);

        given(webRiskClient.verifyUri(url)).willReturn(SAFE_RESPONSE);
        given(subscriptionService.isSubscribed(recipient)).willReturn(true);

        // when
        boolean isSafe = securityService.isPhishingSafe(sms);

        // then
        assertTrue(isSafe);
        // two URLs were verified
        verify(webRiskClient, times(1)).verifyUri(any());
        // sender number was checked only once
        verify(subscriptionService, times(1)).isSubscribed(any());
    }

    @Test
    void shouldPositivelyVerify_NoUrls() {
        // given
        String senderNumber = "123123123";
        String message = "Hi how are you?";
        SMS sms = new SMS(senderNumber, "987987987", message);

        // when
        boolean isSafe = securityService.isPhishingSafe(sms);

        // then
        assertTrue(isSafe);
        // no need to do any processing if there is no URL
        verify(webRiskClient, times(0)).verifyUri(any());
        verify(subscriptionService, times(0)).isSubscribed(any());
    }

    @Test
    void shouldPositivelyVerify_MultipleUrls() {
        // given
        String url1 = "https://example.com";
        String url2 = "https://safewebsite.com";
        String recipient = "123123123";
        String message = String.format("Visit %s and %s for more info.", url1, url2);
        SMS sms = new SMS("987987987", recipient, message);

        given(webRiskClient.verifyUri(url1)).willReturn(SAFE_RESPONSE);
        given(webRiskClient.verifyUri(url2)).willReturn(SAFE_RESPONSE);
        given(subscriptionService.isSubscribed(recipient)).willReturn(true);

        // when
        boolean isSafe = securityService.isPhishingSafe(sms);

        // then
        assertTrue(isSafe);
        // two URLs were verified
        verify(webRiskClient, times(2)).verifyUri(any());
        // sender number was checked only once
        verify(subscriptionService, times(1)).isSubscribed(any());
    }
}
