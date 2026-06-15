public class Main {
    public static void main(String[] args) {
        MyHashMap map = new MyHashMap();
        System.out.println(map.size()); // 0
        map.put("apple", 1);
        System.out.println(map.get("apple")); // 1
        map.put("banana", 2);
        map.put("apple", 3);
        System.out.println(map.size()); // 2
        System.out.println(map.get("apple"));    // 3
        System.out.println(map.get("cherry"));   // null
        System.out.println(map.remove("apple")); // 3
        System.out.println(map.remove("apple")); // null
        System.out.println(map.size()); // 1
        System.out.println(map.get("apple"));    // null
        System.out.println(map.containsKey("banana")); // true
        System.out.println(map.containsKey("b")); // false
    }
}
