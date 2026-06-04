import java.util.Scanner;

public class vending_machine {

    // 조건문 or while true 사용
    static final int COKE = 500, CIDER = 700, FANTA = 300, WATER = 200;

    public static void printMenu(int totalMoney) {
        System.out.println("================================= 자판기 ================================");
        System.out.println("[1]콜라-500원 [2]사이다-700원 [3]환타-300원 [4]물-200원 [5]돈넣기 [6]종료");
        System.out.println("현재 금액 : " + totalMoney + "원");
        System.out.println("==========================================================================");
    }

    public static void main (String[] args){

        Scanner sc = new Scanner(System.in);
        int insertMoney = 0;
        int num = 0;
        int totalMoney = 0;

        while (true){

            // 메뉴 출력
            printMenu(totalMoney);

            // 번호 입력
            System.out.println("원하는 번호를 입력하세요");
            num = sc.nextInt();

            // 종료 조건
            if (num == 6) {
                System.out.println("구매를 종료합니다.");
                break;
            }
            // 돈 넣기
            else if (num == 5){
                System.out.println("원하시는 금액을 넣어주세요.");
                insertMoney = sc.nextInt();
                // 현재 금액 계산
                totalMoney += insertMoney;
                continue;
            }
            // 종료가 아닌 경우
            else if (num == 1){
                System.out.println("[1]콜라를 선택하셨습니다.");
                // 잔액 부족 시 차액 계산 생략 및 메뉴 재출력
                if(totalMoney < COKE){
                    System.out.println("잔액이 부족합니다.");
                    continue;
                }
                else{
                    // 차액 계산
                    totalMoney -= COKE;
                    System.out.printf("콜라 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                }
            }
            else if (num == 2){
                System.out.println("[2]사이다를 선택하셨습니다.");
                if(totalMoney < CIDER){
                    System.out.println("잔액이 부족합니다.");
                    continue;
                }
                else{
                    totalMoney -= CIDER;
                    System.out.printf("사이다 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                }
            }
            else if (num == 3){
                System.out.println("[3]환타를 선택하셨습니다.");
                if(totalMoney < FANTA){
                    System.out.println("잔액이 부족합니다.");
                    continue;
                }
                else{
                    totalMoney -= FANTA;
                    System.out.printf("환타 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                }
            }
            else if (num == 4){
                System.out.println("[4]물을 선택하셨습니다.");
                if(totalMoney < WATER){
                    System.out.println("잔액이 부족합니다.");
                    continue;
                }
                else{
                    totalMoney -= WATER;
                    System.out.printf("물 구매 성공\n현재 %d원 남았습니다.\n", totalMoney);
                }
            }
            else {
                System.out.println("메뉴판의 번호를 다시 입력해주세요.");
            }

        }
    }
}
