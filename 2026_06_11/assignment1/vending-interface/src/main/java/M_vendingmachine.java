public class M_vendingmachine {
    private int totalMoney;
    private M_drink[] drinks;

    public M_vendingmachine(){
        totalMoney = 0;
        drinks = new M_drink[] {
                new M_coke(),
                new M_cider(),
                new M_fanta(),
                new M_water()
        };
    }

    void insertMoney(int money){
        totalMoney += money;
        System.out.println(money + "원을 넣었습니다.");
    }

    void buy(int menu){
        if(drinks[menu-1].getPrice() > totalMoney){
            System.out.println("잔돈이 부족합니다.");
        }
        else{
            totalMoney -= drinks[menu-1].getPrice();
            drinks[menu-1].dispense();
        }
    }

    int returnMoney(){
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
