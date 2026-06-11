public class M_cider implements M_drink{
    private final String name = "사이다";
    private final int price = 500;

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void dispense() {
        System.out.println("사이다가 나왔습니다.");
    }
}
