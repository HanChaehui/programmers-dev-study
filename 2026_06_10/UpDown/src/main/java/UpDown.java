import java.util.Scanner;
import java.util.Random;


public class UpDown {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();
        int answer = rand.nextInt(100) + 1;

        int guess = -1;
        int count = 0;

        System.out.println("숫자를 맞혀보세요! (1 ~ 100)");

        while(true){
            System.out.print("입력 > ");
            guess = sc.nextInt();
            if (guess < 1 || guess > 100) {
                System.out.println("1~100 사이로 입력해 주세요.");
                continue;
            }
            else if(guess > answer){
                System.out.println("DOWN! 더 작은 수입니다.");
            }
            else if(guess < answer) {
                System.out.println("UP! 더 큰 수입니다.");
            }
            else {
                System.out.printf("정답입니다! %d번 만에 맞혔어요.\n", count);
                break;
            }
            count++;
        }
    }
}
