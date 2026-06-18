public class Main {
    public static void main(String[] args) {

        Race race = new Race();

        Snail snail_1 = new Snail(1, race);
        Snail snail_2 = new Snail(2, race);
        Snail snail_3 = new Snail(3, race);

        snail_1.start();
        snail_2.start();
        snail_3.start();
    }
}
