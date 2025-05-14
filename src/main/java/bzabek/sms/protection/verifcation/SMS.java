package bzabek.sms.protection.verifcation;

public record SMS(String sender, String recipient, String message) {

    public static SMS fromRestRequest(SmsRequest smsRequest) {
        return new SMS(smsRequest.sender(), smsRequest.recipient(), smsRequest.message());
    }
}
