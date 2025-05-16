package bzabek.sms.protection.client;

import bzabek.sms.protection.model.ConfidenceLevel;
import bzabek.sms.protection.model.ThreatType;

public record Score(ThreatType threatType, ConfidenceLevel confidenceLevel) {
}
