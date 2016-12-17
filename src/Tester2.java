import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Tester2 {

    private static int height(WAVLTree.WAVLNode node) {
        if (node == null) {
            return -1;
        }
        return Math.max(height(node.getLeftChild()), height(node.getRightChild())) + 1;
    }

    private static int getRank(WAVLTree.WAVLNode node) {
        if (node == null) { // external leaf
            return -1;
        }
        return node.getRank();
    }

    private static void checkTreeAfterInsertions(WAVLTree.WAVLNode node) {
        if (node == null) {
            return;
        }
        if (height(node) != node.getRank()) {
            System.out.println("problem with ranks. tree is not a valid AVL tree");
            System.exit(1);
        }
        if (node.getLeftChild() != null && node.getLeftChild().getParent() != node) {
            System.out.println("problem with parenting");
            System.exit(1);
        }
        if (node.getRightChild() != null && node.getRightChild().getParent() != node) {
            System.out.println("problem with parenting");
            System.exit(1);
        }
        checkTreeAfterInsertions(node.getLeftChild());
        checkTreeAfterInsertions(node.getRightChild());
    }

    private static void checkTreeAfterDeletions(WAVLTree.WAVLNode node) {
        if (node == null) {
            return;
        }
        if (node.getLeftChild() != null && node.getLeftChild().getParent() != node) {
            System.out.println("problem with parenting");
            System.exit(1);
        }
        if (node.getRightChild() != null && node.getRightChild().getParent() != node) {
            System.out.println("problem with parenting");
            System.exit(1);
        }
        int x = getRank(node) - getRank(node.getRightChild());
        int y = getRank(node) - getRank(node.getLeftChild());
        if (x>2 || x <1) {
            System.out.println("Problem with ranks");
            System.exit(1);
        }
        if (y>2 || y <1) {
            System.out.println("Problem with ranks");
            System.exit(1);
        }
        checkTreeAfterDeletions(node.getLeftChild());
        checkTreeAfterDeletions(node.getRightChild());
    }

    public static void main(String[] args) {
        int numOfElements = 1_000_000;

        WAVLTree tree = new WAVLTree();
        List<Integer> nums = new ArrayList<>();
        Random rnd = new Random();

        System.out.println("Inserting "+ numOfElements + " elements");

        for (int i = 1; i <= numOfElements; i++) {
            int num = rnd.nextInt(Integer.MAX_VALUE);
            //int num = i;
            tree.insert(num, String.valueOf(num));
            nums.add(num);
        }

        System.out.println("Done insertion of " + numOfElements + " elements");
        System.out.println("Checking if tree is valid AVL tree");
        checkTreeAfterInsertions(tree.getRoot());
        System.out.println("Tree is valid");
        int height = height(tree.getRoot());
        System.out.printf("size: %d\theight: %d%n", tree.size(), height);
        checkHeight(tree, height);

        Collections.shuffle(nums);
        System.out.println("Now deleting elements in random order");
        for (int i = 0; i < nums.size(); i++) {
            tree.delete(nums.get(i));
            if (i % 10000 == 0) {
                System.out.println("Checking ranks");
                checkTreeAfterDeletions(tree.getRoot());
                System.out.println("Tree is still valid");
            }
        }

        if (tree.empty()) {
            System.out.println("done. tree is empty");
        } else {
            System.out.println("WTF");
        }

        //Tester tester = new Tester();
        //tester.run();
    }

    private static void checkHeight(WAVLTree tree, double height) {
        if (height <= 1.45 * (Math.log10(tree.size()) / Math.log10(2))) {
            System.out.println("height is <= 1.45*log(n)\n");
        } else {
            System.out.println("tree is not balanced.");
            System.exit(0);
        }
    }


}
