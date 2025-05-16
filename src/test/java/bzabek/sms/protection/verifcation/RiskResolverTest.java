package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.client.Score;
import bzabek.sms.protection.client.WebRiskResponse;
import bzabek.sms.protection.model.ConfidenceLevel;
import bzabek.sms.protection.model.ThreatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static bzabek.sms.protection.model.ConfidenceLevel.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskResolverTest {

    private static Stream<Arguments> acceptablePairs() {
        return Stream.of(
                /* acceptableLevel | risk level */
                Arguments.of(SAFE, SAFE),
                Arguments.of(LOW, SAFE),
                Arguments.of(MEDIUM, MEDIUM)
        );
    }

    private static Stream<Arguments> unsafeConfidenceLevels() {
        return Stream.of(
                Arguments.of(LOW),
                Arguments.of(MEDIUM),
                Arguments.of(HIGH),
                Arguments.of(HIGHER),
                Arguments.of(VERY_HIGH),
                Arguments.of(EXTREMELY_HIGH),
                Arguments.of(CONFIDENCE_LEVEL_UNSPECIFIED)
        );
    }

    @ParameterizedTest
    @MethodSource("acceptablePairs")
    void shouldAccept(ConfidenceLevel acceptableLevel, ConfidenceLevel riskLevel) {
        RiskResolver resolver = new RiskResolver(acceptableLevel.name());

        WebRiskResponse response = new WebRiskResponse(List.of(
                new Score(ThreatType.SOCIAL_ENGINEERING, riskLevel)
        ));

        assertTrue(resolver.isRiskAcceptable(response));
    }

    @ParameterizedTest
    @MethodSource("unsafeConfidenceLevels")
    void shouldReject(ConfidenceLevel confidenceLevel) {
        RiskResolver resolver = new RiskResolver(SAFE.name());

        WebRiskResponse response = new WebRiskResponse(List.of(
                new Score(ThreatType.SOCIAL_ENGINEERING, confidenceLevel)
        ));

        assertFalse(resolver.isRiskAcceptable(response));
    }

    @Test
    void shouldReject_MultipleScores() {
        RiskResolver resolver = new RiskResolver(LOW.name());

        WebRiskResponse response = new WebRiskResponse(List.of(
                new Score(ThreatType.SOCIAL_ENGINEERING, SAFE),
                new Score(ThreatType.SOCIAL_ENGINEERING, HIGH),
                new Score(ThreatType.SOCIAL_ENGINEERING, LOW)
        ));

        assertFalse(resolver.isRiskAcceptable(response));
    }

    @Test
    void shouldResolveDefaultAcceptableRiskLevel() {
        RiskResolver resolver = new RiskResolver(null);

        WebRiskResponse response1 = new WebRiskResponse(List.of(
                new Score(ThreatType.SOCIAL_ENGINEERING, SAFE)
        ));
        // SAFE level is acceptable
        assertTrue(resolver.isRiskAcceptable(response1));

        WebRiskResponse response2 = new WebRiskResponse(List.of(
                new Score(ThreatType.SOCIAL_ENGINEERING, LOW)
        ));
        // LOW level is rejected
        assertFalse(resolver.isRiskAcceptable(response2));
    }
}
