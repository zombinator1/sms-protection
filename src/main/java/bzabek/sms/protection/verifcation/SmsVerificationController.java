package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.model.ThreatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsVerificationController {

    private static final Logger logger = LoggerFactory.getLogger(SmsVerificationController.class);
    private final SmsSecurityService smsSecurityService;

    public SmsVerificationController(SmsSecurityService smsSecurityService) {
        this.smsSecurityService = smsSecurityService;
    }

    @PostMapping("/check-phishing")
    public ResponseEntity<SmsResponse> checkForPhishing(@RequestBody SmsRequest smsRequest) {
        logger.debug("Received /check-phishing request [{}]", smsRequest.toString());

        SMS sms = SMS.fromRestRequest(smsRequest);
        boolean isSafe = smsSecurityService.isPhishingSafe(sms);
        String reason = isSafe ? ThreatType.SAFE.name() : ThreatType.SOCIAL_ENGINEERING.name();

        return ResponseEntity.ok(
                new SmsResponse(isSafe, reason)
        );
    }
}
