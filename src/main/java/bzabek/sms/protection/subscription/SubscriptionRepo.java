package bzabek.sms.protection.subscription;

import bzabek.sms.protection.exceptions.SubscriptionsDatabaseException;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.example.jooq.generated.tables.Subscriptions.SUBSCRIPTIONS;

@Component
class SubscriptionRepo {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRepo.class);
    private final DSLContext dslContext;

    SubscriptionRepo(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * Retrieves the subscription status for a given phone number.
     *
     * @return {@code true} if the phone number is subscribed,
     * {@code false} if not subscribed or if an error occurs
     **/
    boolean fetchIsSubscribed(String phoneNumber) {
        logger.debug("Attempting to fetch users subscription info, phone number: [{}]", phoneNumber);
        try {
            return dslContext.select(SUBSCRIPTIONS.IS_SUBSCRIBED)
                    .from(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.PHONE_NUMBER.eq(phoneNumber))
                    .fetchOptional()
                    .map(record -> record.get(SUBSCRIPTIONS.IS_SUBSCRIBED))
                    .orElse(false);

        } catch (Exception e) {
            throw new SubscriptionsDatabaseException("Failed to fetch users subscriptions info from database.", e);
        }
    }

    void insert(String phoneNumber, boolean isSubscribed) {
        logger.debug("Attempting to update users subscription. Phone number: [{}], as [{}]", phoneNumber, isSubscribed);
        try {
            dslContext.insertInto(SUBSCRIPTIONS)
                    .set(SUBSCRIPTIONS.PHONE_NUMBER, phoneNumber)
                    .set(SUBSCRIPTIONS.IS_SUBSCRIBED, isSubscribed)
                    .onDuplicateKeyUpdate()
                    .set(SUBSCRIPTIONS.IS_SUBSCRIBED, isSubscribed)
                    .execute();
        } catch (Exception e) {
            throw new SubscriptionsDatabaseException("Failed to update users subscriptions info.", e);
        }
    }
}
