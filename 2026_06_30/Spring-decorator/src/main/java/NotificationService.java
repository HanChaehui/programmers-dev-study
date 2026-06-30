public class NotificationService {
    private final NotificationSender sender;   // 인터페이스 타입!

    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String to, String message) {
        // TODO: sender 를 이용해 알림을 보낸다
        sender.send(to, message);
    }
}