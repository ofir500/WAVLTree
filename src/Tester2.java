import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Ofir on 02/12/2016.
 */
public class Tester2 {

    public static void main(String[] args) {

        WAVLTree tree = new WAVLTree();
        List<Integer> nums = new ArrayList<>();
        Random rnd = new Random();

        for (int i = 1; i <= 100000; i++) {
            int num = rnd.nextInt(Integer.MAX_VALUE);
            tree.insert(num, String.valueOf(num));
            nums.add(num);
        }

        Collections.shuffle(nums);

        for (int i = 0; i < nums.size(); i++) {
            tree.delete(nums.get(i));
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
