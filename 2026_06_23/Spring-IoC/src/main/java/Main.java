public class Main {
    static void main() {

        System.out.println("===== 2. DI =====");
        CoffeeMaker maker1 = new CoffeeMaker(new ColombiaBean());
        maker1.brew();
        CoffeeMaker maker2 = new CoffeeMaker(new EthiopiaBean());
        maker2.brew();

        System.out.println("\n===== 3. IoC 컨테이너 =====");
        CoffeeContainer container = new CoffeeContainer();
        container.getCoffeeMaker().brew();

        System.out.println("\n===== 4. 헐리우드 원칙 =====");
        Button button = new Button();
        button.setListener(new LikeAction());
        button.press();
    }
}
