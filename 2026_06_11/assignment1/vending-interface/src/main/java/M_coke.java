public class M_coke implements M_drink{
    private final String name = "콜라";
    private final int price = 500;

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void dispense() {
        System.out.println("콜라가 나왔습니다.");
    }
}
