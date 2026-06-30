public class LoggingNotificationSender implements NotificationSender {
    private final NotificationSender delegate;

    public LoggingNotificationSender(NotificationSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(String to, String message) {
        // TODO 1: "발송 시작" 로그 (to 포함)
        // TODO 2: delegate.send(to, message);   ← 실제 발송은 위임
        // TODO 3: "발송 완료" 로그
        System.out.println("[발송 시작] to = " + to);
        delegate.send(to, message);
        System.out.println("[발송 완료]\n");
    }
}