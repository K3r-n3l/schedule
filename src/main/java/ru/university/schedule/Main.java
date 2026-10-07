package ru.university.schedule;

import ru.university.schedule.model.Lesson;
import ru.university.schedule.store.AvlTree;

public class Main {
    public static void main() {
        AvlTree tree = new AvlTree();

        tree.insert(new Lesson());
    }
}
