import java.util.Scanner;

public class member_management {
    static Scanner sc = new Scanner(System.in);
    static int totalCnt = 0; // 해당 프로그램에서 저장할 수 있는 회원수
    static int memberCnt = 0; // 실제 회원수

    // 요금제를 사용자한테 받는 함수 작성
    // [1]Lite : 10명 [2]Basic : 20명 [3]Premium : 30명
    public static int printPricePlan() {
        System.out.println("[요금제를 선택하세요]");
        System.out.println("[1]Lite : 10명 [2]Basic : 20명 [3]Premium : 30명");
        return Integer.parseInt(sc.nextLine());
    }

    // 수행 업무 목록 출력 함수
    public static int printMenu() {
        System.out.printf("[수행할 업무를 선택하세요 - 현재 회원수 : %d/%d]\n", memberCnt, totalCnt);
        System.out.println("[1]회원추가 [2]회원조회(메일) [3]회원조회(이름)\n[4]회원전체조회 [5]회원정보 수정 [6]회원삭제\n[7]프로그램 종료");
        return Integer.parseInt(sc.nextLine());
    }

    // 1. 회원 추가
    public static void addMember(String[][] members){
        if (memberCnt == totalCnt){
            System.out.println("회원이 꽉 찼습니다");
            return;
        }
        System.out.println("이름을 입력해주세요");
        String name = sc.nextLine();
        System.out.println("이메일을 입력해주세요");
        String email = sc.nextLine();
        System.out.println("연락처를 입력해주세요");
        String phone = sc.nextLine();
        if (checkEmail(members, email)){
            System.out.println("이미 존재하는 회원입니다");
            return;
        }
        members[memberCnt][0] = name;
        members[memberCnt][1] = email;
        members[memberCnt][2] = phone;
        memberCnt++;
    }
    // 1-1. 이메일 중복 체크
    public static boolean checkEmail(String[][] members, String email){
        for(int i = 0; i < memberCnt; i++){
            if(members[i][1].equals(email)){
                return true;
            }
        }
        return false;
    }
    // 2. 이메일로 회원 조회
    public static void selectEmail(String[][] members){
        System.out.println("조회할 이메일을 입력하세요");
        String email = sc.nextLine();

        for(int i = 0; i < memberCnt; i++){
            if(members[i][1].equals(email)){
                System.out.println("[이름] " + members[i][0] +
                        ", [이메일] " + members[i][1] +
                        ", [연락처] " + members[i][2]);
                return;
            }
        }
        System.out.println("찾으시는 정보가 없습니다");
    }

    // 3. 이름으로 회원 조회
    public static void selectName(String[][] members){
        System.out.println("조회할 이름을 입력하세요");
        String name = sc.nextLine();

        for(int i = 0; i < memberCnt; i++){
            if(members[i][0].equals(name)){
                System.out.println("[이름] " + members[i][0] +
                        ", [이메일] " + members[i][1] +
                        ", [연락처] " + members[i][2]);
                return;
            }
        }
        System.out.println("찾으시는 정보가 없습니다");
    }

    // 4. 회원 전체 조회
    public static void selectAll(String[][] members){
        for(int i = 0; i < memberCnt; i++){
            System.out.println("[이름] " + members[i][0] +
                    ", [이메일] " + members[i][1] +
                    ", [연락처] " + members[i][2]);
        }
    }

    // 5. 정보 수정
    public static void updateMember(String[][] members){
        System.out.println("정보를 수정할 회원의 이메일을 입력하세요");
        String email_origin = sc.nextLine();
        int idx = -1;

        for(int i = 0; i < memberCnt; i++){
            if(members[i][1].equals(email_origin)){
                idx = i;
                break;
            }
        }
        if(idx == -1){
            System.out.println("찾으시는 회원이 없습니다.");
            return;
        }
        System.out.println("[수정] 이름을 입력해주세요");
        String newname = sc.nextLine();
        System.out.println("[수정] 이메일을 입력해주세요");
        String newemail = sc.nextLine();
        System.out.println("[수정] 연락처를 입력해주세요");
        String newphone = sc.nextLine();
        if (email_origin.equals(newemail)){
            // 이메일은 그대로
        }
        else if (checkEmail(members, newemail)){
            System.out.println("[수정 실패] 이미 존재하는 회원의 이메일입니다.");
            return;
        }
        members[idx][0] = newname;
        members[idx][1] = newemail;
        members[idx][2] = newphone;
        System.out.println("수정이 완료되었습니다.");
    }

    // 6. 회원 삭제 + 당기기
    public static void deleteMember(String[][] members){
        System.out.println("정보를 삭제할 회원의 이메일을 입력하세요");
        String email_origin = sc.nextLine();
        int idx = -1;

        for(int i = 0; i < memberCnt; i++){
            if(members[i][1].equals(email_origin)){
                idx = i;
                break;
            }
        }
        if(idx == -1){
            System.out.println("찾으시는 회원이 없습니다.");
            return;
        }
        for(int i = idx; i < memberCnt - 1; i++){
            members[i][0] = members[i+1][0];
            members[i][1] = members[i+1][1];
            members[i][2] = members[i+1][2];
        }
        members[memberCnt-1][0] = null;
        members[memberCnt-1][1] = null;
        members[memberCnt-1][2] = null;
        memberCnt--;
        System.out.println("삭제가 완료되었습니다.");
    }

    public static void main(String[] args){
        totalCnt = printPricePlan() * 10;
        String[][] members = new String[totalCnt][3];
        while(true){
            int task = printMenu();
            switch (task){
                case 1:
                    addMember(members);
                    break;
                case 2:
                    selectEmail(members);
                    break;
                case 3:
                    selectName(members);
                    break;
                case 4:
                    selectAll(members);
                    break;
                case 5:
                    updateMember(members);
                    break;
                case 6:
                    deleteMember(members);
                    break;
                case 7:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("1~7의 숫자를 다시 입력해주세요.");
                    break;
            }
        }
    }
}
