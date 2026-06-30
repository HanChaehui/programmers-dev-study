// EmailNotificationSender.java  ── 실제 구현체 예시 (그대로 사용)
public class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(String to, String message) {
        // 실제론 메일 서버 호출. 과제에서는 콘솔 출력으로 흉내.
        System.out.printf("[EMAIL] to=%s : %s%n", to, message);
    }
}