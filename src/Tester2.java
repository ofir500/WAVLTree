import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Tester2 {

	static List<Integer> keysInTree = new ArrayList<>();

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

	private static void insertNonRandomElements(WAVLTree tree, int numOfElements) {
		System.out.println("Inserting " + numOfElements + " elements");
		for (int i = 1; i <= numOfElements; i++) {
			;
			tree.insert(i, String.valueOf(i));
			keysInTree.add(i);
		}

		System.out.println("Done insertion of " + numOfElements + " elements");
	}

	private static void insertRandomElements(WAVLTree tree, int numOfElements) {
		Random rnd = new Random();
		System.out.println("Inserting " + numOfElements + " elements");

		for (int i = 1; i <= numOfElements; i++) {
			int num = rnd.nextInt(Integer.MAX_VALUE);
			tree.insert(num, String.valueOf(num));
			keysInTree.add(num);
		}

		System.out.println("Done insertion of " + numOfElements + " elements");
	}

	private static void deleteRandomKeys(WAVLTree tree) {
		Collections.shuffle(keysInTree);
		System.out.println("Deleting elements in random order");
		for (int i = 0; i < keysInTree.size(); i++) {
			tree.delete(keysInTree.get(i));
			if (i % 10000 == 0) {
				System.out.println("Checking ranks");
				if (!checkTreeAfterDeletions(tree.getRoot())) {
					System.out.println("Something went wrong with the ranks during deletion");
					System.exit(1);
				} else {
					System.out.println("Tree is still valid");
				}
			}
		}
	}

	private static boolean checkTreeAfterInsertions(WAVLTree.WAVLNode node) {
		if (node == null) {
			return true;
		} else if (height(node) != node.getRank()) {
			return false;
		}
		int x = getRank(node) - getRank(node.getRightChild());
		int y = getRank(node) - getRank(node.getLeftChild());
		if (x > 2 || x < 1) {
			return false;
		}
		if (y > 2 || y < 1) {
			return false;
		}
		if (x == 2 && y == 2) {
			return false;
		}

		boolean res = checkTreeAfterInsertions(node.getLeftChild());
		res = res && checkTreeAfterInsertions(node.getRightChild());
		return res;
	}

	private static boolean checkTreeAfterDeletions(WAVLTree.WAVLNode node) {
		if (node == null) {
			return true;
		}
		int x = getRank(node) - getRank(node.getRightChild());
		int y = getRank(node) - getRank(node.getLeftChild());
		if (x > 2 || x < 1) {
			return false;
		}
		if (y > 2 || y < 1) {
			return false;
		}

		boolean res = checkTreeAfterDeletions(node.getLeftChild());
		res = res && checkTreeAfterDeletions(node.getRightChild());
		return res;
	}

	private static boolean checkHeight(WAVLTree tree) {
		int height = height(tree.getRoot());
		if (height <= 1.45 * (Math.log10(tree.size()) / Math.log10(2)))
			return true;
		return false;
	}

	private static boolean checkSearchMethod(WAVLTree tree) {
		Collections.shuffle(keysInTree);
		for (int key : keysInTree) {
			String value = tree.search(key);
			if (value == null || !(value.equals(String.valueOf(key)))) {
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args) {
		int numOfElements = 1_000_000;
		WAVLTree tree = new WAVLTree();

		System.out.println("**********************************************");
		insertRandomElements(tree, numOfElements);
		//insertNonRandomElements(tree, numOfElements);

		System.out.println("Checking if all keys are in the tree");
		if (!checkSearchMethod(tree)) {
			System.out.println("Not all keys were inserted, or search function doesn't work");
			System.exit(1);
		} else {
			System.out.println("All keys are in the tree");
		}

		System.out.println("Checking if tree is a valid AVL tree");
		if (!checkTreeAfterInsertions(tree.getRoot())) {
			System.out.println("Tree is NOT a valid AVL tree");
			System.exit(1);
		} else {
			System.out.println("Tree is valid");
		}

		/*System.out.println("Checking if height is smaller than 1.45log(n)");
		if (!checkHeight(tree)) {
			System.out.println("Height of tree is not valid. tree is not balanced");
		} else {
			System.out.println("Height is valid");
		}*/

		System.out.println("**********************************************\n");

		deleteRandomKeys(tree);

		if (tree.empty()) {
			System.out.println("done. tree is empty");
		} else {
			System.out.println("WTF: tree is marked as not empty yet we deleted all keys");
		}
	}

}
