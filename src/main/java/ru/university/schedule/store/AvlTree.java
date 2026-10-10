package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class AvlTree {
    private static final class Node {
        LessonKey key;
        Lesson value;
        Node left, right;
        int height;

        Node(LessonKey key, Lesson value) {
            this.key = key;
            this.value = value;
            height = 1;
        }
    }

    private Node root;
    private int size;

    public int size() { return size; }

    private static int height(Node p) { return p == null ? 0 : p.height; }
    private static int balanceFactor(Node p) { return height(p.right) - height(p.left); }

    private static void updateHeight(Node p) {
        int rheight = height(p.right);
        int lheight = height(p.left);
        p.height = Math.max(rheight, lheight) + 1;
    }

    private static Node rightRotate(Node p) {
        Node q = p.left;
        p.left = q.right;
        q.right = p;

        updateHeight(p);
        updateHeight(q);
        return q;
    }

    private static Node leftRotate(Node q) {
        Node p = q.right;
        q.right = p.left;
        p.left = q;

        updateHeight(q);
        updateHeight(p);
        return q;
    }

    private static Node rebalance(Node p) {
        updateHeight(p);

        if (balanceFactor(p) == 2) {
            if (balanceFactor(p.right) < 0)
                p.right = rightRotate(p.right);
            return leftRotate(p);
        }
        if (balanceFactor(p) == -2) {
            if (balanceFactor(p.left) > 0)
                p.left = leftRotate(p.left);
            return rightRotate(p);
        }
        return p;
    }

    public void add(Lesson lesson) {
        LessonKey key = LessonKey.of(lesson);
        root = insertNode(root, key, lesson);
        size++;
    }

    private Node insertNode(Node node, LessonKey key, Lesson value) {
        if (node == null) return new Node(key, value);

        int cmp = key.compareTo(node.key);
        if (cmp < 0)      node.left  = insertNode(node.left, key, value);
        else if (cmp > 0) node.right = insertNode(node.right, key, value);
        else throw new IllegalArgumentException(
                    "Занятие с ключом " + key + " уже существует");

        return rebalance(node);
    }

    private static Node findMin(Node p) {
        return p.left != null ? findMin(p.left) : p;
    }

    private static Node removeMin(Node p) {
        if (p.left == null)
            return p.right;

        p.left = removeMin(p.left);
        return rebalance(p);
    }

    private Node removeNode(Node node, LessonKey key) {
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

    private Node findNode(Node node, LessonKey key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0)  return findNode(node.left, key);
        else if (cmp > 0) return findNode(node.right, key);
        return node;
    }

    public Lesson find(LessonKey key) {
        return findNode(root, key).value;
    }

    public boolean remove(LessonKey key) {
        if (find(key) == null) return false;
        root = removeNode(root, key);
        size--;
        return true;
    }

    private void traversal(List<Lesson> lst, Node p) {
        if (p == null) return ;
        traversal(lst, p.left);
        lst.add(p.value);
        traversal(lst, p.right);
    }

    public List<Lesson> snapshot() {
        List<Lesson> lst = new ArrayList<>();
        traversal(lst, root);
        return lst;
    }
}
