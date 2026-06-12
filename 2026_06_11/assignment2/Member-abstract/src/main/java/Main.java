import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("[요금제를 선택하세요]");
        System.out.println("[1]Lite:10 [2]Basic:20 [3]Premium:30");
        int capacity = Integer.parseInt(sc.nextLine());
        MemberManager mm = new MemberManager(capacity * 10);

        while(true){

            System.out.println("[수행할 업무 - 현재 회원수 : " + mm.getMemberCnt() + "/" + mm.getCapacity() + "]");
            System.out.println("[1]회원추가 [2]회원조회(메일) [3]회원조회(이름)");
            System.out.println("[4]전체조회 [5]수정 [6]삭제 [7]종료");
            System.out.print("> ");

            int choice = Integer.parseInt(sc.nextLine());

            switch(choice){
                case 1:
                    if(mm.isFull()){
                        System.out.println("회원이 꽉 찼습니다.");
                    }
                    else{
                        System.out.println("등급 [1]일반 [2]VIP");
                        int grade = Integer.parseInt(sc.nextLine());
                        if(!((grade == 1) || (grade == 2))){
                            System.out.println("잘못된 입력입니다.");
                        }
                        System.out.print("이름 > ");   String name  = sc.nextLine();
                        System.out.print("이메일 > "); String email = sc.nextLine();
                        System.out.print("연락처 > "); String phone = sc.nextLine();

                        if(mm.existsEmail(email)){
                            System.out.println("이미 존재하는 회원입니다.");
                        }
                        else{
                            Member m = (grade == 2) ? new VipMember(name, email, phone)
                                    : new NormalMember(name, email, phone);
                            mm.add(m);
                        }
                    }
                    break;
                case 2:
                    System.out.print("조회할 회원의 이메일을 입력하세요.\n> ");
                    String email = sc.nextLine();
                    if (mm.findByEmail(email) != null){
                        mm.findByEmail(email).printInfo();
                    }
                    else {
                        System.out.println("해당 이메일의 회원이 존재하지 않습니다.");
                    }
                    break;
                case 3:
                    System.out.print("조회할 회원의 이름을 입력하세요.\n> ");
                    String name = sc.nextLine();
                    if (mm.findByName(name) != null){
                        mm.findByName(name).printInfo();
                    }
                    else {
                        System.out.println("해당 이름의 회원이 존재하지 않습니다.");
                    }
                    break;
                case 4:
                    mm.printAll();
                    break;
//                case 5:
//                    System.out.print("정보를 수정할 회원의 이메일을 입력하세요.\n> ");
//                    String fix_email = sc.nextLine();
//                    if (mm.findByEmail(fix_email) != null){
//                        mm.findByEmail(fix_email).printInfo();
//                        System.out.print("수정할 회원 정보를 입력하세요.\n");
//                        System.out.print("이름 > ");   String new_name  = sc.nextLine();
//                        System.out.print("이메일 > "); String new_email = sc.nextLine();
//                        System.out.print("연락처 > "); String new_phone = sc.nextLine();
//                        // 새로 입력한 이메일이 이미 존재하는 이메일이면서, 수정 전 이메일과 같지 않다면
//                        if(mm.existsEmail(new_email)&&!(new_email.equals(fix_email))){
//                            System.out.println("이미 존재하는 회원입니다.");
//                        }
//                        else{
//                            mm.update(fix_email, new_name, new_email, new_phone);
//                            System.out.println("수정이 완료되었습니다.");
//                        }
//                    }
//                    else {
//                        System.out.println("해당 이메일의 회원이 존재하지 않습니다.");
//                    }
//                    break;
                case 5:
                    System.out.print("정보를 수정할 회원의 이메일을 입력하세요.\n> ");
                    String fix_email = sc.nextLine();
                    if (mm.findByEmail(fix_email) == null){
                        System.out.println("해당 이메일의 회원이 존재하지 않습니다.");
                        break;
                    }
                    mm.findByEmail(fix_email).printInfo();
                    System.out.print("수정할 회원 정보를 입력하세요.\n");
                    System.out.print("이름 > ");   String new_name  = sc.nextLine();
                    System.out.print("이메일 > "); String new_email = sc.nextLine();
                    System.out.print("연락처 > "); String new_phone = sc.nextLine();
                    // 새로 입력한 이메일이 이미 존재하는 이메일이면서, 수정 전 이메일과 같지 않다면
                    if(mm.existsEmail(new_email)&&!(new_email.equals(fix_email))){
                        System.out.println("이미 존재하는 회원의 이메일입니다.");
                        break;
                    }
                    mm.update(fix_email, new_name, new_email, new_phone);
                    System.out.println("수정이 완료되었습니다.");
                    break;
                case 6:
                    System.out.print("정보를 삭제할 회원의 이메일을 입력하세요.\n> ");
                    String delete_email = sc.nextLine();
                    if (!mm.delete(delete_email)){
                        System.out.println("해당 이메일의 회원이 존재하지 않습니다.");
                        break;
                    }
                    System.out.println("삭제가 완료되었습니다.");
                    break;
                case 7:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못 입력하셨습니다. 1 ~ 7의 번호를 입력해주세요.");
                    break;
            }
        }
    }
}
