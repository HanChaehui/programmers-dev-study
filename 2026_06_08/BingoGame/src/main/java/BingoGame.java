import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class BingoGame {

    static final int TARGET = 3; // 3줄 먼저 만들면 승리
    static final int SIZE = 5; // 판 크기
    static final int MAX = 25; // 최대 숫자 범위 제한
    static Scanner sc = new Scanner(System.in);

    private int[][] playerBoard = new int[SIZE][SIZE];
    private int[][] computerBoard = new int[SIZE][SIZE];
    private boolean[][] playerMarked = new boolean[SIZE][SIZE];
    private boolean[][] computerMarked = new boolean[SIZE][SIZE];

    private boolean[] called = new boolean[MAX + 1]; // 이미 부른 숫자를 또 부르지 못하게 하기 위해

    // 1. 시작 화면 띄우기
    public void play(){

        System.out.println("===== 빙고 게임 =====");
        System.out.println("컴퓨터와 번갈아 숫자를 불러 빙고를 완성하세요!");
        System.out.println("먼저 " + TARGET + "줄을 완성하면 승리!");

        makeBoard(playerBoard);
        makeBoard(computerBoard);

        int num = 0;

        while(true) {

            // 내 빙고판 출력
            System.out.println("\n===== 사용자 빙고판 =====");
            printBoard(playerBoard, playerMarked);
            // 컴퓨터 빙고판 출력
            System.out.println("\n===== 컴퓨터 빙고판 =====");
            printBoard(computerBoard, computerMarked);

            // 내 차례
            num = playerPick();
            mark(playerBoard, playerMarked, num);
            mark(computerBoard, computerMarked, num);
            if(checkWin(countBingo(playerMarked), countBingo(computerMarked))) {
                System.out.println("\n===== 사용자 빙고판 =====");
                printBoard(playerBoard, playerMarked);
                // 컴퓨터 빙고판 출력
                System.out.println("\n===== 컴퓨터 빙고판 =====");
                printBoard(computerBoard, computerMarked);
                break;
            }

            // 컴퓨터 차례
            num = computerPick();
            mark(playerBoard, playerMarked, num);
            mark(computerBoard, computerMarked, num);
            if(checkWin(countBingo(playerMarked), countBingo(computerMarked))) {
                System.out.println("\n===== 사용자 빙고판 =====");
                printBoard(playerBoard, playerMarked);
                // 컴퓨터 빙고판 출력
                System.out.println("\n===== 컴퓨터 빙고판 =====");
                printBoard(computerBoard, computerMarked);
                break;
            }
        }
    }

    // 2. 빙고판 만들기
    public void makeBoard(int[][] board){
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= MAX; i++) nums.add(i);
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
            System.out.println("\n==사용자 차례==");
            System.out.print("부를 숫자 입력 (1~25) > ");
            int num;
            try {
                num = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요."); continue;
            }
            if(num < 1 || num > 25) {
                System.out.println("1~25 사이로 입력하세요.");
            }
            else if(called[num]) {
                System.out.println("이미 부른 숫자입니다.");
            }
            else{
                called[num] = true;
                return num;
            }
        }
    }

    // 6 - 2. [컴퓨터] 난수 생성
    int computerPick(){
        int num;
        do {
            num = (int) (Math.random() * 24 + 1);
        } while(called[num]);
        System.out.println("\n==컴퓨터 차례==");
        System.out.printf("컴퓨터가 [%d]를 선택하였습니다.\n", num);
        called[num] = true;
        return num;
    }

    // 7. 빙고 완성 확인
    public boolean checkWin(int playerBingo, int computerBingo){
        if(playerBingo >= 3 && computerBingo >=3 ) {
            System.out.println("[무승부] 동시에 빙고 3줄 달성!\n게임을 종료합니다.");
            return true;
        }
        else if(playerBingo >= 3) {
            System.out.println("[사용자] 빙고 3줄 달성! 축하합니다!\n게임을 종료합니다.");
            return true;
        }
        else if(computerBingo >= 3) {
            System.out.println("[컴퓨터] 빙고 3줄 달성!\n게임을 종료합니다.");
            return true;
        }
        else {
            System.out.printf("[사용자] 현재 빙고 %d줄\n", playerBingo);
            System.out.printf("[컴퓨터] 현재 빙고 %d줄\n", computerBingo);
            return false;
        }
    }

}
