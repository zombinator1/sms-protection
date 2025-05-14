package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.client.Score;
import bzabek.sms.protection.client.WebRiskResponse;
import bzabek.sms.protection.model.ConfidenceLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static bzabek.sms.protection.model.ConfidenceLevel.CONFIDENCE_LEVEL_UNSPECIFIED;
import static bzabek.sms.protection.model.ConfidenceLevel.SAFE;

@Component
class RiskResolver {

    private final ConfidenceLevel acceptableRisk;

    RiskResolver(@Value("${app.acceptableRisk}") String acceptableRisk) {
        this.acceptableRisk = acceptableRisk == null ? SAFE : ConfidenceLevel.valueOf(acceptableRisk);
    }

    boolean isRiskAcceptable(WebRiskResponse risk) {
        for (Score score : risk.scores()) {
            int riskLevel = score.confidenceLevel().getLevel();
            if (riskLevel > acceptableRisk.getLevel() || riskLevel == CONFIDENCE_LEVEL_UNSPECIFIED.getLevel()) {
                return false;
            }
        }
        return true;
    }
}
