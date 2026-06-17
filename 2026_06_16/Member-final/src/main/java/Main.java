import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    // 숫자가 아닌 문자 입력 시 -1 반환
    static int readInt(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;   // 잘못된 입력
        }
    }

    public static void main(String[] args) {

        System.out.println("요금제를 선택하세요.");
        System.out.print("[1]Lite:10 [2]Basic:20 [3]Premium:30\n> ");
        PricePlan plan = null;
        while(plan == null){
            int choice = readInt(sc);
            if(choice == -1) System.out.println("숫자를 입력하세요.");
            else if(PricePlan.from(choice) == null) System.out.println("1~3번 중에서 선택하세요.");
            else plan = PricePlan.from(choice);
        }
        MemberManager manager = new MemberManager(plan.getCapacity());
        System.out.println("선택: " + plan + " (정원 " + plan.getCapacity() + ")");

        while(true){
            manager.printMenu();
            int choice = readInt(sc);

            switch (choice){
                case 1:
                    if(manager.isFull()) {
                        System.out.println("정원이 꽉 찼습니다.");
                        break;
                    }
                    System.out.println("[추가] 회원추가");
                    System.out.print("등급 [1]일반 [2]VIP\n> ");
                    int grade = readInt(sc);
                    if(grade == -1) {
                        System.out.println("숫자를 입력해야합니다. 수행 업무 선택으로 돌아갑니다.");
                        break;
                    }
                    else if (grade != 1 && grade != 2) {
                        System.out.println("1~2번 중에서 선택해야합니다. 수행 업무 선택으로 돌아갑니다.");
                        break;
                    }
                    System.out.print("이름 > ");
                    String name = sc.nextLine();
                    System.out.print("이메일 > ");
                    String email = sc.nextLine();
                    System.out.print("연락처 > ");
                    String phone = sc.nextLine();

                    manager.add(grade, name, email, phone);
                    break;
                case 2:
                    System.out.print("[이메일 조회] 조회할 이메일 > ");
                    Member m1 = manager.findByEmail(sc.nextLine());
                    if(m1 != null) m1.printInfo();
                    break;
                case 3:
                    System.out.print("[이름 조회] 조회할 이름 > ");
                    Member m2 = manager.findByName(sc.nextLine());
                    if(m2 != null) m2.printInfo();
                    break;
                case 4:
                    manager.printAll();
                    break;
                case 5:
                    System.out.print("[수정] 수정할 회원의 이메일 > ");
                    String old_email = sc.nextLine();
                    Member m3 = manager.findByEmail(old_email);
                    if(m3 != null) {
                        m3.printInfo();

                        System.out.print("새 이름 > ");
                        String newName = sc.nextLine();
                        System.out.print("새 이메일 > ");
                        String newEmail = sc.nextLine();
                        System.out.print("새 연락처 > ");
                        String newPhone = sc.nextLine();

                        manager.update(old_email, newName, newEmail, newPhone);
                    }
                    break;
                case 6:
                    System.out.print("[삭제] 삭제할 회원의 이메일 > ");
                    manager.remove(sc.nextLine());
                    break;
                case 7:
                    System.out.println("이용해주셔서 감사합니다.");
                    return;
                default:
                    System.out.println("1~7번 중에서 선택하세요.");
            }
        }
    }
}
