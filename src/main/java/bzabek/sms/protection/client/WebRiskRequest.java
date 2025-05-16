package bzabek.sms.protection.client;

import bzabek.sms.protection.model.ThreatType;

import java.util.List;

record WebRiskRequest(String uri, List<ThreatType> threatType, boolean allowScan) {

    static WebRiskRequest phishing(String uri) {
        return new WebRiskRequest(uri, List.of(ThreatType.SOCIAL_ENGINEERING), true);
    }
}
