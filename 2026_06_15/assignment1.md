# 3. Queue

- 먼저 삽입된 요소가 먼저 제거되는 **선입선출 (FIFO) 구조**를 따르는 자료 구조 (인터페이스)
- 대표적인 구현 클래스는 **LinkedList, PriorityQueue, ArrayDeque**가 있음

## Queue - 주요 메서드

- `add(E e)`: 큐의 맨 뒤에 요소를 추가하며, 가득 찬 경우 예외를 발생시킴
- `offer(E e)`: 큐의 맨 뒤에 요소를 추가하며, 가득 차도 예외를 발생시키지 않고 false를 반환
- `remove()`: 큐의 맨 앞에 있는 요소를 제거하고 반환하며,  큐가 비어 있는 경우 예외를 발생시킴
- `poll()`: 큐의 맨 앞에 있는 요소를 제거하고 반환하며, 큐가 비어 있으면 null을 반환
- `element()`: 큐의 맨 앞에 있는 요소를 반환하지만, 제거하지는 않음. 큐가 비어 있는 경우 예외를 발생시킴
- `peek()`: 큐의 맨 앞에 있는 요소를 반환하지만, 제거하지는 않음. 큐가 비어 있으면 null을 반환

## 3-1. LinkedList

- Queue 인터페이스를 구현하므로, 큐로 사용할 수 있음
- 양방향 리스트로, **큐와 리스트로 모두 사용 가능**

#### LinkedList - 메서드

```java
Queue<String> queue = new LinkedList<>();

        // 요소 추가
        queue.add("apple");
        queue.add("banana");
        queue.add("orange");
        queue.add("grape");
        queue.add("watermelon");

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + queue.peek());  // 출력: Apple

        // 큐에서 요소 제거 및 반환
        System.out.println("Poll: " + queue.poll());  // 출력: Apple

        // 큐에서 요소 제거 및 반환
        System.out.println("Poll: " + queue.poll());  // 출력: Banana

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + queue.peek());  // 출력: Orange

        // 큐의 크기 확인
        System.out.println("Queue size: " + queue.size());  // 출력: 1

        // 큐 비우기
        queue.clear();
        System.out.println("Queue is empty: " + queue.isEmpty());  // 출력: true
```

#### LinkedList - 순회 방법

```java
// 순회 방법 1: 향상된 for 루프 사용
        for (String element : queue) {
            System.out.println("Element: " + element);
        }

        // 순회 방법 2: Iterator 사용
        Iterator<String> iterator = queue.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            System.out.println("Element: " + element);
        }
```

## 3-2. PriorityQueue

- 우선순위 큐로, 요소가 자연 순서(기본 순서)나 인스턴스 생성 시 넘긴 **비교기(Comparator)**에 따라 정렬
- FIFO 순서가 아니라 우선순위 따라 요소가 처리됨
- `PriorityQueue`는 내부적으로 전체가 완벽히 정렬된 배열처럼 보관되는 건 아니므로 그냥 인스턴스명으로 출력 시 원하는 순서로 출력되지 않을 수 있음, `poll()` 이용 시 순서대로 출력

#### PriorityQueue - 메서드

```java
Queue<Integer> priorityQueue = new PriorityQueue<>();

        // 요소 추가
        priorityQueue.add(50);
        priorityQueue.add(20);
        priorityQueue.add(40);
        priorityQueue.add(10);

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + priorityQueue.peek());  // 출력: 10

        // 큐에서 요소 제거 및 반환 (우선순위가 높은 요소부터)
        System.out.println("Poll: " + priorityQueue.poll());  // 출력: 10
        System.out.println("Poll: " + priorityQueue.poll());  // 출력: 20

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + priorityQueue.peek());  // 출력: 40

        // 큐의 크기 확인
        System.out.println("Queue size: " + priorityQueue.size());  // 출력: 2

        // 큐 비우기
        priorityQueue.clear();
        System.out.println("Queue is empty: " + priorityQueue.isEmpty());  // 출력: true
```

#### PriorityQueue - 순회 방법

```java
// 순회 방법 1: 향상된 for 루프 사용
        for (Integer element : priorityQueue) {
            System.out.println("Element: " + element);
        }

        // 순회 방법 2: Iterator 사용
        Iterator<Integer> iterator = priorityQueue.iterator();
        while (iterator.hasNext()) {
            Integer element = iterator.next();
            System.out.println("Element: " + element);
        }

        // 순회 방법 3: forEach 메서드와 람다 표현식 사용 (Java 8 이상)
        priorityQueue.forEach(element -> {
            System.out.println("Element: " + element);
        });
```

## PriorityQueue - Comparator

객체들의 정렬 기준을 직접 정해주는 인터페이스

```java
1. Comparator 지정 x -> 기본 순서 (오름 차순)

2. Comparator.comparing 사용

	PriorityQueue<String> pq = new PriorityQueue<>(
	    Comparator.comparingInt(비교 대상 객체의 클래스명::해당 클래스 속 비교 요소)
	);

3. Comparator 오버라이딩하여 정렬 기준 직접 만들어 전달

4. 람다식 사용
4-1. 
		Queue<A_person> priorityQueue = new PriorityQueue<>(
    (p1, p2) -> p1.getName().compareTo(p2.getName())
		);
4-2.
		Comparator<Person> ageComparator = (p1, p2) -> p1.age - p2.age;
```

#### PriorityQueue - Comparator1

```java
				public static void exam2_2() {
        // 나이를 기준으로 오름차순 정렬
				// Queue<A_person> queue = new PriorityQueue<>(Comparator.comparingInt(A_person::getAge));

        Comparator<A_person> ageComparator = new Comparator<A_person>() {
            @Override
            public int compare(A_person p1, A_person p2) {
                // - 음수 값 : p1이 p2보다 작을 때
                // - 0 : p1과 p2가 같을 때
                // - 양수 값 : p1이 p2보다 클 때
                return Integer.compare(p1.getAge(), p2.getAge());
            }
        };

        Queue<A_person> priorityQueue = new PriorityQueue<>(ageComparator);

        // 요소 추가
        priorityQueue.add(new A_person("Alice", 30));
        priorityQueue.add(new A_person("Bob", 25));
        priorityQueue.add(new A_person("Charlie", 35));
        priorityQueue.add(new A_person("Dave", 20));

        // 요소를 하나씩 꺼내면서 출력
        while (!priorityQueue.isEmpty()) {
            System.out.println("Element: " + priorityQueue.poll());
        }

    }
```

#### PriorityQueue - Comparator2

```java
public static void exam2_3() {
        // 이름을 기준으로 오름차순 정렬
				// Queue<A_person> queue = new PriorityQueue<>(Comparator.comparing(A_person::getName));

        Comparator<A_person> nameComparator = new Comparator<A_person>() {
            @Override
            public int compare(A_person p1, A_person p2) {
                return p1.getName().compareTo(p2.getName());
            }
        };

        Queue<A_person> priorityQueue = new PriorityQueue<>(nameComparator);

        // 요소 추가
        priorityQueue.add(new A_person("Alice", 30));
        priorityQueue.add(new A_person("Bob", 25));
        priorityQueue.add(new A_person("Charlie", 35));
        priorityQueue.add(new A_person("Dave", 20));

        // 우선순위에 따라 요소를 하나씩 처리 (이름의 오름차순 정렬된 순서로)
        while (!priorityQueue.isEmpty()) {
            System.out.println("Poll: " + priorityQueue.poll());
        }

    }
```

#### PriorityQueue - Comparator3

```java
public static void exam2_4() {
        // 이름을 내림차순으로 정렬하는 Comparator -> 익명클래스로 구현
        Comparator<A_person> nameComparator = new Comparator<A_person>() {
            @Override
            public int compare(A_person p1, A_person p2) {
                return p2.getName().compareTo(p1.getName());
            }
        };
    }
```

## 3-3. ArrayDeque

- 양방향 큐로 사용 가능

#### ArrayDeque - 메서드

```java
public static void exam3() {
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();

        // 요소 추가
        arrayDeque.add("Apple");
        arrayDeque.add("Banana");
        arrayDeque.add("Orange");

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + arrayDeque.peek());  // 출력: Apple

        // 큐에서 요소 제거 및 반환
        System.out.println("Poll: " + arrayDeque.poll());  // 출력: Apple

        // 큐에서 요소 제거 및 반환
        System.out.println("Poll: " + arrayDeque.poll());  // 출력: Banana

        // 큐의 맨 앞 요소 확인 (제거하지 않음)
        System.out.println("Peek: " + arrayDeque.peek());  // 출력: Orange

        // 큐의 크기 확인
        System.out.println("Queue size: " + arrayDeque.size());  // 출력: 1

        // 큐 비우기
        arrayDeque.clear();
        System.out.println("Queue is empty: " + arrayDeque.isEmpty());  // 출력: true
    }
```

---

# 4. Set

- 중복되지 않는 요소들의 집합을 나타냄 (인터페이스)
- 대표적인 구현 클래스는 **HashSet, TreeSet** 등이 있음
- **순서 보장 X, 중복 불가, 빠른 검색**이 가능

## Set - 주요 메서드

- `add(E e)`: 집합에 요소를 추가, 중복된 요소가 추가되면 추가되지 않음
- `remove(Object o)`: 특정 요소를 제거
- `contains(Object o)`: 집합에 특정 요소가 포함되어 있는지 확인
- `size()`: 집합의 요소 개수를 반환
- `clear()`: 집합의 모든 요소를 제거

## 4-1. HashSet

- 해시 기반 : 내부적으로 HashMap을 사용하여 요소를 저장
- 빠른 성능 : 요소의 추가, 삭제, 검색이 평균적으로 O(1)의 시간 복잡도를 가짐
- 순서 없음 : 요소의 순서를 보장하지 않음

#### HashSet - 메서드

```java
Set<String> hashSet = new HashSet<>();

        hashSet.add("Apple");
        hashSet.add("Banana");
        hashSet.add("Orange");
        hashSet.add("Apple"); // 중복된 요소, 추가되지 않음

        System.out.println("HashSet elements: " + hashSet);

        hashSet.remove("Banana");
        System.out.println("After removing Banana: " + hashSet);

        boolean containsApple = hashSet.contains("Apple");
        System.out.println("Does HashSet contain Apple? " + containsApple);
```

#### HashSet - 순회 방법

```java
				// 순회 방법 1: 향상된 for 루프 사용
        for (String element : hashSet) {
            System.out.println("Element: " + element);
        }

        // 순회 방법 2: Iterator 사용
        Iterator<String> iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            System.out.println("Element: " + element);
        }
```

## 4-2. TreeSet

- 이진 검색 트리 기반 : 내부적으로 TreeMap을 사용하여 요소를 저장
- 정렬된 순서 : 요소가 **자동으로 오름차순으로 정렬**됨
- 성능 : 요소의 추가, 삭제, 검색이 O(log n)의 시간 복잡도를 가짐

#### TreeSet - 메서드

```java
				Set<String> treeSet = new TreeSet<>();

        treeSet.add("Banana");
        treeSet.add("Apple");
        treeSet.add("Orange");
        treeSet.add("Grape");

        System.out.println("TreeSet elements: " + treeSet);

        treeSet.remove("Apple");
        System.out.println("After removing Apple: " + treeSet);

        String firstElement = ((TreeSet<String>) treeSet).first();
        System.out.println("First element: " + firstElement);

        String lastElement = ((TreeSet<String>) treeSet).last();
        System.out.println("Last element: " + lastElement);
```

#### TreeSet - 순회 방법

```java
				// 순회 방법 1: 향상된 for 루프 사용
        for (String element : treeSet) {
            System.out.println("Element: " + element);
        }

        // 순회 방법 2: Iterator 사용
        Iterator<String> iterator = treeSet.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            System.out.println("Element: " + element);
        }
```

---

# Enum (열거형)

- 자바에서 **특정한 상수 집합을 정의**하는 데에 사용되는 특별한 데이터 타입
- enum은 클래스처럼 보이지만 실제로는 고정된 상수들의 집합을 정의한 것이며, 모든 enum은 **java.lang.Enum 클래스를 상속 받음**

## Enum 사용의 장점

- 코드의 명확성 : 코드에서 상수 값을 직접 사용하지 않고, enum을 사용하여 **의미를 명확하게 전달할 수 있음**
- 타입 안정성 : 잘못된 값이 사용되는 것을 방지
- 유지 보수 용이성 : 새로운 상수를 추가하거나 기존 상수를 변경할 때 코드 전체를 쉽게 수정 가능

#### Enum 사용 예시

enum 정의 

```java
// 1. 기본 enum
public enum B_day {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
}

// 2. 필드와 생성자가 있는 enum
public enum B_day_2 {
    SUNDAY("Holiday"),
    MONDAY("Workday"),
    TUESDAY("Workday");

    // 필드
    private String desc;

    // 생성자
    B_day_2(String desc) {
        this.desc = desc;
    }

    // 메서드
    public String getDesc() {
        return desc;
    }
}
```

enum 사용

```java
public class B_enum {

    public static void exam1() {
        B_day today = B_day.MONDAY;

        switch (today) {
            case MONDAY:
                System.out.println("Today is Monday");
                break;
            case TUESDAY:
                System.out.println("Today is Tuesday");
                break;
            // ...
        }

    }

    public static void exam2() {
        // 모든 enum값 순회
        for (B_day_2 day : B_day_2.values()) {
            System.out.println( day + " : " + day.getDesc() + " : " + day.name() );
        }

    }

    static void main(String[] args) {
        exam2();
    }
}
```

`B_day_2`는 enum 상수 하나하나를 **각각의 객체처럼 취급**하고, 상수를 선언할 때 생성자를 호출해서 각 객체의 `desc` 값을 초기화하는 구조

- **enum 상수는 해당 enum 클래스의 인스턴스**이고, 각 상수는 선언될 때 생성자를 통해 필드 값을 초기화함
- **enum은 각 상수가 프로그램 내에서 하나만 존재하도록 보장**되기 때문에 **싱글톤 디자인 패턴**의 성질을 가짐. 즉, `B_day_2.SUNDAY`는 여러 번 참조해도 항상 같은 하나의 객체
- 싱글톤 패턴 : 어떤 클래스의 객체를 **딱 하나만 만들도록 제한하는 설계 방식**

```java
B_day_2 d1 = B_day_2.SUNDAY;
B_day_2 d2 = B_day_2.SUNDAY;
System.out.println(d1 == d2); // true
```

`d1`과 `d2`는 둘 다 같은 `SUNDAY` 객체를 가리킴.