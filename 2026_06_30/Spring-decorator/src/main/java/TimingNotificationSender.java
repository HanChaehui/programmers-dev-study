public class TimingNotificationSender implements NotificationSender{
    private NotificationSender delegate;

    public TimingNotificationSender(NotificationSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(String to, String message) {
        long startTime = System.currentTimeMillis();
        try {
            delegate.send(to, message);
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("발송에 걸린 시간: " + (endTime - startTime) + "ms");
        }
    }
}
