public class L_cider extends L_drink{

    public L_cider(){
        super("사이다", 500);
    }

    @Override
    void dispense() {
        System.out.println("톡 쏘는 사이다가 나왔습니다!");
    }
}
