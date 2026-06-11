public class M_fanta implements M_drink{
    private final String name = "환타";
    private final int price = 300;

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void dispense() {
        System.out.println("환타가 나왔습니다.");
    }
}
