import java.util.Scanner;

public class new_vending {
    static final int COKE = 500, CIDER = 700, FANTA = 300, WATER = 200;
    // 메뉴 출력
    public static void printMenu(int totalMoney) {
        System.out.println("================================= 자판기 ================================");
        System.out.println("[1]콜라-500원 [2]사이다-700원 [3]환타-300원 [4]물-200원 [5]돈넣기 [6]종료");
        System.out.println("현재 금액 : " + totalMoney + "원");
        System.out.println("==========================================================================");
    }
    // 사용자 입력 받기
    public static int getChoice(){
        System.out.println("원하는 번호를 입력하세요");
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }
    // 잔액 계산
    public static int calcMoney(int totalMoney, int price){
        int result = totalMoney - price;
        if (result < 0){
            return -1;
        }
        return result;
    }
    // 입금
    public static int inputMoney(){
        System.out.println("원하시는 금액을 넣어주세요.");
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }

    public static void main (String[] args){

        int insertMoney = 0;
        int num = 0;
        int totalMoney = 0;

        while (true){

            // 메뉴 출력
            printMenu(totalMoney);

            // 번호 입력
            num = getChoice();

            switch(num){
                case 1:
                    System.out.println("[1]콜라를 선택하셨습니다.");
                    if(calcMoney(totalMoney, COKE) == -1){
                        System.out.println("잔액이 부족합니다.");
                    }
                    else{
                        totalMoney = calcMoney(totalMoney, COKE);
                        System.out.printf("콜라 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                    }
                    break;
                case 2:
                    System.out.println("[2]사이다를 선택하셨습니다.");
                    if(calcMoney(totalMoney, CIDER) == -1){
                        System.out.println("잔액이 부족합니다.");
                    }
                    else{
                        totalMoney = calcMoney(totalMoney, CIDER);
                        System.out.printf("사이다 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                    }
                    break;
                case 3:
                    System.out.println("[3]환타를 선택하셨습니다.");
                    if(calcMoney(totalMoney, FANTA) == -1){
                        System.out.println("잔액이 부족합니다.");
                    }
                    else{
                        totalMoney = calcMoney(totalMoney, FANTA);
                        System.out.printf("환타 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                    }
                    break;
                case 4:
                    System.out.println("[4]물을 선택하셨습니다.");
                    if(calcMoney(totalMoney, WATER) == -1){
                        System.out.println("잔액이 부족합니다.");
                    }
                    else{
                        totalMoney = calcMoney(totalMoney, WATER);
                        System.out.printf("물 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                    }
                    break;
                case 5:
                    totalMoney += inputMoney();
                    break;
                case 6:
                    System.out.println("구매를 종료합니다.");
                    return;
                default:
                    System.out.println("메뉴판의 번호를 다시 입력해주세요.");
            }
        }
    }
}
