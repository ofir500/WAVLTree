import java.util.Arrays;

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

	private WAVLNode root;
	private int size;

	public WAVLTree() {
		this.root = null;
		this.size = 0;
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
			this.root = newNode;
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
					currentNode.rightChild = newNode;
					this.size++;
					return 0;
				} else {
					currentNode = currentNode.rightChild;
				}

			} else { // node should be placed in the left sub-tree
				if (currentNode.leftChild == null) { // we found where to place
														// the node
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
		if (type == NodeType.LEAF) { // case 1: the node to be deleted has no
										// children.
			deleteLeafNode(parent, isLeftChild);

		} else if (type == NodeType.LEFT_CHILD_ONLY) { // case 2.1: the node to
														// be deleted has only a
														// left child
			deleteNodeWithOneChild(parent, child.leftChild, isLeftChild);

		} else if (type == NodeType.RIGHT_CHILD_ONLY) { // case 2.2: the node to
														// be deleted has only a
														// right child
			deleteNodeWithOneChild(parent, child.rightChild, isLeftChild);

		} else { // case 3: the node to be deleted has 2 children
			deleteNodeWithTwoChildren(parent, child, isLeftChild);
		}

		size--;
		return 0;
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
			this.root = null;
		} else if (isLeftChild) {
			parentNode.leftChild = null;
		} else {
			parentNode.rightChild = null;
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
			this.root = childOfChild;
		} else if (isLeftChild) {
			parent.leftChild = childOfChild;
		} else {
			parent.rightChild = childOfChild;
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
	private void deleteNodeWithTwoChildren(WAVLNode parent, WAVLNode child, boolean isLeftChild) {
		// find the successor of the node to be deleted
		ParentChildPair pair = minNode(child.rightChild);
		WAVLNode successor = pair.child;
		WAVLNode successorParent = pair.parent;

		// first, we disconnect our successor from its parent
		if (successorParent != null) {
			successorParent.leftChild = successor.rightChild;
		}

		// now we assign our successor a new parent
		if (parent == null) { // deletion of root
			this.root = successor;
		} else if (isLeftChild) {
			parent.leftChild = successor;
		} else {
			parent.rightChild = successor;
		}

		// now we assign our successor its new children - those of the node we
		// want to delete
		successor.leftChild = child.leftChild;
		if (successorParent != null) { // to prevent a node from being its own
										// child
			successor.rightChild = child.rightChild;
		}
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
	private int getRank(WAVLNode node) {
		if (node == null) { // external leaf
			return -1;
		}
		return node.rank;
	}

	private static class WAVLNode {
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
			if (leftChild != null) {
				leftChild.parent = this;
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

		tree.insert(5, "Bla5");
		tree.insert(7, "Bla7");
		tree.insert(6, "Bla6");
		tree.insert(2, "Bla2");
		tree.insert(2, "Bla22");
		tree.insert(1, "Bla1");
		tree.insert(8, "Bla8");

		System.out.printf("size: %d%n", tree.size());
		System.out.printf("max: %s%n", tree.max());
		System.out.printf("min: %s%n", tree.min());
		System.out.println(Arrays.toString(tree.keysToArray()));
		System.out.println(Arrays.toString(tree.infoToArray()));
		System.out.printf("info of 2: %s%n", tree.search(2));
		System.out.printf("info of 1: %s%n", tree.search(1));
		System.out.printf("info of 8: %s%n", tree.search(8));
		System.out.printf("info of 10: %s%n", tree.search(10));

		tree.delete(5);
		tree.delete(6);
		tree.delete(7);
		// tree.delete(8);
		tree.delete(2);
		// tree.delete(1);
		System.out.println(Arrays.toString(tree.keysToArray()));
	}
}
