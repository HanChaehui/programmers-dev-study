public class MemberManager{

    private int memberCnt;
    private Member[] members;

    // 생성자
    public MemberManager(int capacity){
        members = new Member[capacity];
        memberCnt = 0;
    }

    public int getMemberCnt(){
        return memberCnt;
    }

    public int getCapacity(){
        return members.length;
    }

    public boolean isFull(){
        return memberCnt == members.length;
    }

    public boolean existsEmail(String email){
        for(int i = 0; i < memberCnt; i++) {
            if(email.equals(members[i].getEmail())){
                return true;
            }
        }
        return false;
    }

    // [1] 회원 추가
    public void add(Member member){
        members[memberCnt] = member;
        memberCnt++;
    }

    // [2] 회원 조회 - 메일
    public Member findByEmail(String email){
        for(int i = 0; i < memberCnt; i++) {
            if(email.equals(members[i].getEmail())){
                return members[i];
            }
        }
        return null;
    }

    // [3] 회원 조회 - 이름
    public Member findByName(String name){
        for(int i = 0; i < memberCnt; i++) {
            if(name.equals(members[i].getName())){
                return members[i];
            }
        }
        return null;
    }

    // [4] 전체 조회
    public void printAll(){
        System.out.println();
        if(memberCnt == 0){
            System.out.println("등록된 회원이 없습니다.");
        }
        for(int i = 0; i < memberCnt; i++) {
            members[i].printInfo();
        }
        System.out.println();
    }

    // [5] 수정
//    public boolean update(String email, String name, String newEmail, String phone){
//        if (findByEmail(email) != null){
//            findByEmail(email).update(name, newEmail, phone);
//            return true;
//        }
//        return false;
//    }

    // [5] 수정
    public void update(String email, String name, String newEmail, String phone){
        findByEmail(email).update(name, newEmail, phone);
    }

    // [6] 삭제
    public boolean delete(String email){
        int idx = -1;
        for(int i = 0; i < memberCnt; i++) {
            if(email.equals(members[i].getEmail())){
                idx = i;
                break;
            }
        }
        if(idx == -1) return false;

        for(int i = idx; i < memberCnt; i++) {
            members[i] = members[i+1];
        }
        members[memberCnt-1] = null;
        memberCnt--;
        return true;
    }
}
