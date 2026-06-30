// FlakyEmailSender.java  ── 재시도 테스트용: 처음 두 번은 일부러 실패한다 (그대로 사용)
public class FlakyEmailSender implements NotificationSender {
    private int attempt = 0;
    @Override
    public void send(String to, String message) {
        attempt++;
        if (attempt < 3) {
            throw new RuntimeException("일시적 네트워크 오류 (시도 " + attempt + ")");
        }
        System.out.printf("[EMAIL] (시도 %d 성공) to=%s : %s%n", attempt, to, message);
    }
}