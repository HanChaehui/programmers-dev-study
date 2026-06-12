# Collections (가변 길이 ↔ 배열)

---

# 1. List

- 순서가 있는 요소들의 컬렉션을 나타내는 **인터페이스**
- List 인터페이스를 구현하는 대표적인 클래스 : **ArrayList, LinkedList, Vector, Stack** 등
- List는 **순서 구분**, **중복 허용**, **인덱스로 요소 접근** 가능

## List - 주요 메서드

- `add(E e)` : 리스트에 요소 추가
- `get(int index)` : 인덱스에 있는 요소 반환
- `remove(int index)` : 인덱스에 있는 요소 제거
- `size()` : 리스트의 요소 갯수 반환
- `contains(Object o)` : 리스트에 특정 요소가 포함되어 있는지 확인
- `clear()` : 리스트의 모든 요소 제거

---

## 1-1. ArrayList

배열 기반의 리스트로, 인덱스로 빠른 접근이 가능하지만 중간 삽입/삭제가 느림

#### ArrayList - 메서드

```java
List<String> list = new ArrayList<>();

// 요소 추가
list.add("arraylist");

// 특정 인덱스에  요소 추가
list.add(1, "특정 인덱스에 요소 추가");

// 특정 인덱스 요소 접근
String part = list.get(0);

// 특정 인덱스 요소 제거
list.remove(2);

// 리스트의 크기 확인
int size = list.size();

// 특정 요소가 리스트에 포함되어 있는지 확인
boolean check = list.contains("banana");

// 리스트의 모든 요소 제거
list.clear();
```

#### ArrayList - 순회 방법

```java
				// 순회 방법 1 : for 루프 사용
        for ( int i = 0; i < list.size(); i++ ) {
            System.out.println(list.get(i));
        }

        // 순회 방법 2 : 향상된 for 루프 사용
        for ( String fruit : list ) {
            System.out.println(fruit);
        }

        // 순회 방법 3 : Iterator 사용
        Iterator<String> iterator = list.iterator();
        while ( iterator.hasNext() ) {
            String element = iterator.next();
            System.out.println(element);
        }

        // 순회 방법 4 : ListIterator 사용 (양방향 순회 가능)
        ListIterator<String> stringListIterator = list.listIterator();
        // 정방향
        while (stringListIterator.hasNext()) {
            String element = stringListIterator.next();
            System.out.println(element);
        }
        // 역방향
        while (stringListIterator.hasPrevious()) {
            String element = stringListIterator.previous();
            System.out.println(element);
        }
```

## 1-2. LinkedList

노드 기반의 리스트로, 삽입/삭제가 빠르지만 인덱스를 통한 접근이 느림 (메모리 연속적 X)

#### LinkedList - 메서드

```java
List<String> list = new LinkedList<>();

// 요소 추가
list.add("arraylist");

// 특정 인덱스에  요소 추가
list.add(1, "특정 인덱스에 요소 추가");

// 특정 인덱스 요소 접근
String part = list.get(0);

// 특정 인덱스 요소 제거
list.remove(2);

// 마지막 요소 제거
list.removeLast();

// 리스트의 크기 확인
int size = list.size();

// 특정 요소가 리스트에 포함되어 있는지 확인
boolean check = list.contains("banana");

// 리스트의 모든 요소 제거
list.clear();
```

#### LinkedList - 순회 방법

```java
				// 순회 방법 1 : for 루프 사용
        for ( int i = 0; i < list.size(); i++ ) {
            System.out.println(list.get(i));
        }

        // 순회 방법 2 : 향상된 for 루프 사용
        for ( String fruit : list ) {
            System.out.println(fruit);
        }

        // 순회 방법 3 : Iterator 사용
        Iterator<String> iterator = list.iterator();
        while ( iterator.hasNext() ) {
            String element = iterator.next();
            System.out.println(element);
        }

        // 순회 방법 4 : ListIterator 사용 (양방향 순회 가능)
        ListIterator<String> stringListIterator = list.listIterator();
        // 정방향
        while (stringListIterator.hasNext()) {
            String element = stringListIterator.next();
            System.out.println(element);
        }
        // 역방향
        while (stringListIterator.hasPrevious()) {
            String element = stringListIterator.previous();
            System.out.println(element);
        }
```

## 1-3. Stack

**후입선출(LIFO) 구조**를 가지며, 요소를 스택에 추가하고 제거하는 데에 사용

(Stack은 Vector를 상속받고, Vector는 List 인터페이스를 구현하므로 Stack도 add, remove, get 같은 List/Vector 계열 메서드를 사용할 수 있다. 다만 스택의 LIFO 원칙을 지키려면 push, pop, peek 사용이 적절하다.)

#### Stack - 주요 메서드

- `push()` : 맨 위에 요소 삽입
- `pop()` : 맨 위 요소 제거
- `peek()` : 맨 위 요소 확인
- `empty()` : 스택이 비어있는지 확인

```java
Stack<String> stack = new Stack<>();

// PUSH
stack.push("apple");
stack.push("banana");

// POP
String topElement = stack.pop();
System.out.println("topElement : " + topElement);

// PEEK
String peekElement = stack.peek();
System.out.println("peekElement : " + peekElement);

// EMPTY
boolean isEmpty = stack.empty();
System.out.println("isEmpty : " + isEmpty);
```

#### Stack - 순회 방법

```java
				// 순회 방법 1 : for 루프 사용
        for ( int i = 0; i < stack.size(); i++ ) {
            System.out.println( stack.get(i) );
        }

        // 순회 방법 2 : 향상된 for 루프 사용
        for (String element : stack) {
            System.out.println("element : " + element);
        }

        // 순회 방법 3 : Iterator 사용
        Iterator<String> iterator = stack.iterator();
        while ( iterator.hasNext() ) {
            String element = iterator.next();
            System.out.println("element : " + element);
        }

        // 순회 방법 4 : ListIterator 사용 (양방향 순회 가능)
        ListIterator<String> stringListIterator = stack.listIterator();
        // 정방향
        while (stringListIterator.hasNext()) {
            String element = stringListIterator.next();
            System.out.println("element : " + element);
        }
        // 역방향
        while (stringListIterator.hasPrevious()) {
            String element = stringListIterator.previous();
            System.out.println("element : " + element);
        }

        // 순회 방법 5 : pop()을 사용한 순회 (스택의 특성 활용)
        while ( !stack.empty() ) {
            String element = stack.pop();
            System.out.println("element : " + element);
        }
```

---

# 2. Map

- **키-값의 쌍**을 저장하는 자료구조
- **중복된 키를 허용하지 않으며**, 각 키는 하나의 값에 매핑됨
- 만약 동일한 키를 다시 추가하면 기존 값을 덮어 씌움
- Map 인터페이스를 구현하는 대표적인 클래스 : **HashMap**, **TreeMap**
- 특정 키를 사용하여 값에 빠르게 접근 가능

## Map - 주요 메서드

- `put(K key, V value)` : 키-값을 추가
만약 동일한 키가 이미 존재한다면 해당 키의 값이 새 값으로 대체
- `get(Object key)` : 키에 대응하는 값을 반환, 해당 키가 존재하지 않으면 null을 반환
- `remove(Object key)` : 특정 키에 대응하는 키-값 쌍 제거
- `containsKey(Object key)` : 맵에 특정 키가 포함되어 있는지 확인
- `containsValue(Object value)` : 맵에 특정 값이 포함되어 있는지 확인
- `size()` : 맵에 저장된 키-값 쌍의 수를 반환
- `clear()` : 맵의 모든 요소를 제거

## 2-1. HashMap (순서 x)

- 해시 기반: HashMap은 내부적으로 **해시 테이블**을 사용하여 데이터를 저장
- 빠른 성능: 일반적으로 키를 통한 검색, 삽입, 삭제 작업이 O(1)의 시간 복잡도를 가짐
- 순서 보장 없음: 요소들이 **저장된 순서를 보장하지 않음**

#### HashMap - 메서드

```java
Map<String, Integer> hashMap = new HashMap<>();

// 요소 추가
hashMap.put("apple", 10);
hashMap.put("banana", 20);
hashMap.put("orange", 30);
hashMap.put("apple", 40);

System.out.println(hashMap);

// 특정 키의 값 얻기
int value = hashMap.get("apple");
System.out.println("value : " + value);

// 요소 제거
hashMap.remove("apple");
System.out.println(hashMap);

// 키의 존재 여부
boolean containsed = hashMap.containsKey("orange");
System.out.println("containsed : " + containsed);
```

#### HashMap - 순회 방법

```java
				// 순회 방법 1 : entrySet() 방식
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        // 순회 방법 2 : keySet() 방식
        for (String key : hashMap.keySet()) {
            System.out.println(key + " : " + hashMap.get(key));
        }

        // 순회 방법 3 : values() 방법
        for ( Integer val : hashMap.values() ) {
            System.out.println(" value : " + val);
        }
```

## 2-2. TreeMap (순서 o)

- 이진 검색 트리 기반 : TreeMap은 내부적으로 **레드-블랙 트리 구조**를 사용하여 데이터를 저장
- 정렬된 순서 : **키 기준으로 요소들이 자동으로 정렬**(기본적으로 오름차순)
- 성능 : 삽입, 삭제, 검색 작업이 O(log n)의 시간 복잡도를 가짐

대부분의 경우는 HashMap이 기본 선택

키로 빠르게 찾고 저장만 하면 된다, 순서는 상관 없다 → HashMap

키가 정렬되어 있어야 하거나, 범위/이웃 키 탐색이 필요 → TreeMap

#### TreeMap - 메서드

```java
TreeMap<String, Integer> treeMap = new TreeMap<>();

treeMap.put("apple", 10);
treeMap.put("banana", 20);
treeMap.put("orange", 30);

System.out.println(treeMap);

// 첫 번째 요소의 키 얻기
String firstKey = treeMap.firstKey();
System.out.println("firstKey : " + firstKey);

// 마지막 요소의 값 얻기
int value = treeMap.lastEntry().getValue();
System.out.println("value : " + value);
```