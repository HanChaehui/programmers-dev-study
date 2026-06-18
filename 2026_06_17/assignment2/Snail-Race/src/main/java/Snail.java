import java.lang.Thread;
import java.util.Random;

public class Snail extends Thread{

    private Random random = new Random();
    private final int FINISH = 30;
    private int position = 0;
    private int num = 0;
    private Race race;

    public Snail(int num, Race race){
        this.num = num;
        this.race = race;
    }

    @Override
    public void run(){
        // !race.isOver() : 한 달팽이가 경기를 끝냈다면 다른 달팽이는 모두 정지
        while(position < FINISH && !race.isOver()) {
            position += random.nextInt(3) + 1;
            printProgress();
            // 진짜 도착한 달팽이는 finish()호출
            // 다른 달팽이가 경기를 끝내서 강제로 정지한 달팽이는 아무 행동 X
            // sleep이후, while문 밖에서 finish검사를 하지 않고
            // 도착하자마자 finish() 호출하여 다른 달팽이들이 최대한 더 전진 못하도록
            if (position >= FINISH) {
                race.finish(num);
                break;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void printProgress(){
        StringBuilder bar = new StringBuilder();
        for(int i = 0; i < position; i++){
            bar.append("=");
        }
        System.out.println("달팽이" + num + ": " + bar + ">");
    }
}
