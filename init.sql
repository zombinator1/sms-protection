CREATE TABLE IF NOT EXISTS subscriptions (
    phone_number VARCHAR(11) PRIMARY KEY,
    is_subscribed BOOL
);

INSERT INTO subscriptions(phone_number, is_subscribed) VALUES("111222333", true);
INSERT INTO subscriptions(phone_number, is_subscribed) VALUES("987123456", false);
