public class L_coke extends L_drink{

    public L_coke(){
        super("콜라", 500);
    }

    @Override
    void dispense() {
        System.out.println("시원한 콜라가 나왔습니다!");
    }
}
