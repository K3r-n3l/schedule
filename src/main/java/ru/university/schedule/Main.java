package ru.university.schedule;

import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.store.AvlTree;

import java.time.LocalDateTime;

public class Main {
    public static void main() {
        AvlTree tree = new AvlTree();

        Lesson lecture = new Lecture(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "ИВТ-552",
                "Программирование",
                "7-218",
                "mr.prepod",
                "first stream"
        );

        Lesson practice = new Practice(
                LocalDateTime.parse("2026-06-06T10:30:00"),
                "АБ-625",
                "Элтех",
                "999",
                "prepod 2",
                2
        );

        Lesson lesson = new Lesson(
                LocalDateTime.parse("2026-06-06T13:30:00"),
                "ИВТ-552",
                "Занятие",
                "7-218",
                "mr.prepod"
        );

        Lesson cancelled = new Cancelled(
                LocalDateTime.parse("2026-06-06T12:32:00"),
                "РРРФВ",
                "Забыл",
                "1221",
                "eaa"
        );

        tree.root = tree.insert(tree.root, lecture);
        tree.root = tree.insert(tree.root, practice);
        tree.root = tree.insert(tree.root, cancelled);
        tree.root = tree.insert(tree.root, lesson);

        tree.show(tree.root);
        tree.remove(tree.root, LocalDateTime.parse("2026-06-06T12:32:00") );
        tree.show(tree.root);
    }
}
