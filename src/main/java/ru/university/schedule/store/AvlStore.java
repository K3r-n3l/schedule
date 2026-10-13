package ru.university.schedule.store;

import java.util.List;
import java.util.Optional;

public class AvlStore<K extends Comparable<K>, T> implements EntityStore<K, T> {
    protected final class Node {
        K key;
        T value;
        Node left, right;
        int height;

        Node(K key, T value) {
            this.key = key;
            this.value = value;
            height = 1;
        }
    }

    protected Node root;
    protected int size;

    protected int height(Node node) {
        return node == null ? 0 : node.height;
    }

    protected int balanceFactor(Node node) {
        return height(node.right) - height(node.left);
    }

    protected void updateHeight(Node node) {
        int rheight = height(node.right);
        int lheight = height(node.left);
        node.height = Math.max(rheight, lheight) + 1;
    }

    protected Node rightRotate(Node node) {
        Node pivot = node.left;
        node.left = pivot.right;
        pivot.right = node;

        updateHeight(node);
        updateHeight(pivot);
        return pivot;
    }

    protected Node leftRotate(Node node) {
        Node pivot = node.right;
        node.right = pivot.left;
        pivot.left = node;

        updateHeight(node);
        updateHeight(pivot);
        return pivot;
    }

    protected Node rebalance(Node node) {
        updateHeight(node);

        if (balanceFactor(node) == 2) {
            if (balanceFactor(node.right) < 0)
                node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        if (balanceFactor(node) == -2) {
            if (balanceFactor(node.left) > 0)
                node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        return node;
    }

    protected Node insertNode(Node node, K key, T entity) {
        if (node == null) return new Node(key, entity);

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            node.left = insertNode(node.left, key, entity);
        }
        else if (cmp > 0) {
            node.right = insertNode(node.right, key, entity);
        }

        else throw new IllegalArgumentException(
                "Занятие с ключом " + key + " уже существует");

        return rebalance(node);
    }

    protected  Node findMin(Node node) {
        return node.left != null ? findMin(node.left) : node;
    }

    protected Node removeMin(Node node) {
        if (node.left == null)
            return node.right;

        node.left = removeMin(node.left);
        return rebalance(node);
    }

    protected Node removeNode(Node node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0)      node.left  = removeNode(node.left, key);
        else if (cmp > 0) node.right = removeNode(node.right, key);
        else {
            Node leftChild = node.left;
            Node rightChild = node.right;
            if (rightChild == null) return leftChild;

            Node successor = findMin(rightChild);
            successor.right = removeMin(rightChild);
            successor.left  = leftChild;
            return rebalance(successor);
        }
        return rebalance(node);
    }

    protected Node findNode(Node node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0)  return findNode(node.left, key);
        else if (cmp > 0) return findNode(node.right, key);
        return node;
    }

    protected void inorderTraversal(List<T> res, Node p) {
        if (p == null) return;
        inorderTraversal(res, p.left);
        res.add(p.value);
        inorderTraversal(res, p.right);
    }

    @Override
    public Optional<T> findByKey(K key) {
        Node node = findNode(root, key);
        return node == null ? Optional.empty() : Optional.of(node.value);
    }

    @Override
    public void add(K key, T entity) {
        root = insertNode(root, key, entity);
        size++;
    }

    @Override
    public boolean removeByKey(K key) {
        if (findByKey(key).isEmpty()) return false;
        root = removeNode(root, key);
        size--;
        return true;
    }

    @Override public int size() { return size;}
}
