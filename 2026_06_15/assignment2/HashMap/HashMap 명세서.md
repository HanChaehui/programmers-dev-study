# HashMap 직접 만들기 (해시 테이블 구현 · 제네릭 없는 버전)

> Java의 `HashMap`을 직접 구현해 봅니다. **배열 + 해시 함수 + 충돌 처리(체이닝)** 를 손으로 만들면서, 왜 검색/저장이 빠른지(`O(1)`)와 왜 순서가 보장되지 않는지를 체득합니다. **이 버전은 제네릭을 쓰지 않고, 키는 `String` / 값은 `Integer`로 고정**합니다. **아래 Step을 순서대로 따라가면, 마지막 Step에서 나만의 HashMap이 완성됩니다.**
> 
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 1. 무엇을 만드나요?

`put`(저장), `get`(조회), `remove`(삭제)가 되는 나만의 `MyHashMap`을 만듭니다. 키는 단어(String), 값은 숫자(Integer)라고 생각하면 돼요. (예: 단어 → 개수)

```java
MyHashMap map = new MyHashMap();
map.put("apple", 1);
map.put("banana", 2);
System.out.println(map.get("apple"));    // 1
System.out.println(map.get("cherry"));   // null
map.remove("apple");
System.out.println(map.get("apple"));    // null
```

---

## 2. 학습 목표

|개념|어디서 배우나|
|---|---|
|해시 함수|키를 배열 인덱스로 변환|
|버킷 배열|데이터를 담는 배열(bucket)|
|충돌(collision)과 체이닝|같은 인덱스에 여러 키 → 연결 리스트로 해결|
|O(1)의 원리|인덱스로 바로 접근하는 빠른 저장/조회|
|순서가 없는 이유|인덱스가 해시값으로 정해지기 때문|

---

## 3. 핵심 개념

### (1) 해시 테이블 = 배열 + 해시 함수

키를 그대로 배열 인덱스로 쓸 순 없어요(키가 문자열이니까). 그래서 **해시 함수**로 키를 숫자로 바꾼 뒤, 배열 크기로 나눈 나머지를 **인덱스**로 씁니다.

```
"apple" --(hashCode)--> 93029210 --(% 16)--> 10번 칸
```

인덱스를 바로 계산하니까 **한 번에** 그 칸으로 가서 저장/조회할 수 있어요. 이게 `O(1)`의 비밀입니다.

### (2) 충돌(collision)과 체이닝

서로 다른 키가 **같은 인덱스**로 계산될 수 있어요. 이걸 충돌이라고 합니다. 해결책은 그 칸에 값을 하나만 두지 말고, **연결 리스트로 줄줄이 매다는 것**(체이닝)이에요.

```
인덱스 10 → ["apple":1] → ["grape":7] → null
```

조회할 땐 그 칸의 리스트를 따라가며 키가 일치하는 노드를 찾습니다.

### (3) 순서가 없는 이유

저장 위치(인덱스)가 **해시값으로 정해지기 때문에**, 넣은 순서와 무관하게 흩어져 저장됩니다. 그래서 `HashMap`은 순서를 보장하지 않아요.

---

## 4. 파일 구조

|파일|역할|
|---|---|
|`MyHashMap.java`|해시맵 본체. 버킷 배열 + 해시 함수 + put/get/remove. (`Node`는 내부 클래스)|
|`Main.java`|만든 `MyHashMap`을 테스트|

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인 방법**이 있습니다.

---

### Step 1. 노드 + 골격 만들기 (`MyHashMap.java`)

**목표**: 데이터 한 칸(노드)과, 버킷 배열을 가진 해시맵의 뼈대를 만든다.

**할 일**

1. `Node` 내부 클래스: `key`(String), `value`(Integer), 충돌 시 이어붙일 `next`.
2. `MyHashMap`: 버킷 배열 `Node[] buckets`, 용량 `capacity`, 개수 `size`.
3. 생성자에서 배열을 만든다.

<details> <summary>💡 힌트 보기</summary>

```java
public class MyHashMap {

    // 데이터 한 칸 (연결 리스트의 노드)
    static class Node {
        String key;
        Integer value;
        Node next;          // 충돌 시 다음 노드로 연결
        Node(String key, Integer value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node[] buckets;
    private int capacity = 16;
    private int size = 0;

    public MyHashMap() {
        buckets = new Node[capacity];   // 제네릭이 없어 형변환·경고도 없이 깔끔!
    }
}
```

</details>

**확인**: 컴파일 에러가 없으면 OK.

---

### Step 2. 해시 함수 — 키를 인덱스로 (`MyHashMap.java`)

**목표**: 키를 받아 버킷 배열의 인덱스(0 ~ capacity-1)를 돌려주는 함수를 만든다.

**할 일**: `key.hashCode()`로 숫자를 얻고, 음수가 안 되게 처리한 뒤 `% capacity`.

<details> <summary>💡 힌트 보기</summary>

```java
private int getIndex(String key) {
    return Math.abs(key.hashCode()) % capacity;
}
```

`hashCode()`는 음수일 수 있어서 `Math.abs`로 양수로 만든 뒤 나머지를 구합니다.

</details>

**확인**: 같은 키는 항상 같은 인덱스가 나오는지 출력해 확인해 보세요.

---

### Step 3. 저장 — put (`MyHashMap.java`)

**목표**: 키-값을 저장한다. 같은 키가 이미 있으면 값을 갱신, 없으면 새로 추가.

**할 일**

1. `getIndex(key)`로 들어갈 칸을 찾는다.
2. 그 칸의 리스트를 돌며 **같은 키가 있으면 값만 교체** 후 종료.
3. 없으면 새 노드를 만들어 **리스트 맨 앞에 연결**하고 `size++`.

<details> <summary>💡 힌트 보기</summary>

```java
public void put(String key, Integer value) {
    int idx = getIndex(key);
    Node head = buckets[idx];

    // 같은 키가 이미 있나? → 값만 갱신
    for (Node n = head; n != null; n = n.next) {
        if (n.key.equals(key)) {
            n.value = value;
            return;
        }
    }
    // 없으면 새 노드를 맨 앞에 추가 (체이닝)
    Node node = new Node(key, value);
    node.next = head;
    buckets[idx] = node;
    size++;
}
```

키 비교는 항상 `equals`로! (해시값이 같아도 키가 진짜 같은지 확인해야 함)

</details>

**확인**: 같은 키로 두 번 put 하면 값이 덮어써지고, 다른 키면 둘 다 저장되면 성공.

---

### Step 4. 조회 — get (`MyHashMap.java`)

**목표**: 키로 값을 찾는다. 없으면 `null`.

**할 일**: 인덱스를 구해 그 칸의 리스트를 따라가며 키가 일치하는 노드의 값을 반환.

<details> <summary>💡 힌트 보기</summary>

```java
public Integer get(String key) {
    int idx = getIndex(key);
    for (Node n = buckets[idx]; n != null; n = n.next) {
        if (n.key.equals(key)) {
            return n.value;
        }
    }
    return null;   // 끝까지 못 찾으면 null
}
```

</details>

**확인**: 저장한 키는 값이 나오고, 없는 키는 `null`이 나오면 성공.

---

### Step 5. 충돌 확인 + 보조 메서드 (`MyHashMap.java`)

**목표**: 충돌이 실제로 체이닝으로 처리되는지 확인하고, `size()`/`containsKey()`를 만든다.

**할 일**

1. `size()` — 현재 개수 반환.
2. `containsKey(key)` — 키 존재 여부 (값이 `null`일 수도 있으니 직접 탐색).

<details> <summary>💡 힌트 보기</summary>

```java
public int size() {
    return size;
}

public boolean containsKey(String key) {
    int idx = getIndex(key);
    for (Node n = buckets[idx]; n != null; n = n.next) {
        if (n.key.equals(key)) return true;
    }
    return false;
}
```

</details>

**확인(충돌 실험)**: 키를 많이 넣어 같은 칸에 2개 이상 들어가게 해보세요. 그래도 `get`이 둘 다 정확히 찾아오면 체이닝이 잘 동작하는 거예요.

---

### Step 6. 삭제 — remove (`MyHashMap.java`)

**목표**: 키에 해당하는 노드를 리스트에서 제거한다.

**할 일**

1. 인덱스의 리스트를 돌며 키를 찾는다. (`prev`로 앞 노드를 기억)
2. 첫 노드면 `buckets[idx]`를 다음으로, 중간이면 `prev.next`를 건너뛰게 연결.
3. `size--` 후 값 반환. 못 찾으면 `null`.

<details> <summary>💡 힌트 보기</summary>

```java
public Integer remove(String key) {
    int idx = getIndex(key);
    Node n = buckets[idx];
    Node prev = null;

    while (n != null) {
        if (n.key.equals(key)) {
            if (prev == null) buckets[idx] = n.next;  // 첫 노드 삭제
            else              prev.next = n.next;     // 중간/끝 노드 삭제
            size--;
            return n.value;
        }
        prev = n;
        n = n.next;
    }
    return null;
}
```

</details>

**확인**: 삭제 후 `get`이 `null`을 반환하고 `size`가 줄면 성공.

---

### Step 7. 마무리 점검 (완성!)

**점검 항목**

- [ ] 같은 키 put 시 값이 갱신되는가? (중복 키 안 생김)
- [ ] 충돌이 나도 `get`이 정확히 찾아오는가? (체이닝 동작)
- [ ] 없는 키 `get`/`remove`가 `null`을 반환하는가?
- [ ] `size`가 추가/삭제에 맞게 변하는가?

여기까지 통과하면 **나만의 HashMap 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] 해시 함수로 키를 인덱스로 변환했다
- [ ] 버킷 배열에 데이터를 저장했다
- [ ] 충돌을 연결 리스트(체이닝)로 해결했다
- [ ] put/get/remove가 인덱스로 바로 접근해 빠르다는 걸 이해했다
- [ ] 왜 저장 순서가 보장되지 않는지 설명할 수 있다

---

## 7. 최종 완성 체크리스트

- [ ] `MyHashMap.java` — Node 내부 클래스 + 버킷 배열 + 해시 함수
- [ ] put / get / remove / size / containsKey 구현
- [ ] 충돌 시 체이닝으로 정상 동작
- [ ] `Main.java`에서 다양한 시나리오로 테스트

---

## 8. (선택) 도전 과제

1. **리사이즈(resize)**: 데이터가 많아지면(예: size가 capacity의 75%를 넘으면) 배열을 2배로 늘리고 **모든 노드를 새 인덱스로 다시 배치(rehash)** 하기 → 충돌이 줄어 빨라짐
2. **load factor**: 위 75% 같은 기준값을 상수로 빼서 관리
3. **값 타입 바꾸기**: 값을 `Integer` 대신 `String`으로 바꿔 단어→뜻 사전 만들기
4. **keySet() / values()**: 모든 키/값을 모아 반환하는 메서드
5. **음수 해시 안전 처리**: `Math.abs(Integer.MIN_VALUE)`는 음수가 되는 함정 → `(key.hashCode() & 0x7fffffff) % capacity`로 더 안전하게
6. **제네릭으로 일반화 (제네릭을 배운 뒤!)**: `String`/`Integer` 고정을 `<K, V>`로 바꿔 어떤 타입의 키·값도 담을 수 있게 만들기 → 이게 진짜 `HashMap`에 한 걸음 더 가까워지는 단계예요