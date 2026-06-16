public class Main {
    public static void main(String[] args) {
        MyTreeMap treemap = new MyTreeMap();
        treemap.put("a", 1);
        treemap.put("b", 2);
        treemap.put("c", 3);
        treemap.put("d", 4);
        treemap.put("e", 5);
        treemap.put("f", 6);
        treemap.put("g", 7);
        System.out.println(treemap.size()); // 7
        treemap.printSorted();
        System.out.println(treemap.get("c")); // 3
        System.out.println(treemap.containsKey("h")); // false
        System.out.println(treemap.firstKey()); // a
        System.out.println(treemap.lastKey()); // g
        System.out.println(treemap.remove("a")); // 1
        System.out.println(treemap.get("a")); // null
        System.out.println(treemap.size()); // 6
        treemap.printSorted();
        System.out.println(treemap.remove("a")); // null
        treemap.put("a", 100);
        System.out.println(treemap.size()); // 7
        treemap.printSorted();
        treemap.put("a", 200);
        System.out.println(treemap.size()); // 7
        treemap.printSorted();

    }
}
