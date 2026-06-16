# BFS 만들기 (너비 우선 탐색 · 그래프 탐색)

> 그래프를 **인접 리스트**로 만들고, **BFS(너비 우선 탐색)** 를 직접 구현합니다.
> 시작 정점에서 **가까운 정점부터** 차례로 방문하는 알고리즘으로, **Queue**와
> **visited 배열**이 핵심이에요.
> **아래 Step을 순서대로 따라가면, 마지막 Step에서 BFS가 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 1. 무엇을 만드나요?

정점 9개짜리 그래프를 만들고, 1번 정점에서 시작해 BFS로 모든 정점을 방문합니다.

```
1 ---- 2 ---- 6
 \    / \    /
  \  /   4 - 5
   \/   / \
   3 - 7 - 8 - 9
```

```java
int startVertex = 1;
// ... BFS 실행 ...
정점 1에서 시작하는 BFS
1 2 3 4 6 7 5 8 9
```

> "가까운 정점부터" 방문하기 때문에, 1과 직접 연결된 2·3을 먼저, 그다음 거리 2인 정점들을 방문합니다.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 그래프 표현 | 인접 리스트(adjacency list) |
| 무방향 그래프 | 간선을 양쪽에 추가 |
| 큐(Queue) | 먼저 들어온 정점을 먼저 꺼내 방문(FIFO) |
| visited 배열 | 같은 정점을 다시 방문하지 않게 막기 |
| BFS 패턴 | 꺼내고 → 이웃을 큐에 넣고 → 반복 |

---

## 3. 핵심 개념

### (1) 그래프와 인접 리스트
**그래프**는 정점(vertex)과 그 사이를 잇는 간선(edge)으로 이뤄져요. 이걸 코드로 담는 방법 중 하나가 **인접 리스트**입니다. 각 정점이 "나와 연결된 정점들의 목록"을 갖는 거예요.

```
adjacencyList[2] = [1, 3, 4, 6]   → 2번은 1, 3, 4, 6과 연결됨
```
`LinkedList<Integer>[]` 배열로, 칸마다 연결 목록(LinkedList)을 둡니다.

### (2) 무방향 그래프 → 간선은 양쪽에
1과 2가 연결되면, 1의 목록에도 2를, 2의 목록에도 1을 넣어야 해요. 그래서 `addEdge`는 **양쪽 모두**에 추가합니다.

### (3) BFS = 가까운 곳부터 (Queue 사용)
BFS는 시작점에서 **거리가 가까운 정점부터** 차례로 방문해요. 이 "먼저 본 것부터 처리"를 **큐(FIFO)** 로 자연스럽게 구현합니다.

### (4) visited — 중복 방문 막기
그래프는 사이클(빙 도는 길)이 있어서, 표시를 안 하면 같은 정점을 **무한히** 다시 큐에 넣게 돼요. 그래서 `visited[]`로 한 번 넣은 정점은 다시 안 넣습니다.

> **BFS 4단계 패턴**
> 1. 시작 정점을 `visited` 표시하고 큐에 넣는다
> 2. 큐에서 정점을 하나 꺼내 방문(출력)한다
> 3. 그 정점의 이웃 중 **아직 안 간 것**을 `visited` 표시하고 큐에 넣는다
> 4. 큐가 빌 때까지 2~3 반복

---

## 4. 파일 구조

| 구성 | 역할 |
|------|------|
| `Graph` 클래스 | 인접 리스트 + 간선 추가(addEdge) + 출력(printGraph) |
| `main` (실행부) | 그래프를 만들고 BFS를 실행 |

> 둘을 한 파일에 둬도 되고, `Graph.java` / `Main.java`로 나눠도 됩니다.
> 맨 위에 `import java.util.*;` 가 필요해요. (`LinkedList`, `Queue` 사용)

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인 방법**이 있습니다.

---

### Step 1. 그래프 골격 — 인접 리스트 (`Graph`)

**목표**: 정점 개수만큼 인접 리스트 배열을 만들고, 각 칸을 빈 리스트로 초기화한다.

**할 일**
1. `LinkedList<Integer>[] adjacencyList` 필드.
2. 생성자에서 `vertex + 1` 크기로 배열 생성 (1번 정점부터 쓰려고).
3. 각 칸을 `new LinkedList<>()`로 초기화.
4. 인접 리스트를 꺼내는 getter.

<details>
<summary>💡 힌트 보기</summary>

```java
class Graph {
    private LinkedList<Integer>[] adjacencyList;

    public Graph(int vertex) {
        adjacencyList = new LinkedList[vertex + 1];   // 0번은 안 쓰고 1~vertex 사용
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new LinkedList<>();      // 각 칸을 빈 리스트로
        }
    }

    public LinkedList<Integer>[] getAdjacencyList() {
        return adjacencyList;
    }
}
```

각 칸을 `new LinkedList<>()`로 채우지 않으면 `null`이라 나중에 `add` 할 때 에러가 나요.

</details>

**확인**: 컴파일 에러 없이 그래프 객체가 만들어지면 OK.

---

### Step 2. 간선 추가 — addEdge (`Graph`)

**목표**: 두 정점을 연결한다. 무방향이므로 **양쪽 목록 모두**에 추가.

<details>
<summary>💡 힌트 보기</summary>

```java
public void addEdge(int v, int w) {
    adjacencyList[v].add(w);
    adjacencyList[w].add(v);   // 무방향 → 반대쪽도 추가
}
```

만약 `adjacencyList[w].add(v)`를 빠뜨리면, w에서 v로는 못 가는 "한 방향" 그래프가 돼요.

</details>

**확인**: 다음 Step의 `printGraph`로 연결이 맞는지 확인합니다.

---

### Step 3. 그래프 출력 — printGraph (`Graph`)

**목표**: 각 정점의 인접 정점 목록을 출력해 그래프가 잘 만들어졌는지 확인한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public void printGraph() {
    for (int i = 1; i < adjacencyList.length; i++) {   // 1번부터
        System.out.print("Vertex " + i + " : ");
        for (Integer v : adjacencyList[i]) {
            System.out.print(v + " ");
        }
        System.out.println();
    }
}
```

</details>

**확인**: 예를 들어 `Vertex 2 : 1 3 4 6` 처럼, 2번이 1·3·4·6과 연결돼 나오면 성공.

---

### Step 4. BFS 준비 — 큐와 visited (`main`)

**목표**: 그래프를 만들고 간선을 추가한 뒤, BFS에 필요한 `visited`와 `Queue`를 준비한다.

**할 일**
1. `Graph` 생성 + `addEdge`로 간선들 추가.
2. `boolean[] visited`, `Queue<Integer> queue` 준비.
3. 시작 정점을 **visited 표시 + 큐에 넣기**.

<details>
<summary>💡 힌트 보기</summary>

```java
boolean[] visited = new boolean[9 + 1];
Graph graph = new Graph(9);
graph.addEdge(1, 2);
graph.addEdge(1, 3);
// ... (나머지 간선들도 추가) ...

int startVertex = 1;
Queue<Integer> queue = new LinkedList<>();   // 탐색할 정점을 담는 큐

visited[startVertex] = true;   // 시작점 방문 표시
queue.add(startVertex);        // 큐에 넣기
System.out.println("정점 " + startVertex + "에서 시작하는 BFS");
```

</details>

**확인**: 시작점이 큐에 들어가고 visited로 표시되면 준비 끝.

---

### Step 5. BFS 핵심 루프 (`main`)

**목표**: 큐가 빌 때까지, 정점을 꺼내 방문하고 이웃을 큐에 넣는다.

**할 일**
1. 큐에서 정점을 하나 꺼내(`poll`) 출력.
2. 그 정점의 인접 정점들 중 **방문 안 한 것**을 visited 표시하고 큐에 추가.
3. 큐가 빌 때까지 반복.

<details>
<summary>💡 힌트 보기</summary>

```java
while (!queue.isEmpty()) {
    int vertex = queue.poll();        // 큐에서 꺼내서
    System.out.print(vertex + " ");   // 방문(출력)

    // 인접 정점 중 아직 안 간 곳을 큐에 추가
    for (int adj : graph.getAdjacencyList()[vertex]) {
        if (!visited[adj]) {
            visited[adj] = true;      // ★ 큐에 "넣을 때" 표시!
            queue.add(adj);
        }
    }
}
```

**중요**: `visited` 표시를 **큐에 넣는 순간**에 해야 해요. 만약 꺼낼 때 표시하면, 같은 정점이 큐에 여러 번 들어가 중복 방문/비효율이 생깁니다.

</details>

**확인**: 1에서 시작하면 `1 2 3 4 6 7 5 8 9` 순서로 출력되면 성공!

---

### Step 6. 마무리 점검 (완성!)

**점검 항목**
- [ ] `printGraph`로 본 인접 리스트가 그림과 맞는가?
- [ ] BFS가 시작점에서 가까운 정점부터 방문하는가?
- [ ] 사이클이 있어도 무한 반복 없이 끝나는가? (`visited` 덕분)
- [ ] 모든 정점을 빠짐없이 한 번씩 방문하는가?

여기까지 통과하면 **BFS 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] 그래프를 인접 리스트로 표현했다
- [ ] 무방향 간선을 양쪽에 추가했다
- [ ] 큐(FIFO)로 가까운 정점부터 방문했다
- [ ] visited로 중복 방문을 막았다
- [ ] "큐에 넣을 때 visited 표시"가 왜 중요한지 설명할 수 있다

---

## 7. 최종 완성 체크리스트

- [ ] `Graph` — 인접 리스트 + addEdge + printGraph
- [ ] BFS — visited + Queue로 너비 우선 탐색
- [ ] 시작 정점에서 모든 정점을 방문
- [ ] 출력 순서가 `1 2 3 4 6 7 5 8 9`

---

## 8. (선택) 도전 과제

1. **시작점 바꾸기**: `startVertex`를 3이나 5로 바꿔 방문 순서가 어떻게 달라지는지 보기
2. **DFS 만들어 비교**: 같은 그래프를 깊이 우선(스택 또는 재귀)으로 탐색해 BFS와 순서 비교
3. **최단 거리**: BFS는 가까운 곳부터 가니, 시작점에서 각 정점까지의 **간선 개수(최단 거리)** 를 함께 기록하기 (`dist[]` 배열)
4. **경로 복원**: 각 정점을 누가 큐에 넣었는지(`parent[]`) 기록해, 시작점에서 특정 정점까지의 경로 출력
5. **연결되지 않은 그래프**: 모든 정점을 도는 바깥 반복문을 둬서, 끊긴 부분(연결 요소)도 빠짐없이 탐색
6. **인접 행렬 버전**: 인접 리스트 대신 2차원 배열(`boolean[][]`)로 그래프를 표현해 보기