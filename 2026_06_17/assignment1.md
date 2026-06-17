## 멀티 스레딩

- 하나의 프로그램 안에서 여러 작업을 동시에 돌리는 것
- 스레드는 일회용이므로 한 번 실행하고 끝나면, 그 스레드는 다시 사용 불가

#### 장점

- CPU를 놀리지 않고 더 효율적으로 사용할 수 있음
- 작업 중에도 사용자 입력에 바로바로 반응함
- 작업을 나눠 두니 코드가 더 깔끔해짐

#### 주의할 점

- 여러 스레드가 같은 자원을 함께 쓰다보면 문제가 생길 수 있음
- 동기화, 교착 상태 등

#### 스레드를 만드는 2가지 방법

1. Thread 클래스를 상속 받기
2. Runnable 인터페이스를 구현하기 
    
    → Thread를 상속 받으면 다른 클래스를 상속 받을 수 없어 제약이 생기기 때문에 보통은 2번을 많이 사용
    

#### 멀티 스레딩 사용 예시

1. Thread 클래스를 상속 받기
    - `getName()` : Thread 클래스가 제공하는 메서드로 현재 실행 중인 스레드의 이름을 반환
2. Runnable 인터페이스를 구현하기 
    - `Thread.currentThread()` : 현재 이 코드를 실행하고 있는 스레드의 Thread 객체를 반환하는 static 메서드
    - `Runnable`을 구현한 경우 이 클래스는 Thread를 상속받지 않으므로 바로 getName()호출 불가

```java
// 1. Thread 객체 상속
class C_thread_1_1 extends Thread {
    @Override
    public void run() {
        // 수행할 동작 정의
        for ( int i = 0; i < 10; i++ ) {
            // getName() : Thread 클래스가 제공하는 메서드로 현재 실행 중인 스레드의 이름을 반환한다.
            System.out.println("th1 : " + i + " : " + getName());
        }
    }
}
// 2. Runnable 인터페이스 구현
class C_thread_1_2 implements Runnable {

    @Override
    public void run() {
        for ( int i = 0; i < 10; i++ ) {
            // Thread.currentThread() : 현재 이 코드를 실행하고 있는 스레드의 Thread객체를 반환하는 static 메서드
            // Runnable을 구현한 경우 이 클래스는 Thread를 상속하지 않으므로 바로 getName()을 호출불가
            System.out.println("th2 : " + i + Thread.currentThread().getName());
        }
    }
}
```

```java
public class C_thread_1 {
    static void main(String[] args) {
        C_thread_1_1 th1 = new C_thread_1_1();
        th1.start();
        System.out.println("main 쓰레드입니다.");

        Runnable r = new C_thread_1_2();
        Thread th2 = new Thread(r);
        th2.start();

        // * 스레드는 "일회용"이다.
        // - 한 번 실행하고 끝나면, 그 스레드는 다시 못 쓴다
//        th1.start();

        th1 = new C_thread_1_1();
        th1.start();

    }
}
```

#### `start()`와 `run()`의 차이

`run()`을 직접 호출하면?

- 그냥 평범한 메서드를 호출한 것, 즉 새로운 스레드가 생성되지 않음
- 지금 실행 중인 스레드가 `run()` 안의 코드를 그대로 실행함

`start()`를 호출하면?

- 새로운 스레드를 만들고, 그 스레드가 `run()`을 대신 실행해줌

```java
class C_thread_2_1 extends Thread {
    @Override
    public void run() {
        System.out.println("th2_1 run()~~~~~");
        throwException();
    }

    public void throwException() {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class C_thread_2 {

    public static void exam1() {
        C_thread_2_1 th2_1 = new C_thread_2_1();
        th2_1.start();
    }

    public static void exam2() {
        C_thread_2_1 th2_1 = new C_thread_2_1();
        th2_1.run();
    }

    static void main(String[] args) {
        exam2();
    }
}
```

#### 참고 (스레드 life cycle)

- 스레드마다 자기만의 작업 공간(호출 스택)을 가짐, 스레드가끝나면 그 공간도 사라짐
- 스레드가 여러 개면, 누가 언제 얼마나 실행될지는 스케줄러가 정한다. (내 차례가 오면 실행, 시간이 끝나면 다시 대기)
- main 메서드도 사실은 하나의 스레드(main 스레드)다.
- main 이 먼저 끝나도, 다른 스레드가 아직 작업 중이면 프로그램은 끝나지 않는다.

## 싱글 스레드 vs 멀티 스레드

- 멀티스레드가 항상 더 빠른 것은 아님
- 단순 계산만 하는 작업이라면 싱글 스레드가 더 효율적일 수 있음
- context switching(스레드 전환)에 드는 비용 때문
- 서로 다른 자원을 사용하는 작업은 멀티 스레드가 유리

```java

```

## 데몬 스레드

- 메인 작업을 뒤에서 도와주는 보조 스레드
- 일반 스레드가 전부 끝나면, 데몬 스레드도 같이 종료됨
- 가비지 컬렉터(GC), 워드의 자동 저장 등에서 사용

```java

```

## 스레드의 동기화

- 같은 자원을 여러 스레드가 함께 사용하므로, 동시에 접근하면 값이 꼬일 수 있음
- critical section(임계 영역) : 한 번에 한 스레드만 들어가야하는 코드 영역
- lock(잠금) : critical section에 들어가려면 받아야 하는 열쇠
- lock을 가진 한 스레드만 들어가고, 일을 끝내고 lock을 반납해야 다음 스레드가 들어갈 수 있음
- 이렇게 한 스레드의 작업을 다른 스레드가 방해하지 못하게 막는 것을 ‘동기화’라고 함

## 뮤텍스 Mutex(상호 배제)

- lock은 단 1개로, 한 번에 한 스레드만 critical section에 들어갈 수 있게 하는 lock
- 위에서 말한 lock(열쇠 하나, 화장실 하나)이 바로 뮤텍스 개념이다.
- 자바에서는 모든 객체가 lock(= 모니터)을 하나씩 가지고 있고, synchronized 가 이 lock을 잡았다/풀었다 해주는 역할을 한다. → 즉 synchronized(this) = "이 객체의 뮤텍스를 잡는다" 와 같은 뜻이다.
- 참고: 뮤텍스는 열쇠가 1개(한 명만 입장), 세마포어(Semaphore)는 열쇠가 N개(N명 입장)로// 여러 스레드의 동시 접근 수를 조절할 때 쓴다. (뮤텍스는 세마포어의 특수한 경우)

```java

```

#### `syncronized` 사용법 2가지

1. 메서드 전체에 걸기 : 메서드가 끝날 때까지 그 객체의 lock을 잡는다.
`public syncronized void calcSum() { ... }`
2. 일부 블럭에만 걸기 : 꼭 필요한 코드만  { }로 감싸 lock을 잡는다.
`syncronized(객체 참조 변수) { ... critical section ... }`

→ lock을 걸고 있으면 다른 스레드는 기다려야 해서 느려진다. 그래서 메서드 전체보다 필요한 부분만 동기화 블럭을 거는 게 더 효율적이다.

## 세마포어 Semaphore

- 동시에 critical section에 들어갈 수 있는 스레드 ‘수(N)’를 제한하는 잠금 도구
- 뮤텍스가 “열쇠 1개”라면, 세마포어는 “열쇠 N개”다.
- DB 커넥션 풀, 동시 다운로드 갯수 제한 등 자원이 한정 되어 있어 동시 사용 갯수를 조절하고 싶을 때 사용

#### 핵심 메서드 (java.util.concurrent.Semaphore)

- new Semaphore(N) : 동시에 N개까지 허용 (열쇠 N개)
- acquire() : 열쇠 받기 (없으면 생길 때까지 대기)
- release() : 열쇠 반납하기

비유 : 자리가 3개 뿐인 주차장

- 차(스레드)가 들어오려면 입구에서 "주차권"을 받아야 한다. (acquire)
- 주차권은 딱 3장뿐 → 3대까지만 들어가고, 4번째 차는 빈자리가 날 때까지 기다린다.
- 차가 나가면 주차권을 반납한다. (release) → 기다리던 차가 들어올 수 있다.

```java

```