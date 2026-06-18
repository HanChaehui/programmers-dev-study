import java.util.Scanner;

public class Start {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        AccountBook book = new AccountBookImpl();

        while(true){
            // 옵션 출력
            book.printMenu();
            // 번호 입력
            int num = Integer.parseInt(sc.nextLine());
            switch(num){
                case 1:
                    System.out.println("날짜 입력 (예: 2024-09-04) > ");
                    System.out.println("항목 이름 > ");
                    System.out.println("금액 > ");
                    System.out.println("더 추가할까요? (y/n) > ");
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 번호입니다.");
            }
        }
    }
}
