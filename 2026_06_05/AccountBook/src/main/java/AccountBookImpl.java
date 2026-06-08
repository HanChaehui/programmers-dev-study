import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AccountBookImpl implements AccountBook{
    private HashMap<String, ArrayList<Item>> account;

    // constructor
    public AccountBookImpl(){

    }
    @Override
    // 1. 내역 추가
    public void addAccount() {
    }
    void showAccount(); // 2. 내역 조회
    void deleteAll(); // 3. 전체 삭제
    void deleteItem(); // 4. 내역 삭제
}
