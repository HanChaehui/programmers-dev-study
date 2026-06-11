public class L_water extends L_drink{

    public L_water(){
        super("물", 200);
    }

    @Override
    void dispense() {
        System.out.println("깔끔한 물이 나왔습니다!");
    }
}
