# 트리 자료구조 만들기 (이진 트리 + 순회)

> 직접 이진 트리를 만들고, **전위·중위·후위 순회**를 모두 구현해 봅니다.
> 세 순회의 코드는 거의 똑같고 **"루트를 언제 방문하느냐"** 만 다르다는 걸 체득하는 게 목표예요.
> **이 버전은 제네릭 없이 정수(int) 값을 담는 이진 검색 트리(BST)** 로 만듭니다.
> **아래 Step을 순서대로 따라가면, 마지막 Step에서 트리와 세 순회가 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 1. 무엇을 만드나요?

값을 넣어 트리를 만들고, 세 가지 방식으로 전체 노드를 방문(순회)해 출력합니다.

```java
MyTree tree = new MyTree();
int[] values = {50, 30, 70, 20, 40, 60, 80};
for (int v : values) tree.insert(v);

tree.preOrder();    // 전위: 50 30 20 40 70 60 80
tree.inOrder();     // 중위: 20 30 40 50 60 70 80   ← 정렬되어 나옴!
tree.postOrder();   // 후위: 20 40 30 60 80 70 50
```

위 값들로 만들어지는 트리는 이렇게 생겼어요.

```
          50
        /    \
      30      70
     /  \    /  \
   20   40  60   80
```

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 트리 용어 | 루트 / 자식 / 잎(leaf) / 높이 |
| 이진 트리 | 자식이 최대 2개(left/right)인 트리 |
| 재귀로 트리 다루기 | 노드 → 왼쪽 서브트리 → 오른쪽 서브트리 |
| 전위·중위·후위 순회 | 루트를 방문하는 "시점"의 차이 |
| 중위 순회 = 정렬 | BST를 중위 순회하면 오름차순 |

---

## 3. 핵심 개념

### (1) 트리 용어
- **루트(root)**: 맨 위 시작 노드
- **자식(child)**: 한 노드 아래에 달린 노드 (이진 트리는 왼쪽/오른쪽 최대 2개)
- **잎(leaf)**: 자식이 없는 노드
- **서브트리**: 어떤 노드를 루트로 보는 그 아래 전체

### (2) 순회(traversal) = 모든 노드를 빠짐없이 방문하기
이진 트리 순회는 항상 **왼쪽을 오른쪽보다 먼저** 방문해요. 차이는 딱 하나, **루트(자기 자신)를 언제 방문하느냐**입니다.

| 순회 | 방문 순서 | 외우는 법 |
|------|------|------|
| **전위(pre-order)** | **루트** → 왼 → 오 | 루트를 **먼저** |
| **중위(in-order)** | 왼 → **루트** → 오 | 루트를 **가운데** |
| **후위(post-order)** | 왼 → 오 → **루트** | 루트를 **마지막** |

> 그래서 세 순회의 코드는 거의 똑같고, **"루트를 출력하는 줄"의 위치만** 다릅니다. 이게 이 과제의 핵심이에요!

### (3) 각 순회는 언제 쓰나? (감만 잡기)
- **전위**: 트리를 그대로 복사하거나 구조를 저장할 때 (위에서부터)
- **중위**: BST에서 **정렬된 순서**가 필요할 때
- **후위**: 자식을 먼저 처리하고 부모를 나중에 — 트리 삭제, 폴더 용량 계산 등

---

## 4. 파일 구조

| 파일 | 역할 |
|------|------|
| `MyTree.java` | 트리 본체. 노드 + insert + 전위/중위/후위 순회 |
| `Main.java` | 트리를 만들고 세 순회를 출력 |

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인 방법**이 있습니다.

---

### Step 1. 노드 + 트리 골격 만들기 (`MyTree.java`)

**목표**: 값과 좌/우 자식을 가지는 노드, 그리고 루트를 가진 트리 뼈대를 만든다.

**할 일**
1. `Node` 내부 클래스: `value`(int), `left`, `right`.
2. `MyTree`: 루트 `root`.

<details>
<summary>💡 힌트 보기</summary>

```java
public class MyTree {

    static class Node {
        int value;
        Node left;
        Node right;
        Node(int value) {
            this.value = value;
        }
    }

    private Node root;   // 트리의 시작점
}
```

</details>

**확인**: 컴파일 에러가 없으면 OK.

---

### Step 2. 값 넣기 — insert (`MyTree.java`)

**목표**: 값을 넣어 트리를 만든다. 작으면 왼쪽, 크면 오른쪽 (BST 규칙).

**할 일**
1. 루트가 비었으면 새 노드가 루트.
2. 작으면 왼쪽, 크면 오른쪽으로 내려가 빈 자리에 단다.

<details>
<summary>💡 힌트 보기</summary>

재귀로 "이 자리에 넣고 바뀐 서브트리를 돌려준다" 패턴이에요.

```java
public void insert(int value) {
    root = insertNode(root, value);
}

private Node insertNode(Node node, int value) {
    if (node == null) return new Node(value);   // 빈 자리 → 새 노드
    if (value < node.value)      node.left  = insertNode(node.left, value);
    else if (value > node.value) node.right = insertNode(node.right, value);
    // 같은 값이면 무시(중복 안 넣음)
    return node;
}
```

</details>

**확인**: 값을 몇 개 넣은 뒤 Step 3~5의 순회로 확인합니다.

---

### Step 3. 전위 순회 — pre-order (`MyTree.java`)

**목표**: **루트 → 왼쪽 → 오른쪽** 순서로 방문하며 출력한다.

**할 일**: 재귀로, **출력을 맨 먼저** 하고 왼쪽·오른쪽을 방문.

<details>
<summary>💡 힌트 보기</summary>

```java
public void preOrder() {
    System.out.print("전위: ");
    preOrder(root);
    System.out.println();
}

private void preOrder(Node node) {
    if (node == null) return;
    System.out.print(node.value + " ");   // 1) 루트 먼저
    preOrder(node.left);                   // 2) 왼쪽
    preOrder(node.right);                  // 3) 오른쪽
}
```

</details>

**확인**: 예시 트리에서 `50 30 20 40 70 60 80`이 나오면 성공.

---

### Step 4. 중위 순회 — in-order (`MyTree.java`)

**목표**: **왼쪽 → 루트 → 오른쪽** 순서로 방문한다. (BST면 정렬되어 나옴!)

**할 일**: 전위와 똑같되, **출력 줄을 왼쪽 방문과 오른쪽 방문 사이로** 옮긴다.

<details>
<summary>💡 힌트 보기</summary>

```java
public void inOrder() {
    System.out.print("중위: ");
    inOrder(root);
    System.out.println();
}

private void inOrder(Node node) {
    if (node == null) return;
    inOrder(node.left);                   // 1) 왼쪽
    System.out.print(node.value + " ");   // 2) 루트 (가운데!)
    inOrder(node.right);                  // 3) 오른쪽
}
```

전위와 비교해 보세요. **출력 줄의 위치만** 바뀌었죠?

</details>

**확인**: 예시 트리에서 `20 30 40 50 60 70 80`처럼 **정렬되어** 나오면 성공.

---

### Step 5. 후위 순회 — post-order (`MyTree.java`)

**목표**: **왼쪽 → 오른쪽 → 루트** 순서로 방문한다.

**할 일**: 똑같은 구조에서 **출력 줄을 맨 마지막으로** 옮긴다.

<details>
<summary>💡 힌트 보기</summary>

```java
public void postOrder() {
    System.out.print("후위: ");
    postOrder(root);
    System.out.println();
}

private void postOrder(Node node) {
    if (node == null) return;
    postOrder(node.left);                 // 1) 왼쪽
    postOrder(node.right);                // 2) 오른쪽
    System.out.print(node.value + " ");   // 3) 루트 (마지막!)
}
```

세 순회를 나란히 보면, **`System.out.print` 줄이 맨 앞 / 가운데 / 맨 뒤로** 옮겨다닐 뿐이에요.

</details>

**확인**: 예시 트리에서 `20 40 30 60 80 70 50`이 나오면 성공.

---

### Step 6. 마무리 — 세 순회 비교 (`Main.java`)

**목표**: 같은 트리를 세 가지로 순회해 차이를 눈으로 확인한다.

**할 일**: 값들을 넣고 `preOrder()` / `inOrder()` / `postOrder()`를 차례로 호출.

<details>
<summary>💡 힌트 보기</summary>

```java
public class Main {
    public static void main(String[] args) {
        MyTree tree = new MyTree();
        int[] values = {50, 30, 70, 20, 40, 60, 80};
        for (int v : values) tree.insert(v);

        tree.preOrder();    // 전위: 50 30 20 40 70 60 80
        tree.inOrder();     // 중위: 20 30 40 50 60 70 80
        tree.postOrder();   // 후위: 20 40 30 60 80 70 50
    }
}
```

</details>

**점검 항목**
- [ ] 세 순회가 각각 다른 순서로 출력되는가?
- [ ] 중위 순회가 정렬되어 나오는가?
- [ ] 세 메서드의 차이가 "출력 줄의 위치"뿐이라는 걸 이해했는가?

여기까지 통과하면 **트리 + 세 순회 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] 이진 트리의 노드/루트/자식 구조를 만들었다
- [ ] 재귀로 트리를 순회했다
- [ ] 전위·중위·후위의 차이를 "루트 방문 시점"으로 설명할 수 있다
- [ ] BST를 중위 순회하면 정렬된다는 걸 확인했다

---

## 7. 최종 완성 체크리스트

- [ ] `MyTree.java` — Node + insert + preOrder + inOrder + postOrder
- [ ] 세 순회가 모두 정상 동작
- [ ] `Main.java`에서 세 순회 출력 비교

---

## 8. (선택) 도전 과제

1. **레벨 순회(BFS)**: 위에서 아래로, 같은 층을 왼쪽부터 방문 (`Queue` 사용) → `50 30 70 20 40 60 80`
2. **트리 높이**: 가장 깊은 잎까지의 깊이 구하기 (`1 + max(왼쪽 높이, 오른쪽 높이)`)
3. **노드 개수 / 잎 개수**: 전체 노드 수, 자식 없는 잎의 수 세기
4. **값 탐색**: 특정 값이 트리에 있는지 `search(int value)`
5. **트리 그림 출력**: 트리를 옆으로 눕혀 구조를 보여주기
6. **반복문 버전**: 재귀 대신 `Stack`을 써서 전위/중위 순회 구현 (재귀가 내부적으로 어떻게 도는지 이해)