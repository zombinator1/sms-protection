package bzabek.sms.protection.client;

import java.util.List;

public record WebRiskResponse(List<Score> scores) {
}
