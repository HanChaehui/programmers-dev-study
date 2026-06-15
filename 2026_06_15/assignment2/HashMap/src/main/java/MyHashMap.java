public class MyHashMap {

    static class Node {
        String key;
        int value;
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

}
