public class M_water implements M_drink{
    private final String name = "물";
    private final int price = 200;

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void dispense() {
        System.out.println("물이 나왔습니다.");
    }
}
