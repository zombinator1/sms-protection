package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.client.WebRiskClient;
import bzabek.sms.protection.client.WebRiskResponse;
import bzabek.sms.protection.subscription.SubscriptionController;
import bzabek.sms.protection.subscription.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SmsSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final UrlExtractor urlExtractor;
    private final SubscriptionService subscriptionService;
    private final WebRiskClient webRiskClient;
    private final RiskResolver riskResolver;

    SmsSecurityService(UrlExtractor urlExtractor,
                       SubscriptionService subscriptionService,
                       WebRiskClient webRiskClient,
                       RiskResolver riskResolver) {
        this.urlExtractor = urlExtractor;
        this.subscriptionService = subscriptionService;
        this.webRiskClient = webRiskClient;
        this.riskResolver = riskResolver;
    }

    /**
     * @param sms incoming SMS
     * @return return whether the SMS can be further processed. If the user did not subscribe to the feature then his
     * SMS is not checked and processed further.
     */
    public boolean isPhishingSafe(SMS sms) {
        List<String> uris = urlExtractor.extractUrls(sms.message());
        if (uris.isEmpty()) {
            return true;
        }
        boolean isSubscribed = subscriptionService.isSubscribed(sms.recipient());
        if (!isSubscribed) {
            return true;
        }
        for (String uri : uris) {
            WebRiskResponse risk = webRiskClient.verifyUri(uri);
            if (!riskResolver.isRiskAcceptable(risk)) {
                logger.info("SMS verified as dangerous: [{}]", sms);
                return false;
            }
        }
        return true;
    }
}
