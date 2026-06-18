import java.util.Scanner;

public class Main {

    public static class PrintDash extends Thread {
        @Override
        public void run(){
            for(int i = 0; i < 300; i ++){
                System.out.print("-");
            }
        }
    }

    public static class PrintBar extends Thread {
        @Override
        public void run(){
            for(int i = 0; i < 300; i ++){
                System.out.print("|");
            }
        }
    }

    public static class SleepThread extends Thread {
        @Override
        public void run(){
            for (int i = 0; i < 300; i++) System.out.print("-");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("종료");
        }
    }

    public static class CountThread extends Thread {
        @Override
        public void run(){
            int i = 10;
            // 인터럽트 요청 오면 멈춤
            while(i != 0 && !isInterrupted()) {
                System.out.println(i--);
                for (long x = 0; x < 2_500_000_000L; x++) ;  // 시간 지연(busy)
            }
            System.out.println("카운트가 종료되었습니다.");
        }
    }

    public static class CountSleepThread extends Thread {
        @Override
        public void run() {
            int i = 10;
            while (i != 0 && !isInterrupted()) {
                System.out.println(i--);
                try {
                    Thread.sleep(2000);   // 자고 있는 동안 interrupt가 오면?
                } catch (InterruptedException e) {
                    System.out.println("자다가 깨어남! (InterruptedException)");
                    break;   // 깨어나면 반복 종료
                }
            }
            System.out.println("카운트가 종료되었습니다.");
        }
    }

    public static class YieldThread extends Thread {
        private String name;
        public YieldThread(String name) { this.name = name; }
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(name + " 실행 중. 반복: " + i);
                Thread.yield();   // 남은 실행시간 양보(힌트)
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        }
    }

    public static class ManyPrintThread extends Thread {
        private char print;
        public ManyPrintThread(char print) {
            this.print = print;
        }
        @Override
        public void run() {
            int i = 0;
            for(i = 0; i < 30; i++) {
                System.out.print(print);
                for (long x = 0; x < 2_500_000_000L; x++) ;  // 시간 지연(busy)
            }
            System.out.print(i);
        }
    }

    public static void exam1() {
        new PrintDash().start();
        new PrintBar().start();
    }

    public static void exam2() {
        new SleepThread().start();
    }

    public static void exam3() {
        CountThread t1 = new CountThread();
        t1.start();
        new Scanner(System.in).nextLine();   // 아무 값 입력 대기
        t1.interrupt();                       // 멈춰달라 요청
    }

    public static void exam4() {
        CountSleepThread t1 = new CountSleepThread();
        t1.start();
        new Scanner(System.in).nextLine();   // 아무 값 입력 대기
        t1.interrupt();                       // 멈춰달라 요청
    }

    public static void exam5() {
        new YieldThread("스레드1").start();
        new YieldThread("스레드2").start();
    }

    public static void exam6() {
        ManyPrintThread t1 = new ManyPrintThread('-');
        ManyPrintThread t2 = new ManyPrintThread('|');
        t1.start();
        t2.start();

        long start = System.currentTimeMillis();
        try {
            t1.join();   // t1이 끝날 때까지 main이 기다림
            t2.join();   // t2도
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("\n소요시간: " + (System.currentTimeMillis() - start) + "ms");
    }


    public static void main(String[] args) {
        exam6();
    }
}
