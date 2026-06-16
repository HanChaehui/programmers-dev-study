public class MyTreeMap {
    static class Node{
        String key;
        Integer value;
        Node left;
        Node right;
        public Node(String key, Integer value){
            this.key = key;
            this.value = value;
        }
    }
    private Node root;
    private int size = 0;

    public void put(String key, Integer value){
        root = put(root, key, value);
    }
    private Node put(Node node, String key, Integer value){
        if(node == null) {
            size++;
            return new Node(key, value);
        }
        if(key.compareTo(node.key) < 0) put(node.left, key, value);
        else if(key.compareTo(node.key) > 0) put(node.right, key, value);
        else node.value = value;
        return node;
    }

    public Integer get(String key){
        Node node = get(root, key);
        if (node == null) return null;
        return node.value;
    }
    private Node get(Node node, String key){
        if(node == null) return null;
        if(key.compareTo(node.key) < 0) return get(node.left, key);
        else if(key.compareTo(node.key) > 0) return get(node.right, key);
        else return node;
    }

    public void printSorted(){
        printSorted(root);
        System.out.println();
    }
    private void printSorted(Node node){
        if(node == null) return;
        printSorted(node.left);
        System.out.print(node.key + " ");
        printSorted(node.right);
    }

    public int size(){
        return size;
    }

    public boolean containsKey(String key){
        return get(root, key)!= null;
    }

    public String firstKey(){
        if(root == null) return null;
        Node node = root;
        while(node.left != null){
            node = node.left;
        }
        return node.key;
    }

    public String lastKey(){
        if(root == null) return null;
        Node node = root;
        while(node.right != null){
            node = node.right;
        }
        return node.key;
    }

    public Integer remove(String key) {
        Integer find = get(key);
        if (find == null) return null;
        root = remove(root, key);
        size--;
        return find;
    }
    // 타켓 노드가 제거 완료 된 루트 노드 찾기 - 서브트리로 재귀
    private Node remove(Node node, String key){
        if(node == null) return null;
        if(key.compareTo(node.key) < 0) node.left = remove(node.left, key);
        else if(key.compareTo(node.key) > 0) node.right = remove(node.right, key);
        // 삭제할 노드 발견
        else{
            // 왼 or 오 자식이 하나만 있거나
            // 자식이 아에 없는 경우
            if(node.left == null) return node.right;
            if(node.right == null) return node.left;

            // 왼,오 자식이 모두 있는 경우
            Node succ = node;
            // 오른쪽 서브 트리에서 가장 작은 노드 찾기 (후계자)
            while(succ.left != null) succ = succ.left;
            // 삭제할 노드에 후계자 노드 위임
            node.key = succ.key;
            node.value = succ.value;
            // 삭제할 노드의 오른쪽 서브 트리 남아있는 (구)후계자 삭제
            node.right = remove(node.right, succ.key);
        }
        return node;
    }
}
