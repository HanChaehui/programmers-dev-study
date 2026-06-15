# TreeMap 직접 만들기 (이진 검색 트리 구현 · 제네릭 없는 버전)

> Java의 `TreeMap`을 직접 구현해 봅니다. **이진 검색 트리(BST)** 를 손으로 만들면서,
> 왜 키가 **자동으로 정렬**되는지와 탐색이 왜 **O(log n)** 인지를 체득합니다.
> **이 버전은 제네릭을 쓰지 않고, 키는 `String` / 값은 `Integer`로 고정**합니다.
> **아래 Step을 순서대로 따라가면, 마지막 Step에서 나만의 TreeMap이 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 0. 먼저 알아둘 점

진짜 `TreeMap`은 **레드-블랙 트리**라는 "스스로 균형을 맞추는 트리"를 씁니다. 하지만 그 균형 로직은 매우 복잡해요. 그래서 이 과제에서는 그 바탕이 되는 **이진 검색 트리(BST)** 까지 직접 만듭니다.
- BST만으로도 **정렬된 순서**와 **평균 O(log n)** 탐색을 그대로 경험할 수 있어요.
- "어떤 상황에서 BST가 느려지는지", "그래서 왜 균형이 필요한지"는 마지막에 다루고,
  **레드-블랙 균형 잡기는 도전 과제**로 둡니다.

---

## 1. 무엇을 만드나요?

`put`(저장), `get`(조회), `remove`(삭제)가 되고, **키가 정렬되어 출력**되는 `MyTreeMap`을 만듭니다.

```java
MyTreeMap map = new MyTreeMap();
map.put("banana", 2);
map.put("apple", 1);
map.put("cherry", 3);

map.printSorted();          // [apple=1] [banana=2] [cherry=3]  ← 넣은 순서와 무관하게 정렬!
System.out.println(map.get("banana"));   // 2
System.out.println(map.firstKey());      // apple (가장 작은 키)
System.out.println(map.lastKey());       // cherry (가장 큰 키)
```

> HashMap과 가장 다른 점: **키가 항상 정렬된 순서로 나온다는 것**이에요.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 이진 검색 트리(BST) | 왼쪽 < 부모 < 오른쪽 구조로 저장 |
| 키 비교 | `compareTo`로 좌/우 방향 결정 |
| 중위 순회 = 정렬 | 왼→자신→오른 순서로 방문하면 정렬되어 나옴 |
| O(log n)의 원리 | 트리 높이만큼만 내려가면 됨 |
| 균형의 중요성 | 치우치면 O(n)이 되는 이유 |

---

## 3. 핵심 개념

### (1) 이진 검색 트리(BST) — 왼쪽 < 부모 < 오른쪽
각 노드는 자식을 최대 둘 가집니다. 규칙은 단순해요: **왼쪽 자식은 나보다 작고, 오른쪽 자식은 나보다 크다.**

```
            banana
           /      \
       apple      cherry
```
그래서 키를 찾을 때 비교 한 번으로 **왼쪽/오른쪽 중 한쪽만** 내려가면 됩니다. 절반씩 줄어드니 빨라요.

### (2) 정렬의 비밀 — 중위 순회(in-order)
**왼쪽 → 자신 → 오른쪽** 순서로 방문하면, 키가 **오름차순**으로 나옵니다.
위 트리를 중위 순회하면 `apple → banana → cherry`. 이게 `TreeMap`이 "정렬된 순서"인 이유예요.

### (3) O(log n)과 균형
균형 잡힌 트리는 높이가 약 `log n`이라, 탐색·삽입·삭제가 **O(log n)** 입니다.
하지만 **정렬된 데이터를 순서대로 넣으면**(예: a, b, c, d...) 한쪽으로만 쭉 자라서 사실상 **연결 리스트(O(n))** 가 돼요.

```
a
 \
  b
   \
    c   ← 균형이 깨진 트리 (느림)
```
그래서 진짜 `TreeMap`은 **레드-블랙 트리**로 자동 균형을 맞춰 항상 O(log n)을 보장합니다. (이 과제는 BST까지, 균형은 도전 과제)

---

## 4. 파일 구조

| 파일 | 역할 |
|------|------|
| `MyTreeMap.java` | 트리맵 본체. 노드(좌/우) + put/get/remove + 정렬 출력 |
| `Main.java` | 만든 `MyTreeMap`을 테스트 |

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인 방법**이 있습니다.

---

### Step 1. 노드 + 골격 만들기 (`MyTreeMap.java`)

**목표**: 자식을 둘(left/right) 가지는 노드와, 루트를 가진 트리맵의 뼈대를 만든다.

**할 일**
1. `Node` 내부 클래스: `key`(String), `value`(Integer), `left`, `right`.
2. `MyTreeMap`: 루트 `root`, 개수 `size`.

<details>
<summary>💡 힌트 보기</summary>

```java
public class MyTreeMap {

    static class Node {
        String key;
        Integer value;
        Node left;      // 나보다 작은 키들
        Node right;     // 나보다 큰 키들
        Node(String key, Integer value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;   // 트리의 시작점
    private int size = 0;
}
```

</details>

**확인**: 컴파일 에러가 없으면 OK.

---

### Step 2. 저장 — put (`MyTreeMap.java`)

**목표**: 키-값을 알맞은 위치에 저장한다. 같은 키면 값 갱신, 작으면 왼쪽, 크면 오른쪽.

**할 일**
1. 루트부터 시작해 `key.compareTo(현재 키)`로 비교.
2. 작으면 왼쪽, 크면 오른쪽으로 내려가다가 빈 자리에 새 노드를 단다.
3. 같은 키면 값만 갱신.

<details>
<summary>💡 힌트 보기</summary>

재귀로 짜면 깔끔해요. "이 자리에 넣고, 바뀐 서브트리를 돌려준다"는 패턴입니다.

```java
public void put(String key, Integer value) {
    root = putNode(root, key, value);
}

private Node putNode(Node node, String key, Integer value) {
    if (node == null) {                 // 빈 자리 → 새 노드
        size++;
        return new Node(key, value);
    }
    int cmp = key.compareTo(node.key);
    if (cmp < 0)      node.left  = putNode(node.left, key, value);   // 작으면 왼쪽
    else if (cmp > 0) node.right = putNode(node.right, key, value);  // 크면 오른쪽
    else              node.value = value;                           // 같으면 값 갱신
    return node;
}
```

키 비교는 `compareTo`로! (음수면 작다, 0이면 같다, 양수면 크다)

</details>

**확인**: 여러 개 넣은 뒤 Step 4의 정렬 출력으로 확인해요. 같은 키 두 번 넣으면 값이 갱신되고 개수는 안 늘어야 합니다.

---

### Step 3. 조회 — get (`MyTreeMap.java`)

**목표**: 키로 값을 찾는다. 없으면 `null`.

**할 일**: 루트부터 `compareTo`로 비교하며 왼쪽/오른쪽으로 내려가, 같은 키를 만나면 값 반환.

<details>
<summary>💡 힌트 보기</summary>

```java
public Integer get(String key) {
    Node n = root;
    while (n != null) {
        int cmp = key.compareTo(n.key);
        if (cmp < 0)      n = n.left;    // 작으면 왼쪽으로
        else if (cmp > 0) n = n.right;   // 크면 오른쪽으로
        else              return n.value; // 찾음!
    }
    return null;   // 끝까지 못 찾음
}
```

</details>

**확인**: 저장한 키는 값이 나오고, 없는 키는 `null`이 나오면 성공.

---

### Step 4. 정렬 출력 — 중위 순회 (`MyTreeMap.java`)

**목표**: 키가 **정렬된 순서**로 출력되게 한다. (TreeMap의 핵심!)

**할 일**: **왼쪽 → 자신 → 오른쪽** 순서로 재귀 방문하며 출력.

<details>
<summary>💡 힌트 보기</summary>

```java
public void printSorted() {
    inOrder(root);
    System.out.println();
}

private void inOrder(Node node) {
    if (node == null) return;
    inOrder(node.left);                                         // 1) 왼쪽 먼저
    System.out.print("[" + node.key + "=" + node.value + "] "); // 2) 자신
    inOrder(node.right);                                        // 3) 오른쪽
}
```

이 "왼 → 자신 → 오른" 순서가 바로 정렬을 만들어내는 마법이에요.

</details>

**확인**: 일부러 순서를 섞어 넣어도(banana, apple, cherry) 출력은 항상 정렬(apple, banana, cherry)되면 성공!

---

### Step 5. 보조 메서드 — size / containsKey / 최소·최대 키 (`MyTreeMap.java`)

**목표**: 개수, 키 존재 여부, 그리고 가장 작은/큰 키를 구한다.

**할 일**
1. `size()` — 개수 반환.
2. `containsKey(key)` — 직접 탐색해 존재 여부 반환.
3. `firstKey()` / `lastKey()` — **맨 왼쪽**이 최소, **맨 오른쪽**이 최대.

<details>
<summary>💡 힌트 보기</summary>

```java
public int size() { return size; }

public boolean containsKey(String key) {
    Node n = root;
    while (n != null) {
        int cmp = key.compareTo(n.key);
        if (cmp < 0)      n = n.left;
        else if (cmp > 0) n = n.right;
        else              return true;
    }
    return false;
}

public String firstKey() {           // 가장 작은 키 = 맨 왼쪽
    if (root == null) return null;
    Node n = root;
    while (n.left != null) n = n.left;
    return n.key;
}

public String lastKey() {            // 가장 큰 키 = 맨 오른쪽
    if (root == null) return null;
    Node n = root;
    while (n.right != null) n = n.right;
    return n.key;
}
```

BST에선 왼쪽으로만 끝까지 가면 최소, 오른쪽으로만 끝까지 가면 최대예요.

</details>

**확인**: `firstKey()`가 정렬 출력의 맨 앞 키와, `lastKey()`가 맨 뒤 키와 같으면 성공.

---

### Step 6. 삭제 — remove (`MyTreeMap.java`) · 가장 어려운 부분

**목표**: 키에 해당하는 노드를 트리에서 제거한다. 트리 규칙(왼<부모<오른)을 유지하면서!

삭제는 **세 가지 경우**로 나뉩니다.
1. **자식이 없는 노드(잎)**: 그냥 떼어낸다.
2. **자식이 하나**: 그 자식을 내 자리에 올린다.
3. **자식이 둘**: 오른쪽 서브트리에서 **가장 작은 키(후계자)** 를 찾아 내 자리에 복사하고, 그 후계자를 삭제한다.

<details>
<summary>💡 힌트 보기</summary>

먼저 키가 있는지 확인하고 개수를 줄인 뒤, 재귀로 노드를 떼어냅니다.

```java
public Integer remove(String key) {
    Integer old = get(key);
    if (old == null) return null;   // 없으면 아무것도 안 함
    root = removeNode(root, key);
    size--;
    return old;
}

private Node removeNode(Node node, String key) {
    if (node == null) return null;
    int cmp = key.compareTo(node.key);
    if (cmp < 0)      node.left  = removeNode(node.left, key);
    else if (cmp > 0) node.right = removeNode(node.right, key);
    else {
        // 찾음! 경우를 나눈다
        if (node.left == null)  return node.right;  // 경우1·2 (왼쪽 없음)
        if (node.right == null) return node.left;   // 경우2 (오른쪽 없음)

        // 경우3: 자식 둘 → 오른쪽의 최소(후계자)로 대체
        Node succ = node.right;
        while (succ.left != null) succ = succ.left;
        node.key = succ.key;
        node.value = succ.value;
        node.right = removeNode(node.right, succ.key); // 후계자 제거
    }
    return node;
}
```

왜 "오른쪽의 최소"일까요? 그 값이 **나보다는 크지만 오른쪽 중에선 가장 작아서**, 내 자리에 와도 왼<부모<오른 규칙이 깨지지 않기 때문이에요.

</details>

**확인**: 잎 노드, 자식 하나인 노드, 자식 둘인 노드를 각각 삭제해보고, 매번 정렬 출력이 여전히 올바른 순서면 성공.

---

### Step 7. 마무리 점검 (완성!)

**점검 항목**
- [ ] 넣은 순서와 무관하게 `printSorted()`가 정렬되어 나오는가?
- [ ] 같은 키 put 시 값이 갱신되고 개수는 안 늘어나는가?
- [ ] 없는 키 `get`/`remove`가 `null`을 반환하는가?
- [ ] 세 가지 삭제 경우가 모두 정상 동작하는가?
- [ ] `firstKey`/`lastKey`가 최소/최대 키를 정확히 주는가?

여기까지 통과하면 **나만의 TreeMap 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] 이진 검색 트리 구조(왼<부모<오른)를 만들었다
- [ ] `compareTo`로 키를 비교해 좌/우로 탐색했다
- [ ] 중위 순회로 정렬된 순서를 얻었다
- [ ] 트리 높이만큼만 내려가면 되니 빠르다는 걸 이해했다
- [ ] 치우치면 O(n)이 되는 이유와, 그래서 균형이 필요한 이유를 설명할 수 있다

---

## 7. 최종 완성 체크리스트

- [ ] `MyTreeMap.java` — Node(left/right) + root + 해당 메서드들
- [ ] put / get / remove / size / containsKey / printSorted / firstKey / lastKey
- [ ] 삭제 세 경우 모두 처리
- [ ] `Main.java`에서 정렬·삭제 시나리오 테스트

---

## 8. (선택) 도전 과제

1. **불균형 실험**: a, b, c, d, e를 순서대로 넣어보고 트리가 한쪽으로 치우치는 걸 확인 → "왜 균형이 필요한가" 체감
2. **레드-블랙 트리로 균형 잡기**: 회전(rotation)과 재색칠로 항상 O(log n) 보장 (진짜 TreeMap에 한 걸음 더!) — 난이도 ★★★
3. **범위 검색**: `headMap`(특정 키 미만), `subMap`(범위) 같은 TreeMap 고유 기능 구현
4. **ceilingKey / floorKey**: 주어진 키 이상/이하인 가장 가까운 키 찾기
5. **HashMap과 비교**: 같은 데이터를 MyHashMap과 MyTreeMap에 넣고, 출력 순서·속도가 어떻게 다른지 비교
6. **제네릭으로 일반화 (배운 뒤!)**: 키를 `Comparable`을 구현한 어떤 타입이든 받게 `<K, V>`로 확장