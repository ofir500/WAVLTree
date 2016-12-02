import java.util.Arrays;

/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree with distinct integer keys and info
 *
 */

public class WAVLTree {

	private WAVLNode root;
	private int size;

	public WAVLTree() {
		this.root = null;
		this.size = 0;
	}

	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 *
	 */
	public boolean empty() {
		return size == 0;
	}

	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	public String search(int k) {
		return search(k, this.root);
	}

	public String search(int k, WAVLNode node) {
		if (node == null) {
			return null;
		} else if (node.key == k) {
			return node.getInfo();
		} else if (k > node.key) {
			return search(k, node.getRightChild());
		} else {
			return search(k, node.leftChild);
		}
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the WAVL tree. the tree must
	 * remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were necessary. returns -1
	 * if an item with key k already exists in the tree.
	 */
	public int insert(int k, String i) {
		WAVLNode newNode = new WAVLNode(k, i);
		if (this.root == null) { // if tree is empty, add as root
			this.root = newNode;
			this.size++;
			return 0;
		}

		WAVLNode currentNode = this.root;
		while (true) {
			if (currentNode.key == k) {
				currentNode.setInfo(i);
				return 0;
			} else if (k > currentNode.getKey()) {
				if (currentNode.rightChild == null) {
					currentNode.rightChild = newNode;
					this.size++;
					return 0;
				} else {
					currentNode = currentNode.rightChild;
				}
			} else {
				if (currentNode.leftChild == null) {
					currentNode.leftChild = newNode;
					this.size++;
					return 0;
				} else {
					currentNode = currentNode.leftChild;
				}
			}
		}
	}

	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of
	 * rebalancing operations, or 0 if no rebalancing operations were needed.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k) {
		if (this.root == null) {
			return -1;
		}

		WAVLNode parent = null;
		WAVLNode child = this.root;

		while (true) {
			if (child == null) {
				return -1;
			} else if (child.key == k) {
				return delete(parent, child);
			} else {
				parent = child;
				if (k < child.getKey()) {
					child = child.getLeftChild();
				} else {
					child = child.getRightChild();
				}
			}
		}
	}

	public int delete(WAVLNode parent, WAVLNode child) {
		if (isLeaf(child)) {
			if (parent.getLeftChild() == child) {
				parent.setLeftChild(null);
			} else {
				parent.setRightChild(null);
			}
			size--;
			return 0;
		}

		int flag = hasOneChild(child);
		if (flag == 0) { // only left child
			if (parent.getLeftChild() == child) {
				parent.setLeftChild(child.getLeftChild());
			} else {
				parent.setRightChild(child.getLeftChild());
			}
			size--;
			return 0;
		} else if (flag == 1) { // only right child
			if (parent.getLeftChild() == child) {
				parent.setLeftChild(child.getRightChild());
			} else {
				parent.setRightChild(child.getRightChild());
			}
			size--;
			return 0;
		}

		// our child has 2 chlidren, find it's predecessor
		ParentChildPair pair = minNode(child.rightChild);
		WAVLNode pred = pair.child;
		WAVLNode predParent = pair.parent;
		if (predParent != null) {
			predParent.setLeftChild(null);
		}
		if (parent != null && parent.getLeftChild() == child) {
			parent.setLeftChild(pred);
		} else if (parent != null) {
			parent.setRightChild(pred);
		} else {
			this.root = pred;
		}
		pred.setLeftChild(child.leftChild);
		if (predParent != null) {
			pred.setRightChild(child.rightChild);
		}
		size--;
		return 0;
	}

	private boolean isLeaf(WAVLNode node) {
		return node.getLeftChild() == null && node.getRightChild() == null;
	}

	/**
	 * 
	 * @param node
	 * @return -1 if leaf or has 2 childs, 0 if has just left child, 1 if has
	 *         only right child
	 */
	private int hasOneChild(WAVLNode node) {
		if (node.getLeftChild() != null && node.getRightChild() == null) {
			return 0;
		} else if (node.getRightChild() != null && node.getLeftChild() == null) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * public String min()
	 *
	 * Returns the iîfo of the item with the smallest key in the tree, or null
	 * if the tree is empty
	 */
	public String min() {
		return min(this.root);
	}

	private String min(WAVLNode node) {
		if (node == null) {
			return null;
		} else if (node.getLeftChild() == null) {
			return node.getInfo();
		} else {
			return min(node.getLeftChild());
		}
	}

	private ParentChildPair minNode(WAVLNode node) {
		if (node.getLeftChild() != null && node.getLeftChild().getLeftChild() == null) {
			return new ParentChildPair(node, node.getLeftChild());
		} else if (node.getLeftChild() == null) {
			return new ParentChildPair(null, node);
		} else {
			return minNode(node.getLeftChild());
		}
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree, or null if
	 * the tree is empty
	 */
	public String max() {
		return max(this.root);
	}

	private String max(WAVLNode node) {
		if (node == null) {
			return null;
		} else if (node.getRightChild() == null) {
			return node.getInfo();
		} else {
			return max(node.getRightChild());
		}
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree, or an empty
	 * array if the tree is empty.
	 */
	public int[] keysToArray() {
		KeysArray res = new KeysArray(this.size);
		keysToArray(this.root, res);
		return res.arr;
	}

	private void keysToArray(WAVLNode node, KeysArray arr) {
		if (node == null) {
			return;
		} else {
			keysToArray(node.leftChild, arr);
			arr.add(node.getKey());
			keysToArray(node.getRightChild(), arr);
		}
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty.
	 */
	public String[] infoToArray() {
		InfosArray res = new InfosArray(this.size);
		infoToArray(this.root, res);
		return res.arr;
	}

	private void infoToArray(WAVLNode node, InfosArray arr) {
		if (node == null) {
			return;
		} else {
			infoToArray(node.getLeftChild(), arr);
			arr.add(node.getInfo());
			infoToArray(node.getRightChild(), arr);
		}
	}

	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree.
	 *
	 * precondition: none postcondition: none
	 */
	public int size() {
		return this.size;
	}

	private int getRank(WAVLNode node) {
		if (node == null) {
			return -1;
		}
		return node.getRank();
	}

	/**
	 * 
	 */
	public static class WAVLNode {
		private Integer key;
		private String info;
		private int rank;

		private WAVLNode rightChild;
		private WAVLNode leftChild;

		public WAVLNode(Integer key, String info) {
			this.key = key;
			this.info = info;
			this.rank = 0;
			this.rightChild = null;
			this.leftChild = null;
		}

		public Integer getKey() {
			return key;
		}

		public void setKey(Integer key) {
			this.key = key;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}

		private int getRank() {
			return rank;
		}

		private void setRank(int rank) {
			this.rank = rank;
		}

		public WAVLNode getRightChild() {
			return rightChild;
		}

		public void setRightChild(WAVLNode rightChild) {
			this.rightChild = rightChild;
		}

		public WAVLNode getLeftChild() {
			return leftChild;
		}

		public void setLeftChild(WAVLNode leftChild) {
			this.leftChild = leftChild;
		}
	}

	private static class KeysArray {
		int[] arr;
		int i;

		private KeysArray(int size) {
			this.arr = new int[size];
			this.i = 0;
		}

		private void add(int k) {
			arr[i] = k;
			i += 1;
		}
	}

	private static class InfosArray {
		String[] arr;
		int i;

		InfosArray(int size) {
			this.arr = new String[size];
			this.i = 0;
		}

		void add(String info) {
			arr[i] = info;
			i += 1;
		}
	}

	static class ParentChildPair {
		WAVLNode parent;
		WAVLNode child;

		public ParentChildPair(WAVLNode parent, WAVLNode child) {
			this.parent = parent;
			this.child = child;
		}
	}

	public static void main(String[] args) {

		WAVLTree tree = new WAVLTree();

		tree.insert(5, "Bla5");
		tree.insert(7, "Bla7");
		tree.insert(6, "Bla6");
		tree.insert(2, "Bla2");
		tree.insert(2, "Bla22");
		tree.insert(1, "Bla1");
		tree.insert(8, "Bla8");
		System.out.println("takua3");
		System.out.printf("size: %d%n", tree.size());
		System.out.printf("max: %s%n", tree.max());
		System.out.printf("min: %s%n", tree.min());
		System.out.println(Arrays.toString(tree.keysToArray()));
		System.out.println(Arrays.toString(tree.infoToArray()));
		System.out.printf("info of 2: %s%n", tree.search(2));

		tree.delete(1);
		System.out.println(Arrays.toString(tree.keysToArray()));
	}
}
