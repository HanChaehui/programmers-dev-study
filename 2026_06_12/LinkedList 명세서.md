# 과제 2 — 노드로 `MyLinkedList` 직접 만들기

## 목표

각 노드가 `prev`/`next`로 앞뒤 노드의 **주소**를 들고 "논리적으로" 연결되는 구조를 직접 만든다. **왜 삽입/삭제는 빠르고 인덱스 접근(`get`)은 느린지**를 코드로 체득한다.

## 준비물

`MyLinkedListTest.java` 스켈레톤의 `// TODO`를 단계별로 채운다. `printLinks()`는 **완성된 채로** 줬으니, 단계마다 실행해 연결 상태를 눈으로 보며 진행한다.

## 핵심 아이디어

- 노드 한 칸 = `[prev][data][next]`. prev/next에 앞뒤 노드의 주소를 담는다.
- `head`(첫 노드), `tail`(마지막 노드)만 들고 있으면 전체를 타고 다닐 수 있다.
- 삽입/삭제는 **연결(화살표)만 바꾸면** 된다 → 데이터 이동이 없다 → 빠르다.
- index번째를 찾으려면 head부터 **한 칸씩 걸어가야** 한다 → 느리다.

---

## 단계별 진행

### Step 1. `Node` 클래스

생성자에서 `this.data = data;` 만 채운다. (prev/next는 처음엔 null)

### Step 2. 필드 이해 (작성돼 있음)

`head`, `tail`, `size`가 왜 필요한지 생각해본다.

### Step 3. `addLast(String data)`

- 비어 있으면(`head == null`) `head = tail = node;`
- 아니면 `node.prev = tail; tail.next = node; tail = node;`
- 마지막에 `size++`
- ✅ 확인: "가","나","다" addLast 후 `printLinks()`가 `[null <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]`

### Step 4. `printLinks()` (제공됨 — 읽고 이해만)

직접 만들 필요는 없다. head부터 `next`를 타고 가며 각 노드의 `[prev <- data -> next]`를 출력한다. 이게 앞으로 단계마다 쓸 **디버깅 도구**가 된다.

### Step 5. `addFirst(String data)`

- 비어 있으면 `head = tail = node;`
- 아니면 `node.next = head; head.prev = node; head = node;`
- `size++`
- ✅ 확인: `addFirst("앞")` 하면 맨 앞에 `[null <- 앞 -> 가]`가 붙는다. **다른 노드는 그대로**인 게 포인트.

### Step 6. `nodeAt(int index)` + `get(int index)`

- `nodeAt`: `head`부터 시작해 `next`로 `index`번 이동한 노드를 반환.
- `get`: `nodeAt(index).data` 반환.
- 🤔 생각: 1000번째 노드를 꺼내려면 몇 번 이동해야 할까?

### Step 7. `insert(int index, String data)` — 핵심

- `index == 0`이면 `addFirst`, `index == size`이면 `addLast`로 넘긴다.
- 그 외:
    1. `Node next = nodeAt(index); Node prev = next.prev;`
    2. 새 노드의 양옆 연결: `node.prev = prev; node.next = next;`
    3. 양옆이 새 노드를 보게: `prev.next = node; next.prev = node;`
    4. `size++`
- ✅ 확인: `insert(2, "끼움")` 하면 2번 자리 **양옆 연결만** 바뀐다.

---

## 도전 과제 (선택)

`remove(int index)` : 삭제할 노드의 `prev`와 `next`를 **서로 직접 연결**하고 `size--`. (맨 앞/맨 뒤를 지울 땐 `head`/`tail` 갱신에 주의)

## 제출 / 확인

완성한 `MyLinkedListTest.java`를 실행했을 때 아래처럼 나오면 통과.

```
addLast 후: [null <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]
addFirst 후: [null <- 앞 -> 가] [앞 <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]
get(2) = 나
insert 후: [null <- 앞 -> 가] [앞 <- 가 -> 끼움] [가 <- 끼움 -> 나] [끼움 <- 나 -> 다] [나 <- 다 -> null]
```

```java
public class MyLinkedListTest {  
    public static void main(String[] args) {  
        MyLinkedList list = new MyLinkedList();  
  
        // --- Step 3 + 4 확인 ---  
        list.addLast("가");  
        list.addLast("나");  
        list.addLast("다");  
        System.out.print("addLast 후: ");  
        list.printLinks();  
        // 기대: [null <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]  
  
        // --- Step 5 확인 ---        list.addFirst("앞");  
        System.out.print("addFirst 후: ");  
        list.printLinks();  
        // 기대: [null <- 앞 -> 가] [앞 <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]  
  
        // --- Step 6 확인 ---        System.out.println("get(2) = " + list.get(2));   // 기대: 나  
  
        // --- Step 7 확인 ---        list.insert(2, "끼움");  
        System.out.print("insert 후: ");  
        list.printLinks();  
        // 기대: [null <- 앞 -> 가] [앞 <- 가 -> 끼움] [가 <- 끼움 -> 나] [끼움 <- 나 -> 다] [나 <- 다 -> null]  
    }  
}  
  
  
class MyLinkedList {  
  
    // [Step 1] 노드 한 칸: 데이터 + 앞/뒤 노드의 주소  
    static class Node {  
        String data;  
        Node prev;   // 앞 노드  
        Node next;   // 뒤 노드  
        Node(String data) {  
            // TODO: this.data 를 설정하세요.  
        }  
    }  
  
    // [Step 2] 필드 (작성돼 있음)  
    private Node head;   // 첫 노드  
    private Node tail;   // 마지막 노드  
    private int size;  
  
    // [Step 3] 맨 뒤에 추가  
    void addLast(String data) {  
        Node node = new Node(data);  
        // TODO: head == null 이면 head = tail = node;  
        //       아니면 node.prev = tail;  tail.next = node;  tail = node;  
        //       마지막에 size++  
    }  
  
    // [Step 4] 연결 상태 출력 (제공됨 — 읽고 이해만 하세요)  
    void printLinks() {  
        Node cur = head;  
        while (cur != null) {  
            String p = (cur.prev == null) ? "null" : cur.prev.data;  
            String n = (cur.next == null) ? "null" : cur.next.data;  
            System.out.print("[" + p + " <- " + cur.data + " -> " + n + "] ");  
            cur = cur.next;  
        }  
        System.out.println();  
    }  
  
    // [Step 5] 맨 앞에 추가  
    void addFirst(String data) {  
        Node node = new Node(data);  
        // TODO: head == null 이면 head = tail = node;  
        //       아니면 node.next = head;  head.prev = node;  head = node;  
        //       마지막에 size++  
    }  
  
    // [Step 6] index번째 노드 찾기  
    private Node nodeAt(int index) {  
        // TODO: head 부터 시작해서 next 로 index 번 이동한 노드를 반환하세요.  
        return null;  
    }  
  
    String get(int index) {  
        // TODO: nodeAt(index).data 를 반환하세요.  
        return null;  
    }  
  
    // [Step 7] index 위치에 삽입 (양옆 연결만 바꾸기)  ★핵심★  
    void insert(int index, String data) {  
        // TODO: index == 0 이면 addFirst, index == size 이면 addLast 로 처리.  
        //       그 외:  
        //         Node next = nodeAt(index);  Node prev = next.prev;  
        //         Node node = new Node(data);  
        //         node.prev = prev;  node.next = next;  
        //         prev.next = node;  next.prev = node;  
        //         size++  
    }  
  
    // [도전] index 위치 노드 삭제  
    void remove(int index) {  
        // TODO (도전): 삭제할 노드의 prev 와 next 를 서로 연결하고 size--  
        //              (맨 앞/맨 뒤 삭제 시 head/tail 갱신 주의)  
    }  
  
    int size() { return size; }  
}
```