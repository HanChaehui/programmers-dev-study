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
        System.out.print("전위: ");
        preOrder(root);
        System.out.println();
    }

    private void preOrder(Node node){
        if(node == null) return;
        System.out.print(node.value + " ");
        preOrder(node.left);
        preOrder(node.right);
    }

    public void inOrder(){
        System.out.print("중위: ");
        inOrder(root);
        System.out.println();
    }

    private void inOrder(Node node){
        if(node == null) return;
        inOrder(node.left);
        System.out.print(node.value + " ");
        inOrder(node.right);
    }

    public void postOrder(){
        System.out.print("후위: ");
        postOrder(root);
        System.out.println();
    }

    private void postOrder(Node node){
        if(node == null) return;
        postOrder(node.left);
        postOrder(node.right);
        System.out.print(node.value + " ");
    }
}
