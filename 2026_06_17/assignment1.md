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
    - `getName()` : 현재 실행 중인 **스레드의 이름을 반환**하는 **Thread 클래스의 메서드**
2. Runnable 인터페이스를 구현하기 
    - `Thread.currentThread()` : 현재 실행 중인 **스레드의 Thread 객체를 반환**하는 **Thread 클래스의 static 메서드**
    - `Runnable`구현 클래스는 Thread를 상속받지 않으므로 바로 getName()호출 불가능, 따라서 `Thread.currentThread.getName()` 사용
    - Runnable 인터페이스의 핵심 메서드는 사실상 `void run();` 뿐이며, 
    스레드 이름, 우선순위, 상태 같은 건 `Thread` 객체가 관리

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

- 그냥 평범한 메서드를 호출한 것, 즉 새로
- 운 스레드가 생성되지 않음
- 지금 실행 중인 스레드(메인 스레드)가 `run()` 안의 코드를 그대로 실행함

`start()`를 호출하면?

- 새로운 스레드를 만들고, 그 스레드가 `run()`을 대신 실행해줌

```java
class C_thread_2_1 extends Thread {
    @Override
    public void run() {
        System.out.println("현재 스레드: " + Thread.currentThread().getName());
        System.out.println("th2_1 run()~~~~~");
        throwException();
    }

    public void throwException() {
        try {
            throw new Exception();
        } catch (Exception e) {
		        // 현재 실행중인 스레드의 호출스택 출력
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
		    exam1(); // java.lang.Exception
			               // at C_thread_2_1.throwException(C_thread_2.java:28)
			               // at C_thread_2_1.run(C_thread_2.java:23)
        exam2(); // java.lang.Exception
										 // at C_thread_2_1.throwException(C_thread_2.java:28)
										 // at C_thread_2_1.run(C_thread_2.java:23)
	                   // at C_thread_2.exam2(C_thread_2.java:44)
	                   // at C_thread_2.main(C_thread_2.java:48)
    }
}
```

#### 참고 (스레드 life cycle)

- 각 스레드는 자신이 호출한 메서드들과 지역변수들을 저장하는 독립적인 **호출 스택**을 가짐, 그래서 여러 스레드가 동시에 실행되어도 각자의 메서드 실행 흐름은 따로 관리됨
- 스레드 실행이 끝나면 그 스레드의 호출 스택도 함께 사라짐
- **스레드의 호출 스택 : 스레드가 자신이 호출한 메서드, 지역변수를 스택으로 저장**
- 스레드가 여러 개면, 누가 언제 얼마나 실행될지는 스케줄러가 정한다. (내 차례가 오면 실행, 시간이 끝나면 다시 대기)
- main 메서드도 사실은 하나의 스레드(main 스레드)다.
- main 이 먼저 끝나도, 다른 스레드가 아직 작업 중이면 프로그램은 끝나지 않는다.

```java
    static void main(String[] args) {
    
		    // thread-0 스레드가 호출한 메서드 스택
		    exam1(); // java.lang.Exception
			               // at C_thread_2_1.throwException(C_thread_2.java:28)
			               // at C_thread_2_1.run(C_thread_2.java:23)
			               
			  // main 스레드가 호출한 메서드 스택             
        exam2(); // java.lang.Exception
										 // at C_thread_2_1.throwException(C_thread_2.java:28)
										 // at C_thread_2_1.run(C_thread_2.java:23)
	                   // at C_thread_2.exam2(C_thread_2.java:44)
	                   // at C_thread_2.main(C_thread_2.java:48)
    }
```

## 싱글 스레드 vs 멀티 스레드

- 멀티스레드는 작업을 동시에 처리할 수 있지만, 스레드 전환 비용(context switching)이 있기 때문에 항상 더 빠른 것은 아님
- context switching : CPU가 실행할 스레드를 바꾸는 것, CPU가 현재 스레드의 상태를 저장하고, 다른 스레드의 상태를 불러옴
- 싱글 스레드가 유리 : 단순 계산이나 짧은 작업
ex) 숫자 더하기, 짧은 반복문, 간단한 출력
- 멀티 스레드가 유리 : 파일/네트워크/입출력처럼 기다리는 시간이 많은 작업
ex) 파일 읽기, 네트워크 요청, DB 조회, 사용자 입력 대기, 이미지 다운로드

```java
class C_thread_3_1 extends Thread{
    @Override
    public void run() {
        for ( int i = 0; i < 300; i++ ) {
            System.out.printf("%s ", "|");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("thread-0 소요시간 : " + (endTime - C_thread_3.startTime) + "ms");
    }
}

public class C_thread_3 {
    static long startTime = 0;

    // - 싱글스레드
    public static void exam1() {
        long startTime = System.currentTimeMillis();
        for ( int i = 0; i < 300; i++ ) {
            System.out.printf("%s ", "-");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("소요시간1 : " + (endTime - startTime) + "ms");
        for ( int i = 0; i < 300; i++ ) {
            System.out.printf("%s ", "|");
        }
        endTime = System.currentTimeMillis();
        System.out.println("소요시간2 : " + (endTime - startTime) + "ms");
    }

    // - 멀티스레드
    public static void exam2() {
        C_thread_3_1 thread = new C_thread_3_1();
        startTime = System.currentTimeMillis();
        thread.start();
        // main스레드가 출력
        for ( int i = 0; i < 300; i++ ) {
            System.out.printf("%s ", "-");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("main 스레드 소요시간 : " + (endTime - startTime) + "ms");
    }

    public static void main(String[] args) {

        exam1(); // 싱글 스레드: main 스레드 하나가 - 출력 후 | 출력을 순차 실행
                 // 전체 완료 시간(- 출력 + | 출력)은 소요 시간2
        exam2(); // 멀티 스레드: main 스레드는 - 출력, 새 스레드는 | 출력
                 // 두 스레드가 번갈아 실행되므로 출력이 섞일 수 있음
                 // 전체 완료 시간은 두 소요 시간 중 더 긴 시간?
    }
}
```

## 데몬 스레드 Daemon Thread

- 메인 작업을 뒤에서 도와주는 보조 스레드
- **일반 스레드가 모두 종료되면 데몬 스레드도 함께 종료됨**
- `setDaemon(true)`는 `start()` 호출 전에 설정해야 함
- ex) 가비지 컬렉터(GC), 워드의 자동 저장, 백그라운드 모니터링 등

```java
public class C_thread_4_daemon implements Runnable {

    static boolean autoSave = false;

    // 3초 마다 한 번씩 "자동저장이 켜져 있으면" 저장을 실행
    // 데몬 스레드라서 main 스레드가 끝나고 일반 스레드가 모두 종료되면,
		// 자동 저장 스레드도 함께 종료된다.
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep( 3*1000 );
                if ( autoSave ) {
                    autoSave();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void autoSave() {
        System.out.println("작업 파일이 자동 저장되었습니다.");
    }

    public static void main(String[] args) {
        Thread thread = new Thread(new C_thread_4_daemon());
        thread.setDaemon(true);
        thread.start();

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i);
            if ( i == 5 ) autoSave = true;
        }

        System.out.println("프로그램을 종료합니다.");
    }
}

*데몬 스레드 : 3초마다 한 번씩 자동 저장 여부를 검사하는 백그라운드 작업
*main 스레드 : 1초마다 숫자를 출력하고, 약 6초쯤 지난 뒤부터 자동 저장 기능이 켜짐

<예상 실행 흐름>
1초 후: 0
2초 후: 1
3초 후: 2        데몬 스레드 검사, autoSave false
4초 후: 3
5초 후: 4
6초 후: 5        autoSave = true
9초 후:          데몬 스레드 검사, autoSave true → 자동 저장
10초 후: 9
프로그램을 종료합니다.

<출력 결과>
0
1
2
3
4
5
6
7
작업 파일이 자동 저장되었습니다.
8
9
프로그램을 종료합니다.

데몬 스레드가 아닌 일반 스레드였다면 run() 내부 while(true)가 있으므로
main이 끝나도 이 스레드가 계속 살아 있어서 프로그램이 종료되지 않을 수 있음
```

## 스레드의 동기화

#### 동기화가 필요한 이유

- 여러 스레드가 같은 자원을 공유하고 동시에 사용하는 경우 값이 꼬일 수 있음
- 이를 막기 위해 한 번에 하나의 스레드만 특정 코드 영역에 들어가도록 제한하는 것이 동기화

#### Critical Section (임계영역)

- 한 번에 한 스레드만 들어가야 하는 부분
- **공유 자원**을 읽거나 수정하는 코드 영역
    
    ```java
    if (balance >= money) {
    		Thread.sleep(1000);
    		balance -= money;
    }
    ```
    

#### lock (잠금)

- critical section에 들어가기 위해 필요한 열쇠같은 개념
- lock을 가진 한 스레드만 critical section에 들어갈 수 있고, 작업이 끝나면 lock을 반납해야 다음 스레드가 들어갈 수 있음
- 비유 : 화장실 = critical section / 열쇠 = lock / 사람 = thread 
           (열쇠가 하나뿐이면 한 번에 한 명만 화장실에 들어갈 수 있음)

## 뮤텍스 Mutex (상호 배제)

- **lock이 단 1개**라서, 한 번에 오직 한 스레드만 critical section에 들어갈 수 있게 하는 잠금 방식
- 자바에서는 모든 객체가 자기만의 lock, 즉 **모니터 monitor**를 하나씩 가지고 있음
- `synchronized` 는 이 객체의 lock을 잡고 푸는 역할
- `synchronized(this){...}`: 현재 객체(공유 객체)의 lock을 잡고, 이 블록 안의 코드를 실행하겠다는 뜻

```java
Runnable r = new ThreadEx_bank();

new Thread(r).start();
new Thread(r).start();

// 두 스레드가 같은 ThreadEx_bank 객체 하나를 실행
// ThreadEx_bank 안의 ThreadEx_account 객체 하나가 사용
// 그 안에 balance도 공유 -> 동시에 건드려서 문제가 발생할수도!

Thread-0 ┐
         ├─ 같은 ThreadEx_bank 객체 공유
Thread-1 ┘
              └─ 같은 account 객체 공유
                   └─ 같은 balance 공유
```

```java
class ThreadEx_account {

    private int balance = 1000;

    public int getBalance() {
        return balance;
    }

    // 출금
    // 동기화 X
    // 스레드 1이 if ( balance >= money ) 통과 후 balance -= money; 하기 전
    // 스레드 2가 if ( balance >= money ) 통과 한다면 balance가 음수 될수도
//    public void withdraw(int money) {
//
//        if ( balance >= money ) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            balance -= money;
//        }
//
//    }

    // 동기화 O
    public void withdraw(int money) {
        synchronized (this) {
            // 코드 작성
            if ( balance >= money ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                balance -= money;
            }
        }
    }
}

// 통장에서 계속 돈을 출금
class ThreadEx_bank implements Runnable {

    private ThreadEx_account account = new ThreadEx_account();

    @Override
    public void run() {
        while ( account.getBalance() > 0 ) { // 잔고가 남아 있는 동안 반복
            // 100, 200, 300 중 임의로 선택해서 출금
            int money = (int) (Math.random() * 3 + 1) * 100;
            account.withdraw(money);
            System.out.println("balance : " + account.getBalance());
        }

    }
}

public class C_thread_5_sync {
    static void main(String[] args) {
    
        Runnable r = new ThreadEx_bank();

        new Thread(r).start(); // 출금 스레드2
        new Thread(r).start(); // 출금 스레드1

    }
}
```

#### `syncronized()` 사용법 2가지

1. **메서드 전체에 걸기** : 메서드가 끝날 때까지 현재 객체 this의 lock을 잡는다.
    
    `public syncronized void withdraw(int money) { ... }`
    ( == `public void withdraw(int money) { synchronized (this) {...} }` )
    
2. **필요한 블럭에만 걸기** : 진짜 공유 자원을 건드리는 부분만 synchronized 블록으로 감싸 lock을 잡는다.
`syncronized(객체 참조 변수) { ... critical section ... }`

→일반적으로 lock을 오래 잡고 있으면 다른 스레드가 오래 기다려야 해서 성능이 떨어질 수 있음. 그래서 메서드 전체보다 필요한 부분만 동기화 블럭을 거는 게 더 효율적일 수 있음

## 세마포어 Semaphore

- lock이 N개여서, 한 번에 N개의 스레드까지 critical section에 들어갈 수 있는 잠금 방식
- **뮤텍스는 열쇠가 1개인 세마포어**라고 볼 수 있음
- ex) DB 연결, 동시 다운로드 갯수 제한, 프린터, 주차장 등 
개수가 제한된 자원을 여러 스레드가 동시에 너무 많이 쓰지 못하게 “최대 동시 사용 개수”를 제한하는 도구

#### 핵심 메서드 (java.util.concurrent.Semaphore)

- `new Semaphore(N)` : 동시에 N개까지 허용 (열쇠 N개)
- `acquire()` : 열쇠 받기 (없으면 생길 때까지 대기)
- `release()` : 열쇠 반납하기
- 세마포어 static으로 선언하기!!!!!!!!!!!!

세마포어를 이용해서 동시에 주차장에 들어갈 수 있는 차의 수를 3대로 제한하는 예제

- 차(스레드)가 들어오려면 입구에서 `aquire()` 로 "주차권"을 받아야 한다.
- 주차권은 딱 3장 뿐 → 3대까지만 입장, 자리가 없다면 빈자리가 날 때까지 대기,동시에 최대 3개의 스레드만 `acquire()`를 통과할 수 있음
- 차가 나가면 `release()` 로 주차권을 반납한다.→ 기다리던 차가 들어올 수 있다.

```java
// static으로 선언했기 때문에 모든 Car 객체가 
// 같은 parkingLot 세마포어를 공유함
import java.util.concurrent.Semaphore;

class Car implements Runnable {

    // 주차장 자리 3개 = 허가권 3개
    private static final Semaphore parkingLot = new Semaphore(3);

    private final String name;

    public Car(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        boolean acquired = false;

        try {
            System.out.println(name + " : 주차 자리를 기다리는 중...");

            parkingLot.acquire(); // 주차권 받기, 없으면 대기
            acquired = true;

            System.out.println(name + " : 주차 완료! (남은 자리 : " + parkingLot.availablePermits() + ")");

            Thread.sleep(2000); // 2초 동안 주차 중

            System.out.println(name + " : 출차합니다.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquired) {
                parkingLot.release(); // 주차권 반납
            }
        }
    }
}

public class C_thread_6_sem {
    public static void main(String[] args) {
        // 차 6대가 한꺼번에 주차 시도
        for (int i = 1; i <= 6; i++) {
            new Thread(new Car("Car-" + i)).start();
        }
    }
}
```

#### 뮤텍스 vs 세마포어

- 뮤텍스는 열쇠가 1개(한 스레드만 입장)
 `synchronized (this)`
- 세마포어(Semaphore)는 열쇠가 N개(N스레드 입장) 
`Semaphore semaphore = new Semaphore(N);` `aquire()` `release()`
- **뮤텍스는 세마포어의 특수한 경우 (뮤텍스는 N=1인 세마포어)**