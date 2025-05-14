package bzabek.sms.protection.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionService {

    @Autowired
    private final SubscriptionRepo subscriptionRepo;

    SubscriptionService(SubscriptionRepo subscriptionRepo) {
        this.subscriptionRepo = subscriptionRepo;
    }

    public boolean isSubscribed(String phoneNumber) {
        return subscriptionRepo.fetchIsSubscribed(phoneNumber);
    }

    public void subscribe(String phoneNumber) {
        subscriptionRepo.insert(phoneNumber, true);
    }

    public void unsubscribe(String phoneNumber) {
        subscriptionRepo.insert(phoneNumber, false);
    }
}
