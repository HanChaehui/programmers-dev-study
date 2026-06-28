import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static void main() {

        Operation add1 = new Operation() {
            @Override
            public int apply(int a, int b) {
                return a + b;
            }
        };

        Operation add2 = (a, b) -> a + b;
        Operation add3 = (int a, int b) -> { return a + b; };
        Operation add4 = (a, b) -> { return a + b; };

        Operation sub = (a, b) -> a - b;
        Operation mul = (a, b) -> a * b;
        System.out.println();
        System.out.println("===== 1. 익명 클래스 vs 람다 (같은 동작) =====");
        System.out.println("익명 클래스 add: " + add1.apply(3, 4));
        System.out.println("람다 add: " + add2.apply(3, 4));
        System.out.println();

        System.out.println("===== 2. 람다로 만든 연산들 =====");
        System.out.println("3 + 4 = " + add2.apply(3, 4));
        System.out.println("9 - 2 = " + sub.apply(9, 2));
        System.out.println("3 * 5 = " + mul.apply(3, 5));
        System.out.println();

        Runnable runnable = () -> System.out.println("안녕하세요, 람다!");
        Printer printer = msg -> System.out.println(msg);
        System.out.println("===== 3. 매개변수 개수별 람다 =====");
        System.out.print("(0개) ");
        runnable.run();
        System.out.print("(1개) ");
        printer.print("시작합니다");
        System.out.println("(2개) 10 + 20 = " + add2.apply(10, 20));
        System.out.println();

        ArrayList<String> names = new ArrayList<>(Arrays.asList("가나다", "가", "라마"));
        System.out.println("===== 4. 실전: Comparator로 길이순 정렬 =====");
        System.out.println("정렬 전: " + names);
        names.sort((s1, s2) -> s1.length() - s2.length());
        System.out.println("정렬 후: " + names);
        System.out.println();



    }
}
