import java.util.ArrayList;
import java.util.List;

public class MemberManager {

    private final List<Member> members = new ArrayList<>();
    private final int capacity;

    public MemberManager(int capacity){
        this.capacity = capacity;
    }

    public void printMenu(){
        System.out.println("\n[수행할 업무 - 현재 회원수 : " + members.size() + "/" + capacity + "]");
        System.out.println("[1]회원추가 [2]회원조회(메일) [3]회원조회(이름)");
        System.out.println("[4]전체조회 [5]수정 [6]삭제 [7]종료");
        System.out.print("> ");
    }

    public boolean isFull(){
        return members.size() >= capacity;
    }

    public boolean existsEmail(String email){
        for(Member i : members){
            if(email.equals(i.getEmail())) return true;
        }
        return false;
    }

    public void add(int grade, String name, String email, String phone){
        if(existsEmail(email)) {
            System.out.println("이미 존재하는 회원의 이메일입니다.");
            return;
        }
        if(grade == 1) members.add(new NormalMember(name, email, phone));
        else if(grade == 2) members.add(new VipMember(name, email, phone));
        System.out.println("추가되었습니다.");
    }

    public Member findByName(String name){
        for(Member i : members){
            if(name.equals(i.getName())) {
                return i;
            }
        }
        System.out.println("해당 사용자를 찾을 수 없습니다.");
        return null;
    }

    public Member findByEmail(String email){
        for(Member i : members){
            if(email.equals(i.getEmail())) {
                return i;
            }
        }
        System.out.println("해당 사용자를 찾을 수 없습니다.");
        return null;
    }

    // Main에서 이미 old_email로 findByEmail 검사 완료 했어야함
    // 해당 회원 없다면 실행 x, 있다면 정보 출력 후 수정 정보 받아 update()호출
    public void update(String old_email, String name, String email, String phone){
        if(existsEmail(email)&&!(old_email.equals(email))) {
            System.out.println("이미 존재하는 회원의 이메일입니다.");
            return;
        }
        // 정보 수정할 회원 찾아 수정
        findByEmail(old_email).update(name, email, phone);
        System.out.println("수정되었습니다.");
    }

    public void remove(String email){
        if(!existsEmail(email)) {
            System.out.println("해당 사용자를 찾을 수 없습니다.");
            return;
        }
        System.out.print("삭제할 회원의 정보 : ");
        findByEmail(email).printInfo();

        for(int i = 0; i < members.size(); i++){
            if(email.equals(members.get(i).getEmail())) {
                members.remove(i);
                System.out.println("삭제되었습니다.");
                return;
            }
        }
    }

    public void printAll(){
        if (members.isEmpty()) {
            System.out.println("등록된 회원이 없습니다.");
            return;
        }
        System.out.println("\n[전체 조회]");
        for(Member i : members) i.printInfo();
        System.out.println();
    }
}
