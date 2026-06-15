public class MyHashMap {

    static class Node {
        String key;
        Integer value;
        Node next;
        Node(String key, int value){
            this.key = key;
            this.value = value;
        }
    }

    private Node[] buckets;
    int capacity = 16;
    int size = 0;

    public MyHashMap() {
        Node[] buckets = new Node[capacity];
    }

    public int getIndex(String key){
        return Math.abs(key.hashCode()) % capacity;
    }

    public void put(String key, Integer value){

        int index = getIndex(key);

        Node head = buckets[index];
        while(head != null){
            if(key.equals(head.key)){
                head.value = value;
                return;
            }
            head = head.next;
        }
        Node node = new Node(key, value);
        node.next = head;
        buckets[index] = node;
        size++;
    }

    public Integer get(String key){
        int index = getIndex(key);

        Node head = buckets[index];
        while(head != null){
            if(key.equals(head.key)){
                return head.value;
            }
            head = head.next;
        }
        return null;
    }

    public int size(){
        return size;
    }

    public boolean containsKey(String key){
        int index = getIndex(key);

        Node head = buckets[index];
        while (head != null) {
            if (key.equals(head.key)) {
                return true;
            }
            head = head.next;
        }
        return false;
    }

//    public Integer remove(String key){
//        int index = getIndex(key);
//        Node prev = buckets[index];
//        if (prev != null && key.equals(prev.key)) {
//            buckets[index] = prev.next;
//            size--;
//            return prev.value;
//        }
//        Node node = prev.next;
//        while (node != null) {
//            if (key.equals(node.key)) {
//                prev.next = node.next;
//                size--;
//                return node.value;
//            }
//            prev = prev.next;
//            node = node.next;
//        }
//        return null;
//    }

    public Integer remove(String key){
        int index = getIndex(key);
        Node node = buckets[index];
        Node prev = null;

        while (node != null) {
            if (key.equals(node.key)) {
                if(prev == null){
                    buckets[index] = node.next;
                }
                else {
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

}
