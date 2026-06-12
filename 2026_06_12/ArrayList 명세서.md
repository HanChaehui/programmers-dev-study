# 과제 1 — 배열로 `MyArrayList` 직접 만들기

## 목표

배열 기반 리스트를 직접 구현하면서, **왜 인덱스 접근(`get`)은 빠르고 맨 앞 삽입(`addFirst`)은 느린지**를 코드로 체득한다.

## 준비물

`MyArrayListTest.java` 스켈레톤을 IDE에 넣고, `// TODO` 부분만 단계별로 채운다. `main`은 이미 들어 있으니 단계마다 실행해서 결과를 확인하면 된다.

## 핵심 아이디어

- 데이터를 `String[] arr`에 **물리적으로 나란히** 담는다.
- `size`는 현재 들어있는 개수.
- 인덱스 접근은 `arr[index]` 한 줄이라 빠르다.
- 맨 앞 삽입은 뒤 요소를 **전부 한 칸씩 밀어야** 해서 느리다. ← 이걸 직접 겪어보는 게 이 과제의 포인트.

---

## 단계별 진행

### Step 1. 필드 이해 (작성돼 있음)

`String[] arr = new String[10];` 와 `int size = 0;` 가 각각 무슨 역할인지 생각해본다.

### Step 2. `addLast(String data)`

빈 끝자리(`size` 위치)에 `data`를 넣고 `size`를 1 늘린다.

- ✅ 확인: "가","나","다"를 addLast 하고 `size()`가 3이면 성공.

### Step 3. `get(int index)`

`index` 위치의 값을 그대로 반환한다. (한 줄이면 끝)

### Step 4. `size()`

`size`를 반환한다.

- ✅ 확인: 여기까지 하면 `main`의 첫 출력이 `size = 3`, `가, 나, 다` 로 나온다.

### Step 5. `ensureCapacity()`

`size`가 `arr.length`와 같으면(=꽉 찼으면) `Arrays.copyOf(arr, arr.length * 2)`로 2배 배열로 교체한다.

- 💡 배열은 크기가 고정이라, 꽉 차면 더 큰 배열로 옮겨 담아야 한다.

### Step 6. `addFirst(String data)` — 이 과제의 핵심

1. `ensureCapacity()` 호출
2. 맨 뒤에서부터 앞으로 가며 `arr[i] = arr[i - 1]` 로 한 칸씩 민다
3. `arr[0] = data;` 하고 `size++`

- ✅ 확인: "가","나","다" 뒤에 `addFirst("앞")` 하면 0번이 "앞", 1번이 "가".
- 🤔 생각: 요소가 100만 개일 때 이 "밀기"는 몇 번 일어날까?

---

## 도전 과제 (선택)

- `insert(int index, String data)` : index 뒤 요소들을 밀고 그 자리에 삽입
- `remove(int index)` : index 뒤 요소들을 앞으로 당기고 `size--`

## 제출 / 확인

완성한 `MyArrayListTest.java`를 실행했을 때 아래와 같이 나오면 통과.

```
size = 3
0,1,2 = 가, 나, 다
addFirst 후 0,1 = 앞, 가
size = 4
```

```java
public class A_collections_list_ex_array {  
    public static void main(String[] args) {  
        MyArrayList list = new MyArrayList();  
  
        // --- Step 2~4 확인 ---  
        list.addLast("가");  
        list.addLast("나");  
        list.addLast("다");  
        System.out.println("size = " + list.size());                 // 기대: 3  
        System.out.println("0,1,2 = " + list.get(0) + ", "  
                + list.get(1) + ", "  
                + list.get(2));                // 기대: 가, 나, 다  
  
        // --- Step 6 확인 ---        list.addFirst("앞");  
        System.out.println("addFirst 후 0,1 = " + list.get(0) + ", " + list.get(1)); // 기대: 앞, 가  
        System.out.println("size = " + list.size());                 // 기대: 4  
    }  
}  
  
  
class MyArrayList {  
  
    // [Step 1] 필드 (작성돼 있음): 데이터를 담을 배열과 현재 개수  
    private String[] arr = new String[10];  
    private int size = 0;  
  
    // [Step 2] 맨 뒤에 추가  
    void addLast(String data) {  
        // TODO: 빈 끝자리(size 위치)에 data를 넣고, size를 1 늘리세요.  
    }  
  
    // [Step 3] 인덱스로 꺼내기  
    String get(int index) {  
        // TODO: index 위치의 값을 반환하세요.  
        return null;  
    }  
  
    // [Step 4] 현재 개수  
    int size() {  
        // TODO: size를 반환하세요.  
        return 0;  
    }  
  
    // [Step 5] 공간이 꽉 찼으면 배열을 2배로 늘리기  
    private void ensureCapacity() {  
        // TODO: size가 arr.length와 같으면  
        //       arr = Arrays.copyOf(arr, arr.length * 2); 로 교체하세요.  
    }  
  
    // [Step 6] 맨 앞에 추가  ★핵심★  
    void addFirst(String data) {  
        // TODO: 1) ensureCapacity() 호출  
        //       2) for 문으로 맨 뒤에서부터 arr[i] = arr[i - 1] 한 칸씩 밀기  
        //       3) arr[0] = data;  그리고 size++  
    }  
  
    // [도전] index 위치에 삽입  
    void insert(int index, String data) {  
        // TODO (도전): index 뒤의 요소들을 한 칸씩 밀고, 그 자리에 data를 넣으세요.  
    }  
  
    // [도전] index 위치 삭제  
    void remove(int index) {  
        // TODO (도전): index 뒤의 요소들을 한 칸씩 앞으로 당기고 size--  
    }  
}
```