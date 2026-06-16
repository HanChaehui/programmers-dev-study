public class Main {
    public static void main(String[] args) {
        MyTree tree = new MyTree();
        int[] arr = {50, 30, 70, 20, 40, 60, 80};
        for(int i : arr) tree.insert(i);

        tree.preOrder();
        tree.inOrder();
        tree.postOrder();
    }
}
