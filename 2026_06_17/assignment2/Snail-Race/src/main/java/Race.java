public class Race {
    // 여러 스레드가 공유하는 변수
    private volatile boolean over = false;

    public boolean isOver(){
        return over;
    }

    // 공유 변수 over를 수정하는 critical section
    // synchronized로 critical section 동기화
    public synchronized void finish(int num){
        // 여러 스레드가 거의 동시에 finish에 도착 후
        // 거의 동시에 finish() 호출 시
        // 먼저 실행한 스레드가 over를 true로 바꿈
        // 또 다른 스레드가 바로 다음에 finish() 호출
        // over 체크를 안하면 둘 다 우승 출력해버림
        // 그래서 if(!over)
        if(!over) {
            over = true;
            System.out.println("\n*** 우승: 달팽이" + num + " ***");
        }
    }

    // 더 먼저 position == 30 되어도
    // finish() 먼저 호출하는 달팽이가 우승..인듯
}
