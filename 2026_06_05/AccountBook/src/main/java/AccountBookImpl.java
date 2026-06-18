import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AccountBookImpl implements AccountBook{
    private HashMap<String, ArrayList<Item>> account;

    public AccountBookImpl(){

    }

    @Override
    public void addAccount() {

    }

    @Override
    public void showAccount() {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void deleteItem() {

    }

    public void printMenu(){
        System.out.println("==== 가계부 ====");
        System.out.println("1. 내역 추가");
        System.out.println("2. 내역 조회");
        System.out.println("3. 전체 삭제");
        System.out.println("4. 내역 삭제");
        System.out.println("5. 종료");
        System.out.println("번호 입력 >");
    }
}
