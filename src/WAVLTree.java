import java.util.*;

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
			if (node == null) {
				return null;
			}
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

	public WAVLNode getRoot() {
		return this.root;
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

		if (type == NodeType.LEAF) { // case 1: the node to be deleted has no children.
			deleteLeafNode(parent, isLeftChild);
			res = balanceAfterDeletion(parent);

		} else if (type == NodeType.LEFT_CHILD_ONLY) { // case 2.1: the node to be deleted has only a left child
			deleteNodeWithOneChild(parent, child.leftChild, isLeftChild);
			res = balanceAfterDeletion(child.leftChild);

		} else if (type == NodeType.RIGHT_CHILD_ONLY) { // case 2.2: the node to be deleted has only a right child
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

		// now we assign our successor its new children - those of the node we want to delete
		successor.rank = child.rank;
		successor.setLeftChild(child.leftChild);
		if (successorParent != null) { // to prevent a node from being its own child
			successor.setRightChild(child.rightChild);
		}

		if (successorParent != null) {
			return successorParent;
		}
		return successor;
	}

	private int balanceAfterDeletion(WAVLNode node) {
		if (node == null) { // reached root. tree is balanced
			return 0;
		}

		int counter = 0;
		RankDiff diff = RankDiff.of(node);

		// we start by demoting a 2,2 leaf if we have one
		if (diff == RankDiff.D2_2 &&  NodeType.of(node) == NodeType.LEAF) {
			node.rank--;
			counter++;
			node = node.parent;
			diff = RankDiff.of(node);
		}

		while (node != null) {
			// single demote of leaf
 			if (diff == RankDiff.D3_2 || diff == RankDiff.D2_3) {
				node.rank--;
				counter++;

			} else if (diff == RankDiff.D3_1) {
				RankDiff prev = RankDiff.of(node.rightChild);
				// double demote
				if (prev == RankDiff.D2_2) {
					node.rank--;
					node.rightChild.rank--;
					counter += 2;

					// rotations
				} else if (prev == RankDiff.D1_1 || prev == RankDiff.D2_1) {
					rotateLeft(node, true);
					if (RankDiff.of(node) == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
						node.rank--;
					}
					counter++;
					return counter;

					// double rotation
				} else {
					doubleRotateRightLeft(node, true);
					counter += 2;
					return counter;
				}

				// symmetrical case
			} else if (diff == RankDiff.D1_3) {
				RankDiff prev = RankDiff.of(node.leftChild);

				// double demote
				if (prev == RankDiff.D2_2) {
					node.rank--;
					node.leftChild.rank--;
					counter += 2;

					// rotation
				} else if (prev == RankDiff.D1_1 || prev == RankDiff.D1_2) {
					rotateRight(node, true);
					if (RankDiff.of(node) == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
						node.rank--;
					}
					counter++;
					return counter;

					//double rotation
				} else {
					doubleRotateLeftRight(node, true);
					counter += 2;
					return counter;
				}
			}

			node = node.parent;
			diff = RankDiff.of(node);
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
			node.rank--;
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

		public Integer getKey() {
			return key;
		}

		public int getRank() {
			return this.rank;
		}

		public WAVLNode getParent() {
			return parent;
		}

		public WAVLNode getRightChild() {
			return rightChild;
		}

		public WAVLNode getLeftChild() {
			return leftChild;
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
		/*tree.insert(2, null);
		tree.insert(1, null);
		tree.insert(4, null);
		tree.insert(3, null);
		Tester.TreePrint tp = new Tester().new TreePrint(); tp.printNode(tree.root);
		tree.delete(1);
		tp.printNode(tree.root);*/

		List<Integer> nums = new ArrayList<>();
		Random rnd = new Random();
		for (int i = 1; i <= 100000; i++) {
			int num = rnd.nextInt(Integer.MAX_VALUE);
			tree.insert(num, String.valueOf(i));
			nums.add(num);
		}
		//checkTree(tree.root);
		Collections.shuffle(nums);

		for (int i=0; i < nums.size(); i++) {
			tree.delete(nums.get(i));
		}

		//Tester.TreePrint tp = new Tester().new TreePrint(); tp.printNode(tree.root);
		//Tester t = new Tester();
		//t.run();
		System.out.println("done");


		//Tester tester = new Tester();
		//tester.run();
	}


}
