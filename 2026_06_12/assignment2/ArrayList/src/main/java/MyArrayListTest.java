import java.util.Arrays;

public class MyArrayListTest {
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

        // --- Step 6 확인 ---
        list.addFirst("앞");
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
        ensureCapacity();
        arr[size] = data;
        size++;
    }

    // [Step 3] 인덱스로 꺼내기
    String get(int index) {
        // TODO: index 위치의 값을 반환하세요.
        return arr[index];
    }

    // [Step 4] 현재 개수
    int size() {
        // TODO: size를 반환하세요.
        return size;
    }

    // [Step 5] 공간이 꽉 찼으면 배열을 2배로 늘리기
    private void ensureCapacity() {
        // TODO: size가 arr.length와 같으면
        //       arr = Arrays.copyOf(arr, arr.length * 2); 로 교체하세요.
        if(size == arr.length){
            arr = Arrays.copyOf(arr, arr.length * 2);
        }
    }

    // [Step 6] 맨 앞에 추가  ★핵심★
    void addFirst(String data) {
        // TODO: 1) ensureCapacity() 호출
        //       2) for 문으로 맨 뒤에서부터 arr[i] = arr[i - 1] 한 칸씩 밀기
        //       3) arr[0] = data;  그리고 size++
        ensureCapacity();
        for(int i = 0; i < size; i++){
            arr[i + 1] = arr[i];
        }
        arr[0] = data;
        size++;
    }

    // [도전] index 위치에 삽입
    void insert(int index, String data) {
        // TODO (도전): index 뒤의 요소들을 한 칸씩 밀고, 그 자리에 data를 넣으세요.
        ensureCapacity();
        for(int i = index; i < size; i++){
            arr[i + 1] = arr[i];
        }
        arr[index] = data;
        size++;
    }

    // [도전] index 위치 삭제
    void remove(int index) {
        // TODO (도전): index 뒤의 요소들을 한 칸씩 앞으로 당기고 size--
        for(int i = index + 1; i < size; i++){
            arr[i - 1] = arr[i];
        }
        arr[size - 1] = null;
        size--;
    }
}