public class Start {

    public static void main(String[] args) {
        int[][] board = new int[SIZE][SIZE];
        boolean[][] marked = new boolean[SIZE][SIZE];
        boolean[] called = new boolean[MAX]; // 이미 부른 숫자를 또 부르지 못하게 하기 위해
        BingoGame game = new BingoGame();
        game.play();
    }
}
