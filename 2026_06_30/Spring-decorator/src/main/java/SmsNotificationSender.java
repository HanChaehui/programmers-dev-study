public class SmsNotificationSender implements NotificationSender{
    @Override
    public void send(String to, String message) {
        System.out.printf("[SMS] to=%s : %s%n", to, message);
    }
}
