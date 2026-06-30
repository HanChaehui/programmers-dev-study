public class RetryNotificationSender implements NotificationSender{
    private NotificationSender delegate;

    public RetryNotificationSender(NotificationSender delegate) {
        this.delegate = delegate;
    }
    // FlakyNotificationSender 구현체 넣어 테스트
    @Override
    public void send(String to, String message) {
        int maxRetry = 3;
        for(int i = 0; i < maxRetry; i++) {
            try {
                delegate.send(to, message);
                return;
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("재시도 " + maxRetry + "회 모두 실패");
        throw new RuntimeException("재시도 " + maxRetry + "회 모두 실패");
    }
}
