import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BingoGame {

    static final int SIZE = 5; // 판 크기
    static final int MAX = 25; // 최대 숫자 범위 제한

    private int[][] board = new int[SIZE][SIZE];
    private boolean[][] marked = new boolean[SIZE][SIZE];
    private boolean[] called = new boolean[MAX]; // 이미 부른 숫자를 또 부르지 못하게 하기 위해

    // 1. 시작 화면 띄우기
    public void play(){
        System.out.println("===== 빙고 게임 =====");
        System.out.println("컴퓨터와 번갈아 숫자를 불러 빙고를 완성하세요!");
    }

    // 2. 빙고판 만들기
    public void makeBoard(int[][] board){
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < MAX; i++) nums.add(i);
        Collections.shuffle(nums); // 랜덤으로 섞기

        int idx = 0;
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                board[i][j] = nums.get(idx++); // 1 ~ 25 사이 랜덤 숫자
            }
        }
    }

    // 3. 빙고판 출력
    public void printBoard(int[][] board, boolean[][] marked){
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                if(marked[i][j]) System.out.print("[ ★] ");
                else System.out.printf("[%2d] ", board[i][j]);
            }
            System.out.println();
        }
    }

    // 4. 숫자 마킹
    public void mark(int[][] board, boolean[][] marked, int num){
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                if(board[i][j] == num) marked[i][j] = true;
            }
        }
    }

    // 5. 빙고 줄 세기
    public int countBingo(boolean[][] marked){
        int count = 0;

        // 가로
        for(int i = 0; i < SIZE; i++){
            boolean bingo = true;
            for(int j = 0; j < SIZE; j++){
                if(!marked[i][j]) bingo = false;
            }
            if(bingo) count++;
        }
        // 세로
        for(int i = 0; i < SIZE; i++){
            boolean bingo = true;
            for(int j = 0; j < SIZE; j++){
                if(!marked[j][i]) bingo = false;
            }
            if(bingo) count++;
        }
        // 대각선 \
        boolean bingo1 = true;
        for(int i = 0; i < SIZE; i++){
            if(!marked[i][i]) bingo1 = false;
        }
        if(bingo1) count++;

        // 대각선 /
        boolean bingo2 = true;
        for(int i = 0; i < SIZE; i++){
            if(!marked[i][SIZE - 1 - i]) bingo2 = false;
        }
        if(bingo2) count++;

        return count;
    }

    // 6 - 1. [사용자] 숫자 부르기
    int playerPick(){
        while(true){

        }
    }

}
