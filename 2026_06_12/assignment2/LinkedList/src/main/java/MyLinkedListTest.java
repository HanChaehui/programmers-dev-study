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

        // --- Step 5 확인 ---
        list.addFirst("앞");
        System.out.print("addFirst 후: ");
        list.printLinks();
        // 기대: [null <- 앞 -> 가] [앞 <- 가 -> 나] [가 <- 나 -> 다] [나 <- 다 -> null]

        // --- Step 6 확인 ---
        System.out.println("get(2) = " + list.get(2));   // 기대: 나

        // --- Step 7 확인 ---
        list.insert(2, "끼움");
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
            this.data = data;
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
        if(head == null){
            head = node;
            tail = node;
            size++;
        }
        else{
            node.prev = tail;
            tail.next = node;
            tail = node;
            size++;
        }
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
        if(head == null){
            head = node;
            tail = node;
            size++;
        }
        else{
            node.next = head;
            head.prev = node;
            head = node;
            size++;
        }
    }

    // [Step 6] index번째 노드 찾기
    private Node nodeAt(int index) {
        // TODO: head 부터 시작해서 next 로 index 번 이동한 노드를 반환하세요.
        if (index == 0) return head;
        if (index == size - 1) return tail;

        Node cur = head;
        int cnt = 0;
        while(cnt != index && cur != null){
            cur = cur.next;
            cnt++;
        }
        return cur;
    }

    String get(int index) {
        // TODO: nodeAt(index).data 를 반환하세요.
        if (nodeAt(index)!= null) return nodeAt(index).data;
        else return null;
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
        if (index == 0) addFirst(data);
        if (index == size) addLast(data);

        Node next = nodeAt(index);
        Node prev = next.prev;
        Node node = new Node(data);
        node.prev = prev;
        node.next = next;
        prev.next = node;
        next.prev = node;
        size++;
    }

    // [도전] index 위치 노드 삭제
    void remove(int index) {
        // TODO (도전): 삭제할 노드의 prev 와 next 를 서로 연결하고 size--
        //              (맨 앞/맨 뒤 삭제 시 head/tail 갱신 주의)
        if(head == null) return;
        if(index == 0) {
            (head.next).prev = null;
            head = head.next;
            size--;
            return;
        }
        if(index == size - 1){
            (tail.prev).next = null;
            tail = tail.prev;
            size--;
            return;
        }
        Node target = nodeAt(index);
        Node prev = target.prev;
        Node next = target.next;
        prev.next = next;
        next.prev = prev;
        size--;
    }

    int size() { return size; }
}