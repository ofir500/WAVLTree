import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * WAVLTree
 * <p>
 * An implementation of a WAVL Tree with distinct integer keys and info
 */

public class WAVLTree {

	enum NodeType {
		LEAF, RIGHT_CHILD_ONLY, LEFT_CHILD_ONLY, TWO_CHILDREN;

		static NodeType of(WAVLNode node) {
			if (node.leftChild != null && node.rightChild != null) {
				return TWO_CHILDREN;
			} else if (node.leftChild != null) {
				return LEFT_CHILD_ONLY;
			} else if (node.rightChild != null) {
				return RIGHT_CHILD_ONLY;
			} else {
				return LEAF;
			}
		}
	}

	enum RankDiff {
		D0_1, D0_2, D1_0, D1_1, D1_2, D1_3, D2_0, D2_1, D2_2, D2_3, D3_1, D3_2;

		static RankDiff of(WAVLNode node) {
			int diffLeft = getRank(node) - getRank(node.leftChild);
			int diffRight = getRank(node) - getRank(node.rightChild);

			if (diffLeft == 0) {
				if (diffRight == 1)
					return D0_1;
				return D0_2;
			} else if (diffLeft == 1) {
				if (diffRight == 0) {
					return D1_0;
				} else if (diffRight == 1) {
					return D1_1;
				} else if (diffRight == 2) {
					return D1_2;
				}
				return D1_3;
			} else if (diffLeft == 2) {
				if (diffRight == 0) {
					return D2_0;
				} else if (diffRight == 1) {
					return D2_1;
				} else if (diffRight == 2) {
					return D2_2;
				}
				return D2_3;
			} else { // diffLeft == 3
				if (diffRight == 1) {
					return D3_1;
				}
				return D3_2;
			}
		}
	}

	private WAVLNode root;
	private int size;

	public WAVLTree() {
		this.root = null;
		this.size = 0;
	}

	private void setRoot(WAVLNode root) {
		this.root = root;
		if (this.root != null) {
			this.root.parent = null;
		}
	}

	/**
	 * returns true if and only if the tree is empty
	 */
	public boolean empty() {
		return size == 0;
	}

	/**
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	public String search(int k) {
		return search(k, this.root);
	}

	/**
	 * returns the info of an item with key k if it exists in the sub-tree
	 * otherwise, returns null
	 * 
	 * @param k
	 *            - key of node in the tree
	 * @param node
	 *            - root of a sub-tree
	 */
	private String search(int k, WAVLNode node) {
		while (true) {
			if (node == null) {
				return null;
			} else if (node.key == k) {
				return node.info;
			} else if (k > node.key) {
				node = node.rightChild;
			} else {
				node = node.leftChild;
			}
		}
	}

	/**
	 * inserts an item with key k and info i to the WAVL tree. the tree must
	 * remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were necessary. returns -1
	 * if an item with key k already exists in the tree.
	 */
	public int insert(int k, String i) {
		WAVLNode newNode = new WAVLNode(k, i);
		if (this.root == null) { // if tree is empty, add as root
			this.setRoot(newNode);
			this.size++;
			return 0;
		}

		WAVLNode currentNode = this.root;
		while (true) {
			if (k == currentNode.key) { // case 1: node with key k already
										// exists
				return -1;

			} else if (k > currentNode.key) { // node should be placed in the
												// right sub-tree
				if (currentNode.rightChild == null) { // we found where to place
														// the node
					currentNode.setRightChild(newNode);
					this.size++;
					break;
				} else {
					currentNode = currentNode.rightChild;
				}

			} else { // node should be placed in the left sub-tree
				if (currentNode.leftChild == null) { // we found where to place
														// the node
					currentNode.setLeftChild(newNode);
					this.size++;
					break;
				} else {
					currentNode = currentNode.leftChild;
				}
			}
		}

		return balanceAfterInsert(newNode.parent);
	}

	private int balanceAfterInsert(WAVLNode node) {
		if (node == null) {
			return 0;
		}

		RankDiff prev = RankDiff.D1_1;
		int counter = 0;

		while (node != null) {
			RankDiff diff = RankDiff.of(node);
			if (diff == RankDiff.D0_1) {
				node.rank++;
				counter++;
				prev = RankDiff.D1_2;
			} else if (diff == RankDiff.D1_0) {
				node.rank++;
				counter++;
				prev = RankDiff.D2_1;
			} else if (diff == RankDiff.D0_2) {
				if (prev == RankDiff.D1_2) {
					rotateRight(node, false);
					counter++;
				} else {
					doubleRotateLeftRight(node, false);
					counter += 2;
				}
				return counter;
			} else if (diff == RankDiff.D2_0) {
				if (prev == RankDiff.D2_1) {
					rotateLeft(node, false);
					counter++;
				} else {
					doubleRotateRightLeft(node, false);
					counter += 2;
				}
				return counter;
			} else if (diff == RankDiff.D1_1) {
				return counter;
			}

			node = node.parent;
		}
		return counter;
	}

	/**
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of
	 * rebalancing operations, or 0 if no rebalancing operations were needed.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k) {
		// we need to find the node to be deleted and its parent
		WAVLNode parent = null;
		WAVLNode child = this.root;
		boolean isLeftChild = false;

		while (true) {
			if (child == null) { // case 1: the key is not in the tree
				return -1;

			} else if (child.key == k) { // case 2: we found the node to be
											// deleted
				return delete(parent, child, isLeftChild);

			} else { // case 3: continue the search for the node
				parent = child;
				if (k < child.key) {
					child = child.leftChild;
					isLeftChild = true;
				} else {
					child = child.rightChild;
					isLeftChild = false;
				}
			}
		}
	}

	/**
	 * deletes a node from the tree; returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were needed.
	 * 
	 * @param parent
	 *            - parent node of the node to be deleted. null if node to be
	 *            deleted is the root
	 * @param child
	 *            - the node to be deleted
	 * @param isLeftChild
	 *            - should be set to true if the node to be deleted is a left
	 *            child of its parent
	 * @return
	 */
	public int delete(WAVLNode parent, WAVLNode child, boolean isLeftChild) {
		NodeType type = NodeType.of(child);
		int res;

		if (type == NodeType.LEAF) { // case 1: the node to be deleted has no
										// children.
			deleteLeafNode(parent, isLeftChild);

			if (parent == null) {
				return 0;
			} else if (NodeType.of(parent) == NodeType.LEAF) {
				res = balanceAfterDeletion(parent);
			} else if (isLeftChild) {
				res = balanceAfterDeletion(parent.rightChild);
			} else {
				res = balanceAfterDeletion(parent.leftChild);
			}

		} else if (type == NodeType.LEFT_CHILD_ONLY) { // case 2.1: the node to
														// be deleted has only a
														// left child
			deleteNodeWithOneChild(parent, child.leftChild, isLeftChild);
			res = balanceAfterDeletion(child.leftChild);

		} else if (type == NodeType.RIGHT_CHILD_ONLY) { // case 2.2: the node to
														// be deleted has only a
														// right child
			deleteNodeWithOneChild(parent, child.rightChild, isLeftChild);
			res = balanceAfterDeletion(child.rightChild);

		} else { // case 3: the node to be deleted has 2 children
			WAVLNode n = deleteNodeWithTwoChildren(parent, child, isLeftChild);
			res = balanceAfterDeletion(n);
		}

		size--;
		return res;
	}

	/**
	 * deletes a node with no children.
	 * 
	 * @param parentNode
	 *            - parent of the node to be deleted
	 * @param isLeftChild
	 *            - should be set to true if the node to be deleted is a left
	 *            child of its parent
	 */
	private void deleteLeafNode(WAVLNode parentNode, boolean isLeftChild) {
		if (parentNode == null) { // deletion of a leaf which is the root
			this.setRoot(null);
		} else if (isLeftChild) {
			parentNode.setLeftChild(null);
		} else {
			parentNode.setRightChild(null);
		}
	}

	/**
	 * deletes a node that has one child only
	 * 
	 * @param parent
	 *            - parent of the node to be deleted
	 * @param childOfChild
	 *            - the one child of the node to be deleted
	 * @param isLeftChild
	 *            - should be set to true if the node to be deleted is a left
	 *            child of its parent
	 */
	private void deleteNodeWithOneChild(WAVLNode parent, WAVLNode childOfChild, boolean isLeftChild) {
		if (parent == null) {
			this.setRoot(childOfChild);
		} else if (isLeftChild) {
			parent.setLeftChild(childOfChild);
		} else {
			parent.setRightChild(childOfChild);
		}
	}

	/**
	 * deletes a node that has two children
	 * 
	 * @param parent
	 *            - parent of the node to be deleted
	 * @param child
	 *            - the node to be deleted
	 * @param isLeftChild
	 *            - should be set to true if the node to be deleted is a left
	 *            child of its parent
	 */
	private WAVLNode deleteNodeWithTwoChildren(WAVLNode parent, WAVLNode child, boolean isLeftChild) {
		// find the successor of the node to be deleted
		ParentChildPair pair = minNode(child.rightChild);
		WAVLNode successor = pair.child;
		WAVLNode successorParent = pair.parent;

		// first, we disconnect our successor from its parent
		if (successorParent != null) {
			successorParent.setLeftChild(successor.rightChild);
		}

		// now we assign our successor a new parent
		if (parent == null) { // deletion of root
			this.setRoot(successor);
		} else if (isLeftChild) {
			parent.setLeftChild(successor);
		} else {
			parent.setRightChild(successor);
		}

		// now we assign our successor its new children - those of the node we
		// want to delete
		successor.rank = child.rank;
		successor.setLeftChild(child.leftChild);
		if (successorParent != null) { // to prevent a node from being its own
										// child
			successor.setRightChild(child.rightChild);
		}

		if (successorParent != null) {
			return successorParent;
		}
		return successor;
	}

	private int balanceAfterDeletion(WAVLNode node) {
		if (node == null) {
			return 0;
		}

		RankDiff prev = RankDiff.D1_1;
		int counter = 0;

		while (node != null) {
			RankDiff diff = RankDiff.of(node);
			try {
				if (diff == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
					node.rank--;
					counter++;
					prev = RankDiff.D1_1;
				} else if (diff == RankDiff.D3_2) {
					node.rank--;
					counter++;
					prev = RankDiff.D2_1;
				} else if (diff == RankDiff.D2_3) {
					node.rank--;
					counter++;
					prev = RankDiff.D1_2;
				} else if (diff == RankDiff.D3_1) {
					if (prev == RankDiff.D2_2) {
						node.rank--;
						node.rightChild.rank--;
						counter += 2;
						prev = RankDiff.D2_1;
					} else if (prev == RankDiff.D1_1 || prev == RankDiff.D2_1) {
						rotateLeft(node, true);
						counter++;
						return counter;
					} else {
						doubleRotateRightLeft(node, true);
						counter += 2;
						return counter;
					}
				} else if (diff == RankDiff.D1_3) {
					if (prev == RankDiff.D2_2) {
						node.rank--;
						node.leftChild.rank--;
						counter += 2;
						prev = RankDiff.D1_2;
					} else if (prev == RankDiff.D1_1 || prev == RankDiff.D1_2) {
						rotateRight(node, true);
						counter++;
						return counter;
					} else {
						doubleRotateLeftRight(node, true);
						counter += 2;
						return counter;
					}
				} else if (diff == RankDiff.D0_1) {
					node.rank++;
					counter++;
					prev = RankDiff.D1_2;
				} else if (diff == RankDiff.D1_0) {
					node.rank++;
					counter++;
					prev = RankDiff.D2_1;
				} else {
					prev = diff;
				}

				node = node.parent;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(diff);
				System.out.println(prev);
				System.out.println(node.rightChild);
				System.out.println(node.leftChild);
				System.out.println(node.rightChild.rank);
				System.out.println(node.leftChild.rank);
				System.out.println(node.rank);
				System.exit(1);
			}
		}

		return counter;
	}

	/**
	 * Returns the info of the item with the smallest key in the tree, or null
	 * if the tree is empty
	 */
	public String min() {
		if (this.root == null) {
			return null;
		}

		WAVLNode currentNode = this.root;
		while (currentNode.leftChild != null) {
			currentNode = currentNode.leftChild;
		}
		return currentNode.info;
	}

	/**
	 * finds the minimal node in a sub-tree, and its parent
	 *
	 * @param node
	 *            - root of sub-tree
	 * @return ParentChildPair - child is the minimal node of the sub-tree
	 * @throws NullPointerException
	 *             - if node is null
	 */
	private ParentChildPair minNode(WAVLNode node) {
		while (true) {
			if (node.leftChild != null && node.leftChild.leftChild == null) {
				return new ParentChildPair(node, node.leftChild);
			} else if (node.leftChild == null) {
				return new ParentChildPair(null, node);
			} else {
				node = node.leftChild;
			}
		}
	}

	/**
	 * Returns the info of the item with the largest key in the tree, or null if
	 * the tree is empty
	 */
	public String max() {
		if (this.root == null) {
			return null;
		}

		WAVLNode currentNode = this.root;
		while (currentNode.rightChild != null) {
			currentNode = currentNode.rightChild;
		}
		return currentNode.info;
	}

	/**
	 * Returns a sorted array which contains all keys in the tree, or an empty
	 * array if the tree is empty.
	 */
	public int[] keysToArray() {
		int[] arr = new int[this.size];
		keysToArray(this.root, arr, 0);
		return arr;
	}

	/**
	 * fills an array with all keys in the tree
	 */
	private int keysToArray(WAVLNode node, int[] arr, int i) {
		if (node == null) {
			return i;
		} else {
			i = keysToArray(node.leftChild, arr, i);
			arr[i] = node.key;
			i = keysToArray(node.rightChild, arr, i + 1);
			return i;
		}
	}

	/**
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty.
	 */
	public String[] infoToArray() {
		String[] arr = new String[this.size];
		infoToArray(this.root, arr, 0);
		return arr;
	}

	/**
	 * fills an array with all info in the tree
	 */
	private int infoToArray(WAVLNode node, String[] arr, int i) {
		if (node == null) {
			return i;
		} else {
			i = infoToArray(node.leftChild, arr, i);
			arr[i] = node.info;
			i = infoToArray(node.rightChild, arr, i + 1);
			return i;
		}
	}

	/**
	 * Returns the number of nodes in the tree.
	 */
	public int size() {
		return this.size;
	}

	/**
	 * returns the rank of a node or -1 if node is null (external node)
	 */
	private static int getRank(WAVLNode node) {
		if (node == null) { // external leaf
			return -1;
		}
		return node.rank;
	}

	private void rotateRight(WAVLNode node, boolean afterDeletion) {
		WAVLNode parent = node.parent;
		boolean isLeftChild = parent != null && node.parent.leftChild == node;

		WAVLNode k = node.leftChild;
		node.setLeftChild(k.rightChild);
		k.setRightChild(node);

		if (parent != null) {
			if (isLeftChild) {
				parent.setLeftChild(k);
			} else {
				parent.setRightChild(k);
			}
		} else {
			this.setRoot(k);
		}

		node.rank--;
		if (afterDeletion) {
			k.rank++;
			if (RankDiff.of(node) == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
				node.rank--;
			}
		}
	}

	private void rotateLeft(WAVLNode node, boolean afterDeletion) {
		WAVLNode parent = node.parent;
		boolean isLeftChild = parent != null && node.parent.leftChild == node;

		WAVLNode k = node.rightChild;
		node.setRightChild(k.leftChild);
		k.setLeftChild(node);

		if (parent != null) {
			if (isLeftChild) {
				parent.setLeftChild(k);
			} else {
				parent.setRightChild(k);
			}
		} else {
			this.setRoot(k);
		}

		node.rank--;
		if (afterDeletion) {
			k.rank++;
			if (RankDiff.of(node) == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
				node.rank--;
			}
		}
	}

	private void doubleRotateLeftRight(WAVLNode node, boolean afterDeletion) {
		rotateLeft(node.leftChild, afterDeletion);
		rotateRight(node, afterDeletion);

		if (afterDeletion) {
			node.rank--;
		} else {
			node.parent.rank++;
		}
	}

	private void doubleRotateRightLeft(WAVLNode node, boolean afterDeletion) {
		rotateRight(node.rightChild, afterDeletion);
		rotateLeft(node, afterDeletion);

		if (afterDeletion) {
			// node.rank--;
		} else {
			node.parent.rank++;
		}
	}

	public class WAVLNode {
		private Integer key;
		private String info;
		private int rank;

		private WAVLNode parent;
		private WAVLNode rightChild;
		private WAVLNode leftChild;

		private WAVLNode(Integer key, String info) {
			this.key = key;
			this.info = info;
			this.rank = 0;
			this.parent = null;
			this.rightChild = null;
			this.leftChild = null;
		}

		public void setRightChild(WAVLNode rightChild) {
			this.rightChild = rightChild;
			if (rightChild != null) {
				rightChild.parent = this;
			}
		}

		public void setLeftChild(WAVLNode leftChild) {
			this.leftChild = leftChild;
			if (leftChild != null) {
				leftChild.parent = this;
			}
		}

	}

	private static class ParentChildPair {
		WAVLNode parent;
		WAVLNode child;

		public ParentChildPair(WAVLNode parent, WAVLNode child) {
			this.parent = parent;
			this.child = child;
		}
	}

	public static void main(String[] args) {

		WAVLTree tree = new WAVLTree();

		/*
		 * tree.insert(5, "Bla5"); System.out.println("i5"); tree.insert(7,
		 * "Bla7"); System.out.println("i7"); tree.insert(6, "Bla6");
		 * System.out.println("i6"); tree.insert(2, "Bla2");
		 * System.out.println("i2"); tree.insert(2, "Bla22"); tree.insert(1,
		 * "Bla1"); tree.insert(8, "Bla8");
		 */
		/*
		 * for (int i = 1; i <= 10; i++) { tree.insert(i, String.valueOf(i)); }
		 * 
		 * TreePrint tp = tree.new TreePrint(); tp.printNode(tree.root);
		 * 
		 * System.out.printf("size: %d%n", tree.size()); System.out.printf(
		 * "max: %s%n", tree.max()); System.out.printf("min: %s%n", tree.min());
		 * System.out.println(Arrays.toString(tree.keysToArray()));
		 * System.out.println(Arrays.toString(tree.infoToArray()));
		 * System.out.printf("info of 2: %s%n", tree.search(2));
		 * System.out.printf("info of 1: %s%n", tree.search(1));
		 * System.out.printf("info of 8: %s%n", tree.search(8));
		 * System.out.printf("info of 10: %s%n", tree.search(10));
		 * 
		 * tree.delete(5); tp.printNode(tree.root); tree.delete(6);
		 * tp.printNode(tree.root); tree.delete(7); tp.printNode(tree.root); //
		 * tree.delete(8); tree.delete(2); tp.printNode(tree.root); //
		 * tree.delete(1);
		 * System.out.println(Arrays.toString(tree.keysToArray()));
		 * tree.delete(4); tp.printNode(tree.root);
		 */
		tree.run();

		tree.insert(2, null);
		tree.insert(1, null);
		tree.insert(4, null);
		tree.insert(3, null);
		TreePrint tp = tree.new TreePrint();
		tp.printNode(tree.root);
		tree.delete(1);

		tp.printNode(tree.root);

	}

	class TreePrint {

		public <T extends Comparable<?>> void printNode(WAVLTree.WAVLNode root) {
			int maxLevel = maxLevel(root);

			printNodeInternal(Collections.singletonList(root), 1, maxLevel);
		}

		private <T extends Comparable<?>> void printNodeInternal(List<WAVLTree.WAVLNode> list, int level,
				int maxLevel) {
			if (list.isEmpty() || isAllElementsNull(list))
				return;

			int floor = maxLevel - level;
			int endgeLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
			int firstSpaces = (int) Math.pow(2, (floor)) - 1;
			int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

			printWhitespaces(firstSpaces);

			List<WAVLTree.WAVLNode> newNodes = new ArrayList<WAVLTree.WAVLNode>();
			for (WAVLTree.WAVLNode node : list) {
				if (node != null) {
					System.out.print(node.rank);
					newNodes.add(node.leftChild);
					newNodes.add(node.rightChild);
				} else {
					newNodes.add(null);
					newNodes.add(null);
					System.out.print(" ");
				}

				printWhitespaces(betweenSpaces);
			}
			System.out.println("");

			for (int i = 1; i <= endgeLines; i++) {
				for (int j = 0; j < list.size(); j++) {
					printWhitespaces(firstSpaces - i);
					if (list.get(j) == null) {
						printWhitespaces(endgeLines + endgeLines + i + 1);
						continue;
					}

					if (list.get(j).leftChild != null)
						System.out.print("/");
					else
						printWhitespaces(1);

					printWhitespaces(i + i - 1);

					if (list.get(j).rightChild != null)
						System.out.print("\\");
					else
						printWhitespaces(1);

					printWhitespaces(endgeLines + endgeLines - i);
				}

				System.out.println("");
			}

			printNodeInternal(newNodes, level + 1, maxLevel);
		}

		private void printWhitespaces(int count) {
			for (int i = 0; i < count; i++)
				System.out.print(" ");
		}

		private <T extends Comparable<?>> int maxLevel(WAVLTree.WAVLNode root) {
			if (root == null)
				return 0;

			return Math.max(maxLevel(root.leftChild), maxLevel(root.rightChild)) + 1;
		}

		private <T> boolean isAllElementsNull(List<T> list) {
			for (Object object : list) {
				if (object != null)
					return false;
			}

			return true;
		}

	}

	public ArrayList<WAVLTree.WAVLNode> leafs = new ArrayList<WAVLTree.WAVLNode>();
	public WAVLTree generatedTree;
	public int totalInsertRebalances;
	public int totalDeleteRebalances;
	public int maxNumberOfRebalancesInInsert;
	public int maxNumberOfRebalancesInDelete;
	public ArrayList<Integer> keysInTree = new ArrayList<>();

	public void treeGenerator(int size, int IntsLimit) {
		Random random = new Random();
		generatedTree = new WAVLTree();
		int numberOfRebalances = 0;
		int randomNumber;
		System.out.println("Start inserting elements!");
		for (int i = 0; i < size; i++) {
			randomNumber = random.nextInt(IntsLimit);
			if (keysInTree.contains(randomNumber)) {
				i--;
				continue;
			}
			keysInTree.add(randomNumber);
			numberOfRebalances = generatedTree.insert(randomNumber, Integer.toString(randomNumber));
			totalInsertRebalances = numberOfRebalances > 0 ? totalInsertRebalances + numberOfRebalances
					: totalInsertRebalances;
			if (maxNumberOfRebalancesInInsert < numberOfRebalances)
				maxNumberOfRebalancesInInsert = numberOfRebalances;
			if ((i == ((int) size * 3 / 4)) || (i == (int) size / 2)) {
				System.out.println(Integer.toString(i)
						+ " elements were inserted to the tree\n##Testing ranks and ranks differences");
				RankTest(0); // Calls rank test method (Insert version)
			}
		}
		System.out.println(
				"all elements were inserted to the tree\n##Testing ranks and ranks differences one last time!! XDDDDD");
		RankTest(0); // Calls rank test method (Insert version)

		System.out.println("done inserting nodes!..");
		// Sort the key's list
		keysInTree.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}

		});
	}

	public void initialize() {
		this.generatedTree = null;
		this.keysInTree = new ArrayList<>();
		this.leafs = new ArrayList<>();
		this.totalDeleteRebalances = 0;
		this.totalInsertRebalances = 0;
	}

	public void testSize() {
		if (generatedTree.size() != this.keysInTree.size())
			System.err.println("Problem with size method! :/");
		else
			System.out.println("Size method works!");
	}

	public void testEmpty() {
		if (!generatedTree.empty())
			System.err.println("Problem with empty method! :/");
		else
			System.out.println("Empty method works!");
	}

	public void treeDelete() {
		int RebalancesInSingleDelete = 0;
		System.out.println("Start deleting nodes (in keys order)!");
		int firstTestItem = keysInTree.get((int) (keysInTree.size() / 2)); // Test
																			// ranks
																			// and
																			// rank-diff
																			// after
																			// deleting
																			// 1/2
																			// of
																			// the
																			// tree
		int secondTestItem = keysInTree.get((int) (3 * keysInTree.size() / 4)); // Test
																				// ranks
																				// and
																				// rank-diff
																				// after
																				// deleting
																				// 3/4
																				// of
																				// the
																				// tree
		for (int min : keysInTree) {
			RebalancesInSingleDelete = generatedTree.delete(min);
			totalDeleteRebalances = RebalancesInSingleDelete > 0 ? RebalancesInSingleDelete + totalDeleteRebalances
					: totalDeleteRebalances;
			if (maxNumberOfRebalancesInDelete < RebalancesInSingleDelete)
				maxNumberOfRebalancesInDelete = RebalancesInSingleDelete;
			if (min == firstTestItem) {
				System.out.println(((int) (keysInTree.size() / 2))
						+ " elements were deleted from the tree\n##Testing ranks and ranks differences");
				RankTest(1); // Calls rank test method (regular version)
			} else if (min == secondTestItem) {
				System.out.println(((int) (3 * keysInTree.size() / 4))
						+ " elements were deleted from the tree\n##Testing ranks and ranks differences");
				RankTest(1); // Calls rank test method (regular version)
			}
		}
		System.out.println("Done deleting nodes.. ;)");
	}

	public void testExistanceOfElements() {
		if (generatedTree.size != keysInTree.size())
			System.err.println("There is some problem with your insert method :'(");
		for (int key : keysInTree) {
			String val = generatedTree.search(key);
			if (val == null || !val.equals(Integer.toString(key))) {
				System.err.println("There is some problem with your insert method :'(");
				System.exit(1);
			}
		}
		System.out.println("All elements are in the tree! Good job.. :)");
	}

	public void createLeafList() {
		leafs = new ArrayList<>();
		/*
		 * int[] array = generatedTree.keysToArray(); for (int i : array) {
		 * WAVLTree.WAVLNode node =
		 * generatedTree.TreePosition(generatedTree.root, i); if (node.key == i
		 * && node.leftChild == null && node.rightChild == null)
		 * leafs.add(node); }
		 */
		shtut(leafs, generatedTree.root);
	}

	public void shtut(ArrayList<WAVLNode> shtut, WAVLNode node) {
		if (node == null) {
			return;
		}
		if (NodeType.of(node) == NodeType.LEAF) {
			shtut.add(node);
		} else {
			shtut(shtut, node.leftChild);
			shtut(shtut, node.rightChild);
		}
	}

	public void RankTest(int i) {
		createLeafList();
		boolean RanksTest = true;
		boolean RankDifferenceTest = true;
		if (i == 1) {
			for (WAVLTree.WAVLNode node : leafs) {
				RanksTest = RanksTest && RanksTest(node);
				RankDifferenceTest = RankDifferenceTest && RankDifferenceTest(node);
			}
		} else if (i == 0) {
			for (WAVLTree.WAVLNode node : leafs) {
				RanksTest = RanksTest && RanksTestInsertOnly(node);
				RankDifferenceTest = RankDifferenceTest && RankDifferenceTest(node);
			}

		}
		if (!RanksTest)
			System.err.println("Problem with ranks... :/");
		else
			System.out.println("Ranks are fine so far! ;)");
		if (!RankDifferenceTest)
			System.err.println("Problem with rank difference... :/");
		else
			System.out.println("Rank diff 'r great!!! XD");
	}

	public boolean RankDifferenceTest(WAVLTree.WAVLNode node) {
		while (node != null) {
			int leftDiff = node.leftChild != null ? node.rank - node.leftChild.rank : node.rank + 1;
			int rightDiff = node.rightChild != null ? node.rank - node.rightChild.rank : node.rank + 1;
			if ((rightDiff != 1 && rightDiff != 2) && (leftDiff != 1 && leftDiff != 2)) {
				return false;
			}
			node = node.parent;
		}
		return true;
	}

	// Ranks test, regular version!
	// In regular version the method check whether each node's rank is
	// max(node.left,node.right)+1 or max(node.left,node.right)+2

	public boolean RanksTest(WAVLTree.WAVLNode node) {
		while (node != null) {
			int leftChildsRank = node.leftChild != null ? node.leftChild.rank : -1;
			int rightChildsRank = node.rightChild != null ? node.rightChild.rank : -1;
			if ((node.rank != Math.max(rightChildsRank, leftChildsRank) + 1)
					&& (node.rank != Math.max(rightChildsRank, leftChildsRank) + 2))
				return false;
			node = node.parent;
		}
		return true;
	}

	// Ranks test, insert-only version!
	// In insert only version the method check whether each node's rank is
	// max(node.left,node.right)+1
	public boolean RanksTestInsertOnly(WAVLTree.WAVLNode node) {
		while (node != null) {
			int leftChildsRank = node.leftChild != null ? node.leftChild.rank : -1;
			int rightChildsRank = node.rightChild != null ? node.rightChild.rank : -1;
			if (node.rank != Math.max(rightChildsRank, leftChildsRank) + 1)
				return false;
			node = node.parent;
		}
		return true;
	}

	public void testMinAndMax() {
		if (!this.generatedTree.max().equals(Integer.toString(this.keysInTree.get(this.keysInTree.size() - 1))))
			System.err.println("Problem with max method.. :/");
		if (!this.generatedTree.min().equals(Integer.toString(this.keysInTree.get(0))))
			System.err.println("Problem with min method.. :/");
		System.out.println("Min&Max methods work.. ^_^");
	}

	public void testKeysToArrayAndInfoToArray() {
		String[] infoToArray = generatedTree.infoToArray();
		int[] keysToArray = generatedTree.keysToArray();
		int cnt = 0;
		for (int i : keysInTree) {
			if (!infoToArray[cnt].equals(Integer.toString(i))) {
				System.err.println("Problem with infoToArray method!! :S");
				System.exit(1);
			}
			if (keysToArray[cnt] != i) {
				System.err.println("Problem with keysToArrat method!! :S");
				System.exit(1);
			}
			cnt++;
		}
		System.out.println("infoToArray & keysToArray methods work.");

	}

	public void printTree() {
		TreePrint printer = new TreePrint();
		printer.printNode(generatedTree.root);
	}

	public void printInfo() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		System.out.println("********************************************************");
		System.out.println("Number of inserts: " + keysInTree.size());
		if (maxNumberOfRebalancesInInsert > 2 * Math.log10(keysInTree.size()) / Math.log10(2)) {
			System.err.println("2*log(" + keysInTree.size() + ")= "
					+ df.format(2 * (Math.log10(keysInTree.size()) / Math.log10(2)))
					+ "\nWorst case in insert is greater than log(n)!\nCheck your rebalance's methods!");
		}
		System.out.println("Number of rebalancing operations in all inserts: " + totalInsertRebalances);
		System.out.println("Worst Case rebalancing operations in single insert: " + maxNumberOfRebalancesInInsert);
		System.out.println("Average rebalancing operations in insert: "
				+ df.format((double) totalInsertRebalances / keysInTree.size()));
		if (maxNumberOfRebalancesInDelete > Math.log10(keysInTree.size()) / Math.log10(2)) {
			System.err.println(
					"log(" + keysInTree.size() + ")= " + df.format((Math.log10(keysInTree.size()) / Math.log10(2)))
							+ "\nWorst case in delete is greater than log(n)!\nCheck your rebalance's methods!");
		}
		System.out.println("Number of rebalancing operations in all deletes: " + totalDeleteRebalances);
		System.out.println("Worst Case rebalancing operations in single delete: " + maxNumberOfRebalancesInDelete);
		System.out.println("Average rebalancing operations in delete: "
				+ df.format((double) totalDeleteRebalances / keysInTree.size()));
		System.out.println("********************************************************");
		System.out.println();
	}

	public void runTest(int num, int max, int testNum) {
		System.out.println("************Test " + testNum + "************");
		treeGenerator(num, max);
		if (num < 50)
			printTree();
		testSize();
		testExistanceOfElements();
		testKeysToArrayAndInfoToArray();
		testMinAndMax();
		treeDelete();
		testEmpty();
		printInfo();

	}

	public void run() {
		runTest(20, 100, 0);
		initialize();
		for (int i = 1; i <= 10; i++) {
			runTest(i * 10000, i * 500000, i);
			initialize();
		}
	}

}
