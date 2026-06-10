import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("반려동물의 이름을 지어주세요: ");
        String name = sc.nextLine();

        Pet pet = new Pet(name);
        System.out.println(name + "이(가) 태어났어요!");
        pet.showStatus();
        System.out.println();

        int menu = 0;

        while(true){
            System.out.print("무엇을 할까요? [1]먹이주기 [2]놀아주기 [3]상태보기 [4]종료\n> ");
            menu = Integer.parseInt(sc.nextLine());

            switch (menu) {
                case 1:
                    pet.feed();
                    pet.showStatus();
                    break;
                case 2:
                    pet.play();
                    pet.showStatus();
                    break;
                case 3:
                    pet.showStatus();
                    break;
                case 4:
                    System.out.println("안녕!");
                    return;
                default:
                    System.out.println("1~4 중에 골라주세요.");
                    break;
            }
        }
    }
}
