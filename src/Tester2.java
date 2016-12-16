import java.util.*;

/**
 * Created by Ofir on 02/12/2016.
 */
public class Tester2 {

    public static void main(String[] args) {
       WAVLTree b = new WAVLTree();
        b.insert(3, "3");b.insert(8, "8");
        b.insert(1, "1");b.insert(4, "4");b.insert(6,"6");b.insert(2,"2");b.insert(10,"10");b.insert(9,"9");
        b.insert(20,"20");b.insert(25,"25");b.insert(15,"15");b.insert(16,"16");
        System.out.println("Original Tree : ");
        System.out.println("Size: " + b.size());
        System.out.println(Arrays.toString(b.keysToArray()));
        System.out.println("");
        System.out.println("Check whether Node with value 4 exists : " + b.search(4));
        System.out.println("Delete Node with no children (2) : " + b.delete(2));
        System.out.println("Size: " + b.size());
        System.out.println(Arrays.toString(b.keysToArray()));
        System.out.println("\n Delete Node with one child (4) : " + b.delete(4));
        System.out.println("Size: " + b.size());
        System.out.println(Arrays.toString(b.keysToArray()));
        System.out.println("\n Delete Node with Two children (10) : " + b.delete(10));
        System.out.println("Size: " + b.size());
        System.out.println(Arrays.toString(b.keysToArray()));
    }


}
