import java.util.Scanner;

public class L_vendingmachine {

    static Scanner sc = new Scanner(System.in);

    private int totalMoney;
    private L_drink[] drinks;

    public L_vendingmachine(){
        totalMoney = 0;
        drinks = new L_drink[] {
                new L_coke(),
                new L_cider(),
                new L_fanta(),
                new L_water()
        };
    }

    // 돈 넣기 : insertMoney
    public void insertMoney(int money) {
        totalMoney += money;
        System.out.println(money + "원을 넣었습니다.");
    }

    // 음료 구매 : buy - 메뉴 번호 (1~4)로 선택
    public void buy(int menuNumber) {
        if (drinks[menuNumber-1].getPrice() > totalMoney){
            System.out.println("잔돈이 부족합니다.");
        }
        else{
            totalMoney -= drinks[menuNumber-1].price;
            drinks[menuNumber-1].dispense();
        }
    }

    // 종료 시 잔돈 반환
    public int returnMoney() {
        int left = totalMoney;
        totalMoney = 0;
        return left;
    }

    // 메뉴 출력
    public void printMenu(){
        System.out.println("============== 자판기 ==============");
        System.out.println("[1]콜라 : 500  [2]사이다 : 500  [3]환타 : 300  [4]물 : 200");
        System.out.println("[5]돈 넣기  [6]종료");
        System.out.println("현재 금액 : " + totalMoney);
        System.out.println("====================================");
    }
}
