package Stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BalancedBinarySearchTree<T> {

    class TreeNode {
        T data;
        TreeNode left, right;
        int height;
        int size;

        public TreeNode(T data) {
            this.data = data;
            this.left = null;
            this.right = null;
            this.height = 1;
            this.size = 1;
        }
    }
    private TreeNode root = null;
    transient private Comparator<T> comparator;
    private static final int max_size = 131072;
    public BalancedBinarySearchTree(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void addNode(T data) {
        this.root = addNodeRecursive(root, data);
        if(size() > max_size) deleteNodeAtIndex(0);
    }
    public void addAll(List<T> data) {
        for(T datum: data) {
            addNode(datum);
        }
    }
    private TreeNode addNodeRecursive(TreeNode base, T data) {
        if(base == null) return new TreeNode(data);
        if(comparator.compare(data, base.data) < 0) base.left = addNodeRecursive(base.left, data);
        else base.right = addNodeRecursive(base.right, data);
        return rebalance(base);
    }
    public void deleteNodeAtIndex(int index) {
        if(index<0 || index >= size(root)) throw new IndexOutOfBoundsException("Out of Range");
        root = deleteNodeAtIndexRecursive(root, index);
    }
    private TreeNode deleteNodeAtIndexRecursive(TreeNode node, int index) {
        int right_size = size(node.right);
        if(index == right_size) {
            return deleteNode(node);
        } else if(index < right_size) {
            node.right = deleteNodeAtIndexRecursive(node.right, index);
        } else {
            node.left = deleteNodeAtIndexRecursive(node.left, index-right_size-1);
        }
        return rebalance(node);
    }

    /**
     * @param data Must be the exact same object inserted into the Binary Tree.
     */
    public void deleteNode(T data) {
        root = deleteNodeFromDataRecursive(root, data);
    }
    private TreeNode deleteNodeFromDataRecursive(TreeNode node, T data) {
        if(node == null) {
            return null;
        }
        else if(data == node.data) { // Intentional memory address equality check
            return deleteNode(node);
        } else if(comparator.compare(data, node.data) == 0) {
            node.left = deleteNodeFromDataRecursive(node.left, data);
            node.right = deleteNodeFromDataRecursive(node.right, data);
        } else if(comparator.compare(data, node.data) < 0) {
            node.left = deleteNodeFromDataRecursive(node.left, data);
        } else {
            node.right = deleteNodeFromDataRecursive(node.right, data);
        }
        return rebalance(node);
    }

    public T getNodeAtIndex(int index) {
        if(index<0 || index>=size()) throw new IndexOutOfBoundsException();
        return getNodeAtIndexRecursive(root, index).data;
    }
    private TreeNode getNodeAtIndexRecursive(TreeNode node, int index) {
        int right_size = size(node.right);
        if(index == right_size) {
            return node;
        } else if(index < right_size) {
            return getNodeAtIndexRecursive(node.right, index);
        } else {
            return getNodeAtIndexRecursive(node.left, index-right_size-1);
        }
    }
    private TreeNode deleteNode(TreeNode node) {
        if(node.left == null) return node.right;
        if(node.right == null) return node.left;
        TreeNode successor = minimum(node.right);
        node.data = successor.data;
        node.right = deleteMin(node.right);
        return rebalance(node);
    }
    private TreeNode deleteMin(TreeNode node) {
        if(node.left==null) return node.right;
        node.left = deleteMin(node.left);
        return rebalance(node);
    }
    private TreeNode minimum(TreeNode node) {
        while(node.left!=null) node = node.left;
        return node;
    }

    private TreeNode rebalance(TreeNode node) {
        if(node == null) return null;
        update(node);
        int balance = height(node.left) - height(node.right);
        if(balance > 1) {
            if(height(node.left.left) < height(node.left.right)) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if(balance < -1) {
            if(height(node.right.right) < height(node.right.left)) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }
    private TreeNode rotateLeft(TreeNode a) {
        TreeNode b = a.right;
        a.right = b.left;
        b.left = a;
        update(a);
        update(b);
        return b;
    }
    private TreeNode rotateRight(TreeNode b) {
        TreeNode a = b.left;
        b.left = a.right;
        a.right = b;
        update(b);
        update(a);
        return a;
    }

    private void update(TreeNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
        node.size = 1 + size(node.left) + size(node.right);
    }
    public int size() {
        return root == null ? 0 : root.size;
    }
    public List<T> toList() {
        List<T> output = new ArrayList<>();
        populateList(output, root);
        return output;
    }
    private void populateList(List<T> list, TreeNode node) {
        if(node==null) return;
        populateList(list, node.left);
        list.add(node.data);
        populateList(list, node.right);
    }
    private int height(TreeNode node) {
        if(node==null) return 0;
        return node.height;
    }
    private int size(TreeNode node) {
        if(node==null) return 0;
        return node.size;
    }

}
