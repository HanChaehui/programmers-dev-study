# DFS 만들기 (깊이 우선 탐색 · 그래프 탐색)

> BFS와 **같은 그래프**를 이번엔 **DFS(깊이 우선 탐색)** 로 탐색합니다.
> 한 방향으로 **갈 수 있는 데까지 깊이** 들어갔다가, 막히면 되돌아오는(backtrack) 방식이에요.
> **재귀**로 구현하면 가장 자연스럽고, `visited` 배열로 중복 방문을 막습니다.
> **아래 Step을 순서대로 따라가면, 마지막 Step에서 DFS가 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 1. 무엇을 만드나요?

BFS 때와 똑같은 정점 9개짜리 그래프를, 이번엔 DFS로 1번부터 탐색합니다.

```
1 ---- 2 ---- 6
 \    / \    /
  \  /   4 - 5
   \/   / \
   3 - 7 - 8 - 9
```

```java
int startVertex = 1;
// ... DFS 실행 ...
정점 1에서 시작하는 DFS
1 2 3 7 4 5 6 8 9
```

> BFS는 `1 2 3 4 6 7 5 8 9`(가까운 곳부터)였죠. DFS는 `1 2 3 7 ...`처럼 **한 길을 끝까지** 파고듭니다.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 깊이 우선 탐색 | 한 길로 끝까지 → 막히면 되돌아오기(backtrack) |
| 재귀로 그래프 탐색 | 이웃으로 `dfs` 재귀 호출 |
| visited 배열 | 같은 정점을 다시 방문하지 않게 |
| BFS vs DFS | 큐(넓게) vs 재귀·스택(깊게)의 차이 |

---

## 3. 핵심 개념

### (1) DFS = 한 길을 끝까지 (backtrack)
DFS는 한 정점에서 갈 수 있는 길로 **계속 깊이** 들어갑니다. 더 갈 곳이 없으면(막히면) 한 칸 **되돌아와** 다른 길을 시도해요. 이 "되돌아오기"를 **재귀 함수의 호출 스택이 자동으로** 처리해 줍니다.

### (2) 재귀로 구현
"이 정점을 방문 표시하고 출력한 뒤, 안 간 이웃마다 다시 dfs를 부른다." 딱 이게 전부예요.

```
dfs(정점):
    방문 표시 + 출력
    각 이웃 중 안 간 곳 → dfs(이웃)
```

### (3) visited — 중복/무한 방지 (BFS와 동일)
그래프엔 사이클이 있어서, 표시를 안 하면 같은 정점을 무한히 다시 방문해요. `visited[]`로 막습니다.

### (4) ⭐ BFS vs DFS
| | BFS | DFS |
|---|---|---|
| 방문 순서 | 가까운 정점부터 (넓게) | 한 길을 끝까지 (깊게) |
| 자료구조 | **큐(Queue)** | **재귀** 또는 **스택(Stack)** |
| 직관 | 물결이 퍼지듯 | 미로를 한 길로 파고들 듯 |

> 외우는 법: **BFS = 큐, DFS = 스택(또는 재귀).** 사실 BFS 코드에서 큐를 스택으로만 바꿔도 DFS가 됩니다(도전 과제).

---

## 4. 파일 구조

| 구성 | 역할 |
|------|------|
| `Graph` 클래스 | 인접 리스트 + addEdge + **dfs(재귀)** |
| `main` (실행부) | 그래프를 만들고 DFS 실행 |

> `Graph`의 인접 리스트·addEdge 부분은 **BFS 가이드와 동일**해요. BFS를 먼저 했다면 그대로 재사용하고, DFS 메서드만 추가하면 됩니다.
> 맨 위에 `import java.util.*;` 필요(`LinkedList` 사용).

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인 방법**이 있습니다.

---

### Step 1. 그래프 준비 (`Graph`) — BFS와 동일

**목표**: 인접 리스트 그래프와 간선 추가(addEdge)를 만든다. (이미 BFS에서 했다면 재사용)

**할 일**
1. `LinkedList<Integer>[] adjacencyList` + 생성자에서 `vertex + 1` 크기로 초기화.
2. `addEdge(v, w)` — 무방향이므로 양쪽에 추가.

<details>
<summary>💡 힌트 보기</summary>

```java
class Graph {
    private LinkedList<Integer>[] adjacencyList;

    public Graph(int vertex) {
        adjacencyList = new LinkedList[vertex + 1];
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new LinkedList<>();
        }
    }

    public void addEdge(int v, int w) {
        adjacencyList[v].add(w);
        adjacencyList[w].add(v);   // 무방향
    }
}
```

</details>

**확인**: 그래프가 만들어지고 간선이 추가되면 OK.

---

### Step 2. DFS 재귀 메서드 (`Graph`) · 핵심

**목표**: 한 정점에서 시작해, 안 간 이웃으로 계속 깊이 들어가는 재귀 메서드를 만든다.

**할 일**
1. 현재 정점을 **visited 표시 + 출력**.
2. 인접 정점 중 **아직 안 간 곳마다 `dfs`를 재귀 호출**.

<details>
<summary>💡 힌트 보기</summary>

호출부를 깔끔히 하려고, visited를 만들어주는 public 메서드 + 실제 재귀 메서드로 나눴어요.

```java
// 외부에서 부르는 시작점
public void dfs(int start) {
    boolean[] visited = new boolean[adjacencyList.length];
    System.out.println("정점 " + start + "에서 시작하는 DFS");
    dfsRecursive(start, visited);
    System.out.println();
}

// 실제 재귀
private void dfsRecursive(int vertex, boolean[] visited) {
    visited[vertex] = true;            // 방문 표시
    System.out.print(vertex + " ");    // 방문(출력)

    for (int adj : adjacencyList[vertex]) {
        if (!visited[adj]) {
            dfsRecursive(adj, visited); // 안 간 이웃으로 깊이 들어감
        }
    }
}
```

BFS와 비교해 보세요. **큐가 없어졌고**, 대신 자기 자신을 다시 부르는 **재귀**가 그 역할(되돌아오기)을 합니다.

</details>

**확인**: 다음 Step에서 실행해 확인합니다.

---

### Step 3. 실행 (`main`)

**목표**: 그래프를 만들고 간선을 추가한 뒤, 1번에서 DFS를 실행한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public static void main(String[] args) {
    Graph graph = new Graph(9);
    graph.addEdge(1, 2);
    graph.addEdge(1, 3);
    graph.addEdge(2, 3);
    graph.addEdge(2, 4);
    graph.addEdge(2, 6);
    graph.addEdge(3, 7);
    graph.addEdge(4, 5);
    graph.addEdge(4, 7);
    graph.addEdge(4, 8);
    graph.addEdge(5, 6);
    graph.addEdge(7, 8);
    graph.addEdge(8, 9);

    graph.dfs(1);   // 1번에서 깊이 우선 탐색
}
```

</details>

**확인**: `1 2 3 7 4 5 6 8 9` 가 출력되면 성공!

---

### Step 4. 마무리 점검 (완성!)

**점검 항목**
- [ ] 한 길을 끝까지 파고든 뒤 되돌아오는가? (예: 1→2→3→7→4→5→6 ...)
- [ ] 사이클이 있어도 무한 반복 없이 끝나는가? (`visited`)
- [ ] 모든 정점을 빠짐없이 한 번씩 방문하는가?
- [ ] BFS 결과(`1 2 3 4 6 7 5 8 9`)와 순서가 어떻게 다른지 비교해봤는가?

여기까지 통과하면 **DFS 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] DFS가 "깊이 먼저, 막히면 되돌아오기"임을 이해했다
- [ ] 재귀로 그래프를 탐색했다
- [ ] visited로 중복 방문을 막았다
- [ ] BFS(큐)와 DFS(재귀/스택)의 차이를 설명할 수 있다

---

## 7. 최종 완성 체크리스트

- [ ] `Graph` — 인접 리스트 + addEdge + dfs(재귀)
- [ ] DFS가 시작 정점에서 모든 정점을 방문
- [ ] 출력 순서가 `1 2 3 7 4 5 6 8 9`
- [ ] BFS 결과와 비교 완료

---

## 8. (선택) 도전 과제

1. **스택으로 DFS (BFS와 대칭!)**: BFS 코드에서 **`Queue`를 `Stack`으로** 바꿔 반복문 DFS를 만들어 보기.
   재귀 없이도 DFS가 됩니다. (단, 이웃을 넣는 순서에 따라 방문 순서는 재귀 버전과 달라질 수 있어요 — 둘 다 올바른 DFS입니다.)
   <details>
   <summary>💡 스택 버전 힌트</summary>

   ```java
   Stack<Integer> stack = new Stack<>();
   boolean[] visited = new boolean[10];
   stack.push(1);
   while (!stack.isEmpty()) {
       int v = stack.pop();
       if (visited[v]) continue;   // 이미 방문했으면 건너뜀
       visited[v] = true;
       System.out.print(v + " ");
       for (int adj : graph.getAdjacencyList()[v]) {
           if (!visited[adj]) stack.push(adj);
       }
   }
   ```
   </details>
2. **시작점 바꾸기**: `dfs(3)`, `dfs(5)`로 바꿔 방문 순서 변화 관찰
3. **경로/도달 확인**: 특정 정점에 도달 가능한지, 시작점에서 그 정점까지의 경로 출력
4. **사이클 탐지**: DFS 도중 "이미 방문한, 부모가 아닌 정점"을 다시 만나면 사이클이 있다고 판단
5. **연결 요소 세기**: 모든 정점을 도는 바깥 반복문으로, 끊긴 그래프의 덩어리(연결 요소) 개수 세기
6. **BFS와 한 파일에서 비교**: 같은 그래프에 BFS와 DFS를 둘 다 실행해 출력 순서를 나란히 비교