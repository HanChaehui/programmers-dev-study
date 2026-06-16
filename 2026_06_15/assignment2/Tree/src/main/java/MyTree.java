public class MyTree {

    static class Node{
        int value;
        Node left;
        Node right;
        public Node(int value){
            this.value = value;
        }
    }
    private Node root;

//    public void insert(int value){
//        Node node = root;
//        if (root == null) {
//            root = new Node(value);
//            return;
//        }
//        while(true) {
//            if (value < node.value) {
//                if (node.left == null) {
//                    node.left = new Node(value);
//                    return;
//                }
//                node = node.left;
//            } else if (value > node.value) {
//                if (node.right == null) {
//                    node.right = new Node(value);
//                    return;
//                }
//                node = node.right;
//            }
//        }
//    }

    public void insert(int value){
        root = insertNode(root, value);
    }

    private Node insertNode(Node node, int value){
        if(node == null) return new Node(value);
        if(value < node.value) node.left = insertNode(node.left, value);
        else if(value > node.value) node.right = insertNode(node.right, value);
        return node;
    }

    public void preOrder(){
        preOrder(root);
        System.out.println();
    }

    private void preOrder(Node node){
        System.out.print(node.value+" ");
        if(node == null) return;
        preOrder(node.left);
    }
}
