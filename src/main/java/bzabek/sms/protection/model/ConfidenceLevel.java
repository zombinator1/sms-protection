package bzabek.sms.protection.model;

/**
 * Confidence level in how risky a URI is predicted to be.
 */
public enum ConfidenceLevel {

    /**
     * Default type that indicates this enum hasn't been specified.
     * This is not a valid ConfidenceLevel, another type must be specified instead.
     */
    CONFIDENCE_LEVEL_UNSPECIFIED(-1),
    /**
     * URIs that are considered as safe. In most cases, there is no need to classify
     * or recheck their confidence levels again in the near future.
     */
    SAFE(0),

    /**
     * Low confidence that the URI given is risky for this threat type.
     */
    LOW(1),


    /**
     * Medium confidence that the URI given is risky for this threat type.
     * The URI is considered as mildly suspicious (i.e., 1000x time more likely
     * to be malicious than a typical random URI). This confidence level is not
     * recommended for direct enforcements. It is useful for finding suspicious
     * leads and/or triggering further classifications.
     */
    MEDIUM(2),

    /**
     * High (>10%) confidence that the URI given is risky for this threat type.
     * This confidence level is not recommended for direct enforcements. You can
     * combine this confidence level with your own suspicious signals for
     * enforcements after evaluation and verification.
     */
    HIGH(3),

    /**
     * Higher (>90%) confidence that the URI given is risky for this threat type
     * (with no egregious false positives). You can use this confidence level for
     * your own enforcements after evaluation and verification. When compared to
     * EXTREMELY_HIGH and VERY_HIGH, this increases the coverage (up to 90% more
     * in some cases) of malicious URIs but with a small amount of potential false positives.
     */
    HIGHER(4),

    /**
     * Very high (>99%) confidence that the URI given is risky for this threat type
     * (with no egregious false positives). This level is recommended for blocking
     * malicious URIs from your platforms after evaluation and verification.
     */
    VERY_HIGH(5),

    /**
     * Extremely high confidence that the URI given is risky for this threat type.
     * You can use this confidence level for your own enforcements. For example,
     * blocking malicious URIs from your platforms.
     */
    EXTREMELY_HIGH(6);


    private final int level;

    ConfidenceLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
