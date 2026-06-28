public class Order {
    private int id;
    private java.util.List<String> items;

    Order(int id, java.util.List<String> items){
        this.id = id;
        this.items = items;
    }

    public java.util.List<String> getItems() { return items; }
}
