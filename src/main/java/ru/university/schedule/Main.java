package ru.university.schedule;

import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.store.AvlTree;
import ru.university.schedule.store.LessonKey;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main() {
        Lesson lecture = new Lecture(
                LocalDateTime.parse("2026-06-06T14:30:00"),
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

        AvlTree tree = new AvlTree();
        tree.add(lesson);
        tree.add(cancelled);
        tree.add(practice);
        tree.add(lecture);
       // tree.show();

        List<Lesson> res = tree.snapshot();

        //for (var les : res) System.out.println(les);

        LessonKey key = new LessonKey(LocalDateTime.parse("2026-06-06T14:30:00"), "ИВТ-552");
        Lesson ls = tree.find(key);
        System.out.println(ls);

    }
}
