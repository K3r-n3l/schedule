package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

public class AvlTree {
    private static final class Node {
        LocalDateTime key;
        Lesson value;
        Node left, right;
        int height;

        Node(Lesson value) {
            this.key = value.getDateTime();
            this.value = value;
            left = right = null;
            height = 1;
        }
    }

    private Node root;

    private static int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private static int bfactor(Node node) {
        return height(node.right) - height(node.left);
    }

    private static int fixHeight(Node node) {
        int rheight = height(node.right);
        int lheight = height(node.left);
        return (Math.max(rheight, lheight)) + 1;
    }

    private static Node rightRotate(Node p) {
        Node q = p.left;
        p.left = q.right;
        q.right = p;

        fixHeight(p);
        fixHeight(q);

        return q;
    }

    private static Node leftRotate(Node q) {
        Node p = q.right;
        q.right = p.left;
        p.left = q;

        fixHeight(p);
        fixHeight(q);

        return q;
    }

    private static Node balance(Node p) {
        fixHeight(p);

        if (bfactor(p) == 2) {
            if (bfactor(p.right) < 0)
                p.right = rightRotate(p.right);
            return leftRotate(p);
        }
        if (bfactor(p) == -2) {
            if (bfactor(p.left) < 0)
                p.left = leftRotate(p.left);
            return rightRotate(p);
        }
        return p;
    }

    public static Node insert(Node p, Lesson lesson) {
        if (p == null) return new Node(lesson);

        LocalDateTime key = lesson.getDateTime();

        if (key.compareTo(p.key) < 0)
            p.left = insert(p.left, lesson);
        else
            p.right = insert(p.right, lesson);

        return balance(p);
    }

    public static void show(Node p) {
        if (p == null) return;

        Queue<Node> q = new LinkedList<>();
        q.offer(p);

        while (!q.isEmpty()) {
            Node cur = q.peek();
            System.out.println(cur.value);

            if (cur.right != null) q.offer(cur.right);
            if (cur.left != null) q.offer(cur.left);
            q.poll();
        }
    }
}
