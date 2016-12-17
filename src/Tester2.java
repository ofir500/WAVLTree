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

    public static void main(String[] args) {

        WAVLTree tree = new WAVLTree();
        List<Integer> nums = new ArrayList<>();
        Random rnd = new Random();

        for (int i = 1; i <= 500000; i++) {
            if (i == 10) {
                new Tester().new TreePrint().printNode(tree.getRoot());
            }
            //int num = rnd.nextInt(Integer.MAX_VALUE);
            int num = i;
            tree.insert(num, String.valueOf(num));
            nums.add(num);
        }

        System.out.printf("size: %d\theight: %d%n", tree.size(), height(tree.getRoot()));
        Collections.shuffle(nums);

        for (int i = 0; i < nums.size(); i++) {
            tree.delete(nums.get(i));
            if (i == nums.size() - 11) {
                new Tester().new TreePrint().printNode(tree.getRoot());
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


}
