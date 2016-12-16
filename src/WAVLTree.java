/**
 * WAVLTree
 * <p>
 * An implementation of a WAVL Tree with distinct integer keys and info
 */

public class WAVLTree {

	enum NodeType {
		LEAF, UNARY_RIGHT, UNARY_LEFT, TWO_CHILDREN;

		static NodeType of(WAVLNode node) {
			if (node.leftChild != null && node.rightChild != null) {
				return TWO_CHILDREN;
			} else if (node.leftChild != null) {
				return UNARY_LEFT;
			} else if (node.rightChild != null) {
				return UNARY_RIGHT;
			} else {
				return LEAF;
			}
		}
	}

	// this enum represents the rank differences of a node.
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

	/**
	 * returns the root of the tree.
	 * package-private. for testing purposes only
	 */
	WAVLNode getRoot() {
		return this.root;
	}

	/**
	 * sets a new root and sets its parent to null
	 * @param root - new root of tree
	 */
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
		WAVLNode node = search(k, this.root);
		if (node == null) {
			return null;
		}

		return node.info;
	}

	/**
	 * returns the info of an item with key k if it exists in the sub-tree
	 * otherwise, returns null
	 *
	 * @param k    - key of node in the tree
	 * @param node - root of a sub-tree
	 */
	private WAVLNode search(int k, WAVLNode node) {
		while (true) {
			if (node == null) {
				return null;
			} else if (node.key == k) {
				return node;
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
			// case 1: node with key k already exists
			if (k == currentNode.key) {
				return -1;

				// case 2: node should be placed in the right sub-tree
			} else if (k > currentNode.key) {
				if (currentNode.rightChild == null) {
					// we found where to place the node
					currentNode.setRightChild(newNode);
					this.size++;
					break;
				} else { // continue the search
					currentNode = currentNode.rightChild;
				}

				// case 3: node should be placed in the left sub-tree
			} else {
				if (currentNode.leftChild == null) {
					// we found where to place the node
					currentNode.setLeftChild(newNode);
					this.size++;
					break;
				} else {
					currentNode = currentNode.leftChild;
				}
			}
		}

		return rebalanceAfterInsert(newNode.parent);
	}

	/**
	 * rebalances the tree after an insertion to maintain valid rank differences.
	 * returns the amount of rebalancing operations needed.
	 * algorithm is as described in course presentation using rank differences of a node.
	 */
	private int rebalanceAfterInsert(WAVLNode node) {
		if (node == null) { // reached root. tree is balanced
			return 0;
		}

		RankDiff prev = RankDiff.D1_1;
		int counter = 0;

		while (node != null) {
			RankDiff diff = RankDiff.of(node);

			// promotion
			if (diff == RankDiff.D0_1) {
				node.rank++;
				counter++;
				prev = RankDiff.D1_2;

				// promotion. symmetrical case
			} else if (diff == RankDiff.D1_0) {
				node.rank++;
				counter++;
				prev = RankDiff.D2_1;

				// rotation required
			} else if (diff == RankDiff.D0_2) {
				if (prev == RankDiff.D1_2) {
					rotateRight(node, false);
					counter++;
				} else {
					doubleRotateLeftRight(node, false);
					counter += 2;
				}
				return counter;

				// rotation required. symmetrical case
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
		// we need to find the node to be deleted
		WAVLNode node = search(k, this.root);
		if (node == null) {
			// the key is not in the tree
			return -1;
		}

		boolean isLeftChild = node.parent != null && node.parent.leftChild == node;
		return delete(node, isLeftChild);
	}

	/**
	 * deletes a node from the tree; returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were needed.
	 *
	 * @param node        - the node to be deleted
	 * @param isLeftChild - should be set to true if the node to be deleted is a left
	 *                    child of its parent
	 */
	public int delete(WAVLNode node, boolean isLeftChild) {
		NodeType type = NodeType.of(node);
		int res;

		// case 1: the node to be deleted has no children.
		if (type == NodeType.LEAF) {
			deleteLeafNode(node.parent, isLeftChild);
			res = rebalanceAfterDeletion(node.parent);

			// case 2.1: the node to be deleted has only a left child
		} else if (type == NodeType.UNARY_LEFT) {
			deleteNodeWithOneChild(node.parent, node.leftChild, isLeftChild);
			res = rebalanceAfterDeletion(node.leftChild);

			// case 2.2: the node to be deleted has only a right child
		} else if (type == NodeType.UNARY_RIGHT) {
			deleteNodeWithOneChild(node.parent, node.rightChild, isLeftChild);
			res = rebalanceAfterDeletion(node.rightChild);

			// case 3: the node to be deleted has 2 children
		} else {
			WAVLNode n = deleteNodeWithTwoChildren(node, isLeftChild);
			res = rebalanceAfterDeletion(n);
		}

		size--;
		return res;
	}

	/**
	 * deletes a node with no children.
	 *
	 * @param parent  - parent of the node to be deleted
	 * @param isLeftChild - should be set to true if the node to be deleted is a left
	 *                    child of its parent
	 */
	private void deleteLeafNode(WAVLNode parent, boolean isLeftChild) {
		if (parent == null) { // deletion of a leaf which is the root
			this.setRoot(null);
		} else if (isLeftChild) {
			parent.setLeftChild(null);
		} else {
			parent.setRightChild(null);
		}
	}

	/**
	 * deletes a node that has one child only
	 *
	 * @param parent       - parent of the node to be deleted
	 * @param childOfChild - the one child of the node to be deleted
	 * @param isLeftChild  - should be set to true if the node to be deleted is a left
	 *                     child of its parent
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
	 * deletes a node that has two children by replacing it with its successor.
	 * returns the successor's old parent,
	 * or the successor itself if the parent is the node we deleted.
	 *
	 * @param node       - the node to be deleted
	 * @param isLeftChild - should be set to true if the node to be deleted is a left
	 *                    child of its parent
	 */
	private WAVLNode deleteNodeWithTwoChildren(WAVLNode node, boolean isLeftChild) {
		// find the successor of the node to be deleted, and its parent
		// we set its parent to null if the parent is the node to be deleted
		WAVLNode successor = min(node.rightChild);
		WAVLNode successorParent = successor.parent != node ? successor.parent : null;

		// first, we disconnect our successor from its parent
		if (successorParent != null) {
			successorParent.setLeftChild(successor.rightChild);
		}

		// now we assign our successor a new parent
		if (node.parent == null) { // deletion of root
			this.setRoot(successor);
		} else if (isLeftChild) {
			node.parent.setLeftChild(successor);
		} else {
			node.parent.setRightChild(successor);
		}

		// now we assign our successor its new children - those of the node we want to delete
		successor.setLeftChild(node.leftChild);
		if (successorParent != null) { // to prevent a node from being its own child
			successor.setRightChild(node.rightChild);
		}

		//maintain rank
		successor.rank = node.rank;

		return successorParent != null ? successorParent : successor;
	}

	/**
	 * rebalances the tree after a deletion to maintain valid rank differences.
	 * returns the amount of rebalancing operations needed.
	 * algorithm is as described in course presentation using rank differences of a node
	 */
	private int rebalanceAfterDeletion(WAVLNode node) {
		if (node == null) { // reached root. tree is balanced
			return 0;
		}

		int counter = 0;
		RankDiff diff = RankDiff.of(node);

		// we start by demoting a 2,2 leaf if we have one
		if (diff == RankDiff.D2_2 && NodeType.of(node) == NodeType.LEAF) {
			node.rank--;
			counter++;
			node = node.parent;
			diff = RankDiff.of(node);
		}

		while (node != null) {
			// single demote
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
					// after rotation we might have created a 2,2 leaf, check and fix
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
					// after rotation we might have created a 2,2 leaf, check and fix
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
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty
	 */
	public String min() {
		WAVLNode node = min(this.root);
		if (node == null) {
			return null;
		}

		return node.info;
	}

	/**
	 * finds the minimal node in a sub-tree
	 *
	 * @param node - root of sub-tree
	 */
	private WAVLNode min(WAVLNode node) {
		if (node == null) {
			return null;
		}
		while (node.leftChild != null) {
			node = node.leftChild;
		}
		return node;
	}

	/**
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty
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

	/**
	 * rotates a node to the right.
	 * @param node - node to be rotated
	 * @param afterDeletion - should be set to true only if the rotation is done after a delete operation.
	 *                      rotation after insertion requires different rank maintaining than after deletion.
	 */
	private void rotateRight(WAVLNode node, boolean afterDeletion) {
		WAVLNode parent = node.parent;
		boolean isLeftChild = parent != null && node.parent.leftChild == node;

		// make the rotation
		WAVLNode k = node.leftChild;
		node.setLeftChild(k.rightChild);
		k.setRightChild(node);

		// connect the rotated sub-tree to the tree
		if (parent != null) {
			if (isLeftChild) {
				parent.setLeftChild(k);
			} else {
				parent.setRightChild(k);
			}
		} else {
			this.setRoot(k);
		}

		// maintain ranks
		node.rank--;
		if (afterDeletion) {
			k.rank++;
		}
	}

	/**
	 * rotates a node to the left.
	 * @param node - node to be rotated
	 * @param afterDeletion - should be set to true only if the rotation is done after a delete operation.
	 *                      rotation after insertion requires different rank maintaining than after deletion.
	 */
	private void rotateLeft(WAVLNode node, boolean afterDeletion) {
		WAVLNode parent = node.parent;
		boolean isLeftChild = parent != null && node.parent.leftChild == node;

		// make the rotation
		WAVLNode k = node.rightChild;
		node.setRightChild(k.leftChild);
		k.setLeftChild(node);

		// connect the rotated sub-tree to the tree
		if (parent != null) {
			if (isLeftChild) {
				parent.setLeftChild(k);
			} else {
				parent.setRightChild(k);
			}
		} else {
			this.setRoot(k);
		}

		// maintain ranks
		node.rank--;
		if (afterDeletion) {
			k.rank++;
		}
	}

	/**
	 * makes a double rotation.
	 * first, a rotation to the left of the left child,
	 * then, a rotation to the right of the given node
	 * @param node - node to be double rotated
	 * @param afterDeletion - should be set to true only if the rotation is done after a delete operation.
	 *                      rotation after insertion requires different rank maintaining than after deletion.
	 */
	private void doubleRotateLeftRight(WAVLNode node, boolean afterDeletion) {
		rotateLeft(node.leftChild, afterDeletion);
		rotateRight(node, afterDeletion);

		if (afterDeletion) {
			node.rank--;
		} else {
			node.parent.rank++;
		}
	}

	/**
	 * makes a double rotation.
	 * first, a rotation to the right of the right child,
	 * then, a rotation to the left of the given node
	 * @param node - node to be double rotated
	 * @param afterDeletion - should be set to true only if the rotation is done after a delete operation.
	 *                      rotation after insertion requires different rank maintaining than after deletion.
	 */
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

		Integer getKey() {
			return key;
		}

		Integer getRank() {
			return this.rank;
		}

		WAVLNode getParent() {
			return parent;
		}

		WAVLNode getRightChild() {
			return rightChild;
		}

		WAVLNode getLeftChild() {
			return leftChild;
		}

		/*
			important: use ONLY the next functions to set children.
			these functions also maintain parents of nodes.
			setting rightChild or leftChild directly will cause serious stability issues
		 */

		private void setRightChild(WAVLNode rightChild) {
			this.rightChild = rightChild;
			if (rightChild != null) {
				rightChild.parent = this;
			}
		}

		private void setLeftChild(WAVLNode leftChild) {
			this.leftChild = leftChild;
			if (leftChild != null) {
				leftChild.parent = this;
			}
		}

	}

}
