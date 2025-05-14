package bzabek.sms.protection.verifcation;

public record SmsRequest(String sender, String recipient, String message) {
}
