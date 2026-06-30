public class Start {
    static void main() {
        //NotificationSender sender1 = new TimingNotificationSender(new LoggingNotificationSender(new RetryNotificationSender(new FlakyEmailSender())));
        //new NotificationService(sender1).notifyUser("user@test.com", "안녕하세요");

        NotificationSender sender2 = new TimingNotificationSender(new RetryNotificationSender(new LoggingNotificationSender(new FlakyEmailSender())));
        new NotificationService(sender2).notifyUser("user@test.com", "안녕하세요");
    }
}
