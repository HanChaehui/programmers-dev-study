public class L_fanta extends L_drink{

    public L_fanta(){
        super("환타", 300);
    }

    @Override
    void dispense() {
        System.out.println("달콤한 환타가 나왔습니다!");
    }
}
