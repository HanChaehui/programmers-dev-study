public abstract class L_drink {

    protected String name;
    protected int price;

    public L_drink(String name, int price){
        this.name = name;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    abstract void dispense();
}
