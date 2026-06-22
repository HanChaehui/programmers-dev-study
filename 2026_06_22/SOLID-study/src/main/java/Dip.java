public class Dip {
}

class NotificationService {
    private MessageSender ms;

    NotificationService(MessageSender ms) {
        this.ms = ms;
    }

    void notifyUser(String msg) { ms.send(msg); }

}

interface MessageSender {
    void send(String msg);
}

class EmailSender implements MessageSender {

    @Override
    public void send(String msg) {
        System.out.println("[이메일] " + msg);
    }
}

class SmsSender implements MessageSender {

    @Override
    public void send(String msg) {
        System.out.println("[SMS] " + msg);
    }
}