import java.util.Scanner;

public class L_start {
    public static void main(String[] args) {
        L_vendingmachine vending = new L_vendingmachine();
        Scanner sc = new Scanner(System.in);

        while(true){
            vending.printMenu();
            System.out.print("원하는 메뉴를 선택하세요 > ");
            int menu = Integer.parseInt(sc.nextLine());
            switch (menu){
                case 1:
                case 2:
                case 3:
                case 4:
                    vending.buy(menu);
                    break;
                case 5:
                    System.out.print("넣을 금액 > ");
                    int money = Integer.parseInt(sc.nextLine());
                    vending.insertMoney(money);
                    break;
                case 6:
                    System.out.printf("\n잔돈 %d원을 반환되었습니다.\n", vending.returnMoney());
                    return;
                default:
                    System.out.println("잘못 입력하셨습니다. 다시 입력해주세요.");
                    break;
            }
        }
    }
}

