public class Main {
    static void main() {

        Journal j = new Journal();
        JournalSaver js = new JournalSaver();
        j.add("오늘은 자바를 배웠다");
        j.add("SOLID는 어렵지만 재밌다");
        System.out.println("===== SRP: 단일 책임 =====");
        js.print(j);
        System.out.println();

        DiscountPolicy[] p = {new BasicDiscount(), new GoldDiscount(), new VipDiscount()};
        String[] name = {"일반", "골드", "VIP"};
        int price = 10000;
        System.out.println("===== OCP: 개방-폐쇄 =====");
        for(int i = 0; i < p.length; i++) {
            System.out.printf("%s 회원 -> %d원\n", name[i], p[i].discount(price));
        }
        System.out.println();

        Sparrow sparrow = new Sparrow();
        Penguin penguin = new Penguin();
        System.out.println("===== LSP: 리스코프 치환 =====");
        sparrow.eat();
        penguin.eat();
        sparrow.fly();
        penguin.swim();
        System.out.println();

        SimplePrinter sp = new SimplePrinter();
        SmartMachine sm = new SmartMachine();
        System.out.println("===== ISP: 인터페이스 분리 =====");
        sp.print();
        sm.print();
        sm.scan();
        System.out.println();

        System.out.println("===== DIP: 의존관계 역전 =====");
        new NotificationService(new EmailSender()).notifyUser("주문이 완료되었습니다");
        new NotificationService(new SmsSender()).notifyUser("주문이 완료되었습니다");
        System.out.println();

    }
}
