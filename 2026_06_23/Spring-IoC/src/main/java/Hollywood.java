public class Hollywood {
}

interface ClickListener {
    void onClick();
}

class LikeAction implements ClickListener {

    @Override
    public void onClick() {
        System.out.println("내 코드 실행: 좋아요!");
    }
}

class Button {
    private ClickListener listener;

    void setListener(ClickListener c) {
        this.listener = c;
    }
    void press() {
        System.out.println("[시스템] 버튼이 눌렸습니다");
        listener.onClick();
    }
}