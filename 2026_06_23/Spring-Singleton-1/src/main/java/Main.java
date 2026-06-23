public class Main {
    static void main() {
        System.out.println("===== 1. 싱글톤 없이: 번호표 두 대 (버그!) =====");
        NaiveTicketMachine a = new NaiveTicketMachine();
        NaiveTicketMachine b = new NaiveTicketMachine();
        System.out.println("A 기계가 발급: " + a.issue() + "번");
        System.out.println("B 기계가 발급: " + b.issue() + "번");
        System.out.println("A 기계가 발급: " + a.issue() + "번");
        System.out.println("B 기계가 발급: " + b.issue() + "번");
        System.out.println();

        System.out.println("===== 2. 싱글톤 적용: 번호표는 하나뿐 =====");
        TicketMachine w1 = TicketMachine.getInstance();
        TicketMachine w2 = TicketMachine.getInstance();
        TicketMachine w3 = TicketMachine.getInstance();
        System.out.println("1번 창구가 발급: " + w1.issue() + "번");
        System.out.println("2번 창구가 발급: " + w2.issue() + "번");
        System.out.println("1번 창구가 발급: " + w1.issue() + "번");
        System.out.println("3번 창구가 발급: " + w3.issue() + "번");
        System.out.print("같은 기계인가? ");
        System.out.println(w1 == w2);
        System.out.println();

        System.out.println("===== 3. lazy 초기화 (설정 관리자) =====");
        Settings s1 = Settings.getInstance();
        Settings s2 = Settings.getInstance();
        System.out.println("앱 설정 - 테마: " + s1.getTheme());
        System.out.println("앱 설정 - 테마: " + s2.getTheme());
        s1.setTheme("white");
        System.out.println("앱 설정 - 테마: " + s1.getTheme());
        System.out.println("앱 설정 - 테마: " + s2.getTheme());
        System.out.print("같은 설정 객체인가? ");
        System.out.println(s1 == s2);
        System.out.println();

    }
}
