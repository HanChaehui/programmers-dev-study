public class Main {
    static int badMismatch = 0;
    static int goodMismatch = 0;

    static void main() throws InterruptedException {
        int N = 30;

        Thread[] t1 = new Thread[N];
        for (int i = 0; i < N; i++) {
            final String myName = "손님" + i;
            t1[i] = new Thread(() -> {
                String r = GreetingServiceBad.getInstance().greet(myName);
                if (!r.equals(myName)) {
                    synchronized (Main.class) { badMismatch++; }
                }
            });
        }
        for (Thread t : t1) t.start();   // 동시에 출발
        for (Thread t : t1) t.join();    // 다 끝날 때까지 대기


        Thread[] t2 = new Thread[N];
        for (int i = 0; i < N; i++) {
            final String myName = "손님" + i;
            t2[i] = new Thread(() -> {
                String r = GreetingServiceGood.getInstance().greet(myName);
                if (!r.equals(myName)) {
                    synchronized (Main.class) { goodMismatch++; }
                }
            });
        }
        for (Thread t : t2) t.start();
        for (Thread t : t2) t.join();

        System.out.println("===== 같은 싱글톤을 30개 스레드가 동시에 사용 =====");
        System.out.println("[필드에 저장] 엉킴: " + badMismatch + " / " + N);
        System.out.println("[파라미터로]  엉킴: " + goodMismatch + " / " + N);
        System.out.println();

        UserDao user1 = UserDao.getInstance();
        UserDao user2 = UserDao.getInstance();
        System.out.println("===== 필드에 둬도 되는 것: 다른 싱글톤 참조 =====");
        System.out.println(user1.findUser("Kim"));
        System.out.println(user2.findUser("lee"));
        System.out.print("같은 DAO인가? ");
        System.out.println(user1 == user2);
        System.out.println();
    }
}
